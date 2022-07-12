package devesh.app.database.MusicMain;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "music",indices = {@Index(value = {"song_id","song_title"},
        unique = true)})
public class Music {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "song_id")
    public String Song_ID;

    @ColumnInfo(name = "uri")
    public String ContentUri;

    @ColumnInfo(name = "song_title")
    public String Song_Title;

    @ColumnInfo(name = "album_title")
    public String Album_Title;

    @ColumnInfo(name = "album_id")
    public String Album_ID;

    @ColumnInfo(name = "artist_name")
    public String Artist_Name;

    @ColumnInfo(name = "album_artist")
    public String Album_Artist;

    @ColumnInfo(name = "artist_id")
    public String Artist_ID;

    @ColumnInfo(name = "genre")
    public String Genre;

    @ColumnInfo(name = "genre_id")
    public String Genre_ID;

    @ColumnInfo(name = "duration")
    public long Duration;

    @ColumnInfo(name = "composer")
    public String Composer;

    @ColumnInfo(name = "cd_track_no")
    public int CD_Track_Number;

    @ColumnInfo(name = "year")
    public int Year;

    @ColumnInfo(name = "album_art_uri")
    public String AlbumArtUri;




}



