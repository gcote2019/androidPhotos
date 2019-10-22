package ca.qc.bdeb.p55.georges.photos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton    fabPhoto;
    private FloatingActionButton    fabToutVoir;

    private RecyclerView recyclerView;
    private ImageRecyclerViewAdapter adapter = null;
    private ImageViewPagerAdapter viewPagerAdapter = null;
    private RecyclerView.LayoutManager  layoutManager;
    private ArrayList<PhotoItem> list = new ArrayList<>();

    private String currentPhotoPath;
    private Uri    currentUri;

    private static final String         PREFS = "preferences";
    private static final String         SHARED_PREFERENCES_LIST = "prefs_list";

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final String PHOTO = "420-P55-BB";
    private static final String FILENAME = "filename";
    private static final String URI = "uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        adapter = new ImageRecyclerViewAdapter(list);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        fabPhoto = findViewById(R.id.floatingActionPrendreUnePhoto);
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prendreUnePhoto();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long) viewHolder.itemView.getTag());
            }
        }).attachToRecyclerView(recyclerView);

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new ImageViewPagerAdapter(this, list);
        viewPager.setAdapter(viewPagerAdapter);

        lire();
    }


    private void removeItem(long tag) {
        int pos = 0;
        for (PhotoItem item : list) {
            if (tag == item.getTag()) {
                try {
                    new File(item.getFilename()).delete();
                }
                catch(SecurityException ex) {
                    // il faudrait avertir
                    ex.printStackTrace();
                }
                list.remove(pos);
                adapter.notifyItemRemoved(pos);
                viewPagerAdapter.notifyDataSetChanged();
                return;
            }
            pos++;
        }
    }
    /*
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    */

    private File createImageFile() throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                PHOTO,
                ".jpg",
                storageDir
        );

        // Gardons le nom du fichier pour plus tard
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void prendreUnePhoto() {
        Intent prendreUnePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Il y a une caméra?
        if (prendreUnePhotoIntent.resolveActivity(getPackageManager()) != null) {
            // Créer le fichier
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Il faudrait peut-être avertir l'usager
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName(),
                        photoFile);
                currentUri = photoURI;
                prendreUnePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(prendreUnePhotoIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString(FILENAME);
            String uri = savedInstanceState.getString(URI);
            if (!currentPhotoPath.isEmpty() && !uri.isEmpty()) {
                currentUri = Uri.parse(uri);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!currentPhotoPath.isEmpty()) {
            outState.putString(FILENAME, currentPhotoPath);
        }
        if (currentUri != null) {
            outState.putString(URI, currentUri.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (currentPhotoPath.isEmpty()) {
            return;
        }

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                list.add(new PhotoItem(currentUri.toString(), currentPhotoPath));
                adapter.notifyItemInserted(list.size() - 1);
                viewPagerAdapter.notifyDataSetChanged();
            }
            else
            {
                try {
                    new File(currentPhotoPath).delete();
                }
                catch(SecurityException ex) {
                    ex.printStackTrace();
                }
            }
            currentPhotoPath = "";
        }
    }

    private void lire() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.contains(SHARED_PREFERENCES_LIST)) {
            Gson gson = new Gson();
            String json = prefs.getString(SHARED_PREFERENCES_LIST, "");
            ArrayList<PhotoItem> newList = gson.fromJson(json, new TypeToken<ArrayList<PhotoItem>>(){}.getType());
            if (newList.size() > 0) {
                list.clear();
                for (PhotoItem item : newList) {
                    item.updateTag();
                    list.add(item);
                }
                adapter.notifyDataSetChanged();
                viewPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void ecrire() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SHARED_PREFERENCES_LIST, gson.toJson(list));
        editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ecrire();
    }
}
