package ca.qc.bdeb.p55.georges.photos;

import android.net.Uri;

import java.net.URI;

public class PhotoItem {
    private static long counter = 1;
    private long tag;
    String  uri;
    String  filename;
    public PhotoItem(String uri, String filename)
    {
        this.uri = uri;
        this.filename = filename;
        tag = getNext();
    }

    private static synchronized long getNext() {
        return ++counter;
    }
    public Uri getUri() {
        return Uri.parse(uri);
    }
    public String getFilename() {
        return filename;
    }

    public String getName() {
        String  str = filename.toString();

        int pos = str.lastIndexOf('/');
        if (pos > 0) {
            str = str.substring(pos+1);
            if (str.isEmpty()) {
                str = "?";
            }
        }
        return str;
    }
    public long getTag() {
        return tag;
    }

    public void updateTag() {
        tag = getNext();
    }
}
