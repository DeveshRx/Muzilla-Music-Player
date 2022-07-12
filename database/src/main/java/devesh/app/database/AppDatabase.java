package devesh.app.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import devesh.app.database.MusicMain.Album;
import devesh.app.database.MusicMain.Music;


@Database(entities = {Music.class, Album.class},
        version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MusicDAO musicDAO();
}