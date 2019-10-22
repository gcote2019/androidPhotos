package ca.qc.bdeb.p55.georges.photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ItemViewHolder> {
    private List<PhotoItem> imageList;
    private OnSelectionClickListener listener;

    public interface OnSelectionClickListener {
        void onSelectPhotoClick(int position);
    }

    public void setOnSelectionClickListener(OnSelectionClickListener listener) {
        this.listener = listener;
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView    imageViewPhoto;
        public TextView     textViewNom;

        public ItemViewHolder(View itemView, final OnSelectionClickListener listener) {
            super(itemView);

            imageViewPhoto = itemView.findViewById(R.id.recyclerview_imageView);

            imageViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // vérifier que le listener n'est pas null
                    if (listener != null) {
                        int position = getAdapterPosition();
                        // la position est valide?
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onSelectPhotoClick(position);
                        }
                    }
                }
            });

            textViewNom = itemView.findViewById(R.id.recyclerview_textView);
        }
    }

    public ImageRecyclerViewAdapter(List<PhotoItem> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view, parent, false);
        ItemViewHolder ivh = new ItemViewHolder(v, listener);
        return ivh;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        //Picasso.get().load(imageList.get(position).getUri()).into(holder.imageViewPhoto);
        holder.imageViewPhoto.setImageURI(imageList.get(position).getUri());
        holder.textViewNom.setText((imageList.get(position).getName()));
        holder.itemView.setTag(imageList.get(position).getTag());
     }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
}
