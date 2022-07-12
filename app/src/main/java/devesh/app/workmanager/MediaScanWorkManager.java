package devesh.app.workmanager;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

import devesh.app.R;
import devesh.app.database.AppDatabase;
import devesh.app.database.MusicMain.Music;

public class MediaScanWorkManager extends Worker {
    List<Music> musicList = new ArrayList<>();
    AppDatabase appDatabase;
String TAG="MSWM: ";

    public MediaScanWorkManager(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);

        appDatabase = Room
                .databaseBuilder(context, AppDatabase.class, context.getString(R.string.DATABASE_NAME))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

    }


    @Override
    public Result doWork() {

        getMusic();

        return Result.success();
    }

    void getMusic(){
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,

                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,

                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,

                MediaStore.Audio.Media.GENRE,
                MediaStore.Audio.Media.GENRE_ID,

                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.CD_TRACK_NUMBER,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.TITLE,
        };


        String selection = MediaStore.Audio.Media.IS_MUSIC +
                " >= ?";
        String[] selectionArgs = new String[]{
                String.valueOf(1)
        };
        /*String selection = MediaStore.Audio.Media.DURATION +
                " >= ?";
        String[] selectionArgs = new String[]{
                String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))
        };*/


        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";


        try (Cursor cursor = getApplicationContext().getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);

            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int albumArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
            int genreColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE);
            int genreIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE_ID);
            int composerColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER);
            int cdTrackNoColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.CD_TRACK_NUMBER);
            int yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
            int isMusicColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC);
            int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);

            if (!musicList.isEmpty()) {
                musicList.clear();

            }

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                int size = cursor.getInt(sizeColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);


                String title = cursor.getString(titleColumn);
                int isMusic = cursor.getInt(isMusicColumn);

                Music music = new Music();
                music.ContentUri = String.valueOf(contentUri);
                music.Song_Title = title;
                music.Duration = duration;
                music.Song_ID = String.valueOf(id);
                music.Album_Title = cursor.getString(albumColumn);
                music.Album_ID = cursor.getString(albumIdColumn);
                music.Artist_Name = cursor.getString(artistColumn);
                music.Album_Artist = cursor.getString(albumArtistColumn);
                music.Artist_ID = cursor.getString(artistIdColumn);
                music.Genre = cursor.getString(genreColumn);
                music.Genre_ID = cursor.getString(genreIdColumn);
                music.Composer = cursor.getString(composerColumn);
                music.CD_Track_Number = cursor.getInt(cdTrackNoColumn);
                music.Year = cursor.getInt(yearColumn);

                int thumbColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                int _thumpId = cursor.getInt(thumbColumn);
                Uri imageUri_t = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, _thumpId);

                music.AlbumArtUri = String.valueOf(imageUri_t);
                musicList.add(music);

                Log.d(TAG, "getMedia: " + title);

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                // videoList.add(new Video(contentUri, name, duration, size));
            }

            appDatabase.musicDAO().nukeMusicDB();
            appDatabase.musicDAO().insertAll(musicList);

        }

    }

}
