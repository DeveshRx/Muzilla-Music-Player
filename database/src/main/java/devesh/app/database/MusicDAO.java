package devesh.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import devesh.app.database.MusicMain.Album;
import devesh.app.database.MusicMain.Music;

@Dao
public interface MusicDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Music> musicList);

    @Delete
    void delete(Music music);

    @Query("SELECT * FROM music")
    List<Music> getAll();


    @Query("SELECT * FROM music WHERE song_id IN (:id)")
    List<Music> getMusicByID(String id);

    @Query("SELECT * FROM music GROUP BY album_title ORDER BY album_title ASC")
    List<Music> getAllAlbums();

    @Query("SELECT * FROM music GROUP BY artist_name ORDER BY artist_name ASC")
    List<Music> getAllArtist();

    @Query("SELECT * FROM music GROUP BY genre ORDER BY genre ASC")
    List<Music> getAllGenre();


    @Query("SELECT * FROM music WHERE album_title IN (:id)")
    List<Music> getMusicByAlbumTitle(String id);

    @Query("SELECT * FROM music WHERE artist_name IN (:artistName) GROUP BY album_title ORDER BY album_title ASC")
    List<Music> getMusicAlbumsByArtist(String artistName);

    @Query("DELETE FROM music")
    void nukeMusicDB();




}
