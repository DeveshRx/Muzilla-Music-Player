package devesh.app.common.tools;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.io.IOException;

public class AlbumArtFromId{


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Bitmap getAlbumArtwork(Context mContext,String album_Id) throws IOException {

        long albumId = Long.parseLong(album_Id);
        Uri contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumId
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return mContext.getApplicationContext().getContentResolver().loadThumbnail(contentUri, new Size(640, 480), null);
        } else {
            return null;
        }
    }
}
