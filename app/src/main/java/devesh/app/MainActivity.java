package devesh.app;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

import devesh.app.activities.SettingsActivity;
import devesh.app.common.tools.AlbumArtFromId;
import devesh.app.common.tools.NowPlayingSess;
import devesh.app.database.AppDatabase;
import devesh.app.database.MusicMain.Music;
import devesh.app.databinding.ActivityMainBinding;
import devesh.app.fragment.AlbumDetailsFragment;
import devesh.app.fragment.ArtistDetailsFragment;
import devesh.app.fragment.HomeFragment;
import devesh.app.fragment.PlayerFragment;
import devesh.app.viewmodel.AppLiveModel;
import devesh.app.workmanager.MediaScanWorkManager;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    ActivityMainBinding mBinding;

    Constants constants = new Constants();
    List<Music> musicList = new ArrayList<>();
    AppDatabase appDatabase;
    NowPlayingSess.MusicSess nowPlayingMusicSess;
    FragmentManager fragmentManager;
    Fragment fragmentScreen;
    NowPlayingSess nowPlayingManager;

    AppLiveModel appLiveModel;
    //NowPlayingSess nowPlayingSong = new NowPlaying();
    Fragment oldFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        nowPlayingMusicSess = new NowPlayingSess.MusicSess();
        nowPlayingManager = new NowPlayingSess(this);

        appDatabase = Room
                .databaseBuilder(this, AppDatabase.class, getString(R.string.DATABASE_NAME))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();


        fragmentManager = getSupportFragmentManager();

        mBinding.layoutAppbar.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.MenuItem_item) {
                Log.d(TAG, "onCreate: MenuItem_item Clicked");
                return true;
            }else if(item.getItemId() == R.id.MenuItem_Sync){
                Toast.makeText(this, "Media Scan Started", Toast.LENGTH_SHORT).show();
                getMedia();
                return true;
            }else if(item.getItemId()==R.id.MenuItem_Settings){
                Intent intent = new Intent(this, SettingsActivity.class);

                startActivity(intent);
                return true;

            }
            return false;
        });


        fragmentScreen = new HomeFragment();

        appLiveModel = new ViewModelProvider(this).get(AppLiveModel.class);

        CheckPermissions();

        getMedia();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(nowPlayingManager.getState()!=NowPlayingSess.STATE_PLAYING){
            StopMediaService();
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


    }

    @Override
    protected void onStart() {
        super.onStart();
        /*getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(mBinding.fragmentContainerFrame.getId(), AlbumListFragment.class, null)
                .commit();
        */



        if (fragmentScreen == null) {
            fragmentScreen = new HomeFragment();

        }

        if (fragmentManager.findFragmentByTag("home") == null) {

            if (fragmentManager.getFragments().isEmpty()) {
                fragmentManager.beginTransaction()
                        .add(mBinding.fragmentContainerFrame.getId(), fragmentScreen, "home")

                        .commit();

            }

        } else {

        }


        appLiveModel.getNowPlayingSong().observe(this, nowplayingSessVal -> {

            nowPlayingMusicSess = nowplayingSessVal;

            setNowPlayingSongUI();

        });

        nowPlayingMusicSess = nowPlayingManager.getNowPlaying();
        if (nowPlayingMusicSess == null) {
            nowPlayingMusicSess = appLiveModel.getNowPlayingSong().getValue();
        }
if(nowPlayingManager.getState()==NowPlayingSess.STATE_PAUSE){
    StartMediaService();
}

        setNowPlayingSongUI();

    }

    @Override
    public void onBackPressed() {

        if(fragmentManager.findFragmentByTag("nowplaying") != null){
            if(fragmentManager.findFragmentByTag("nowplaying").isVisible()){
                mBinding.miniplayer.getRoot().setVisibility(View.VISIBLE);
            }

        }

        super.onBackPressed();

        if (fragmentManager != null) {
            if (fragmentManager.findFragmentByTag("home") != null) {
                if (fragmentManager.findFragmentByTag("home").isVisible()) {
                    fragmentScreen = fragmentManager.findFragmentByTag("home");

                    if(nowPlayingManager.getState()!=NowPlayingSess.STATE_PLAYING){
                        StopMediaService();
                    }

                }
            }



        }


    }

    public void  getMedia(){

        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(MediaScanWorkManager.class)
                        .build();

        WorkManager
                .getInstance(this)
                .enqueue(uploadWorkRequest);

    }


    public void getMediax() {
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

        /*
        Cursor cursor = getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );




        while (cursor.moveToNext()) {
            // Use an ID column from the projection to get
            // a URI representing the media item itself.
            Log.d(TAG, "getMedia: " + cursor.toString());


        }
        */
    }

    public void playSong(Music music) {

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(constants.PLAY_ACTION);
        playIntent.putExtra(constants.MEDIA_URI, music.ContentUri);
        playIntent.putExtra(constants.SONG_ID, music.Song_ID);

        if (nowPlayingMusicSess == null) {
            nowPlayingMusicSess = new NowPlayingSess.MusicSess();
        }
        nowPlayingMusicSess.isPlaying = true;
        nowPlayingMusicSess.music = music;

        appLiveModel.getNowPlayingSong().postValue(nowPlayingMusicSess);

        nowPlayingManager.setNowPlaying(nowPlayingMusicSess, NowPlayingSess.STATE_PLAYING);

        startService(playIntent);
        setNowPlayingSongUI();

    }

    public void PlayPauseButton(View v){
        if(!nowPlayingMusicSess.isPlaying){
            playSong(nowPlayingMusicSess.music);
        }else{
            pauseSong();
        }

    }

    public void pauseSong() {

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(constants.PAUSE_ACTION);

        if (nowPlayingMusicSess == null) {
            nowPlayingMusicSess = new NowPlayingSess.MusicSess();
        }
        nowPlayingMusicSess.isPlaying = false;

        appLiveModel.getNowPlayingSong().postValue(nowPlayingMusicSess);

        nowPlayingManager.setNowPlaying(nowPlayingMusicSess, NowPlayingSess.STATE_PAUSE);

        startService(playIntent);
        setNowPlayingSongUI();

    }


    public void SelectItem(Music music, String type) {
        Log.d(TAG, "SelectItem: " + music.Song_Title);
        Log.d(TAG, "SelectItem: " + type);

        Bundle bundle = new Bundle();
        bundle.putString(constants.ADAPTER_MODE, type);

        if (type.equals(constants.ADAPTER_MODE_ALBUM)) {
            bundle.putString(constants.ADAPTER_MODE_ALBUM, music.Album_Title);
            setFragment(new AlbumDetailsFragment(), bundle, type);

        } else if (type.equals(constants.ADAPTER_MODE_GENRE)) {
            bundle.putString(constants.ADAPTER_MODE_GENRE, music.Genre);
        } else if (type.equals(constants.ADAPTER_MODE_ARTIST)) {
            bundle.putString(constants.ADAPTER_MODE_ARTIST, music.Artist_Name);
            setFragment(new ArtistDetailsFragment(), bundle, type);

        }



    }

    void StartMediaService() {
        Intent intent = new Intent(getBaseContext(), MediaPlayerService.class);
        //intent.putExtra(INTENT_PODCAST_ID, episode.PodcastID);
        ContextCompat.startForegroundService(this, intent);
    }

    void StopMediaService() {
        Intent intent = new Intent(getBaseContext(), MediaPlayerService.class);
        // intent.putExtra(INTENT_PODCAST_ID, episode.PodcastID);
        // intent.putExtra(INTENT_EPIDOSE_ID, episode.guid);
        // intent.putExtra(INTENT_DOWNLOAD_URL, episode.DownloadURL);
        //    startService(intent);
        stopService(intent);

    }

    void setFragment(Fragment fragment, Bundle bundle, String tag) {
        oldFrag = fragmentScreen;
        fragmentScreen = fragment;
        if (bundle != null) {

            fragmentScreen.setArguments(bundle);

        }


        fragmentManager.beginTransaction()
                .hide(oldFrag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(mBinding.fragmentContainerFrame.getId(), fragmentScreen, tag)
                .setReorderingAllowed(true)
                .addToBackStack("app")
                .commit();



        /*
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)

                .addToBackStack("home")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(mBinding.fragmentContainerFrame.getId(), fragment, null)
                .commit();*/
    }

    void setNowPlayingSongUI() {
        if (nowPlayingMusicSess != null) {
            if (nowPlayingMusicSess.music != null) {
                mBinding.miniplayer.getRoot().setVisibility(View.VISIBLE);
                mBinding.miniplayer.SongName.setText(nowPlayingMusicSess.music.Song_Title);
                mBinding.miniplayer.ArtistName.setText(nowPlayingMusicSess.music.Album_Artist);

                AlbumArtFromId albumArtFromId=new AlbumArtFromId();
                try {
                    mBinding.miniplayer.AlbumArtImageView.setImageBitmap(albumArtFromId.getAlbumArtwork(this,nowPlayingMusicSess.music.Album_ID));
                }catch (Exception e){
                    Log.e(TAG, "setNowPlayingSongUI: "+e );
                }

                if(nowPlayingMusicSess.isPlaying){
                    mBinding.miniplayer.PlayPauseButton.setImageResource(devesh.app.common.R.drawable.pause_circle_48px);
                }else{
                    mBinding.miniplayer.PlayPauseButton.setImageResource(devesh.app.common.R.drawable.play_circle_48px);
                }


            }else{
                // not available
                mBinding.miniplayer.getRoot().setVisibility(View.GONE);
            }
        }else{
            // not available
            mBinding.miniplayer.getRoot().setVisibility(View.GONE);
        }


    }

    public void OpenNowPlaying(View v){
// TODO: open now playing screen
      /*
        setFragment(new PlayerFragment(),null,"nowplaying");
        mBinding.miniplayer.getRoot().setVisibility(View.INVISIBLE);
       */

    }

    void CheckPermissions(){
        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE};
        Permissions.check(this/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                getMedia();

            }


            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
            }
        });
    }

}