package devesh.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.room.Room;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import devesh.app.common.tools.AlbumArtFromId;
import devesh.app.common.tools.NowPlayingSess;
import devesh.app.database.AppDatabase;
import devesh.app.database.MusicMain.Music;
import devesh.app.viewmodel.AppLiveModel;


public class MediaPlayerService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    final String TAG = "MPS: ";
    // private NotificationManager mNM;
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private final int NOTIFICATION = 5015;

    Notification notification;
    ExoPlayer player;
    NotificationManagerCompat notificationManager;
    RemoteViews notificationLayout;
    WifiManager.WifiLock wifiLock;
    PowerManager.WakeLock wakeLock;
    PlayerNotificationManager playerNotificationManager;
    Runnable runnable;
    Handler handler;
    MediaItem mediaItem;
    Constants constants=new Constants();
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    AppDatabase appDatabase;
    NowPlayingSess nowPlayingSess;


    public MediaPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
       /* WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyWifiLock");
        wifiLock.acquire();
     */
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:MyWakeLock");
        wakeLock.acquire(60*60*1000L /*10 minutes*/);

        mediaSession = new MediaSessionCompat(this, "mediaplayerservice");

        mediaSessionConnector = new MediaSessionConnector(mediaSession);

        nowPlayingSess=new NowPlayingSess(this);

        player = new ExoPlayer.Builder(this).build();

        Player.Listener pl =new Player.Listener() {
            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                Log.d(TAG, "onIsLoadingChanged: ");
                //ExoPlayerStatus exoPlayerStatus = new ExoPlayerStatus();
                if (isLoading) {
                  //  exoPlayerStatus.Status = HomeActivity.EXO_PLAYER_EVENT_STATUS_LOADING;
                } else {
                   // exoPlayerStatus.Status = HomeActivity.EXO_PLAYER_EVENT_STATUS_LOADED;
                }
              //  EventBus.getDefault().post(exoPlayerStatus);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    // Active playback.
               //     MonitorProgress();
                } else {
                    // Not playing because playback is paused, ended, suppressed, or the player
                    // is buffering, stopped or failed. Check player.getPlayWhenReady,
                    // player.getPlaybackState, player.getPlaybackSuppressionReason and
                    // player.getPlaybackError for details.
                }
                Log.d(TAG, "onIsPlayingChanged: " + isPlaying);
                NowPlayingSess.MusicSess ms=nowPlayingSess.getNowPlaying();
                if(ms!=null){

                    ms.isPlaying=isPlaying;

                    int SP=NowPlayingSess.STATE_PAUSE;
                    if(isPlaying){
                        SP=NowPlayingSess.STATE_PLAYING;
                    }
                    nowPlayingSess.setNowPlaying(ms,SP);
                }
            }

            @Override
            public void onMetadata(Metadata metadata) {
                Log.d(TAG, "onMetadata: " + metadata.toString());
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Throwable cause = error.getCause();
                if (cause instanceof HttpDataSource.HttpDataSourceException) {
                    // An HTTP error occurred.
                    HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                    // This is the request for which the error occurred.
                    DataSpec requestDataSpec = httpError.dataSpec;
                    // It's possible to find out more about the error both by casting and by
                    // querying the cause.
                    if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                        // Cast to InvalidResponseCodeException and retrieve the response code,
                        // message and headers.
                    } else {
                        // Try calling httpError.getCause() to retrieve the underlying cause,
                        // although note that it may be null.
                    }
                }
                Log.e(TAG, "onPlayerError: " + error);            }

            @Override
            public void onPlayerErrorChanged(@Nullable PlaybackException error) {
                Log.e(TAG, "onPlayerErrorChanged: " + error);
            }

            @Override
            public void onEvents(Player player, Player.Events events) {
                Log.d(TAG, "onEvents: " + events);
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
                        || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
                    //    uiModule.updateUi(player);
                }

                if(player!=null){
                    Log.d(TAG, "onEvents: player.isPlaying(): "+player.isPlaying());

                }



            }

            @Override
            public void onVolumeChanged(float volume) {
                Log.d(TAG, "onVolumeChanged: " + volume);

            }
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_IDLE) {
                    Log.d(TAG, "onPlaybackStateChanged: STATE_IDLE");
                } else if (playbackState == Player.STATE_ENDED) {
                    Log.d(TAG, "onPlaybackStateChanged: STATE_ENDED");
                } else if (playbackState == Player.STATE_READY) {
                    Log.d(TAG, "onPlaybackStateChanged: STATE_READY");
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Log.d(TAG, "onPlaybackStateChanged: STATE_BUFFERING");
                } else if (playbackState == Player.EVENT_PLAYBACK_STATE_CHANGED) {
                    Log.d(TAG, "onPlaybackStateChanged: EVENT_PLAYBACK_STATE_CHANGED");
                }

            }

        };
        player.addListener(pl);

        appDatabase = Room
                .databaseBuilder(this, AppDatabase.class, getString(R.string.DATABASE_NAME))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//      PodcastID = (String) intent.getExtras().get(HomeActivity.INTENT_PODCAST_ID);
        try {

            if (intent != null) {

                if (intent.getAction() != null) {
                    Log.d(TAG, "onStartCommand: ");
                //    Log.d(TAG, intent.getExtras().toString());
                    if (intent.getAction().equals(constants.PLAY_ACTION)) {
                        Log.d(TAG, "onStartCommand: Play Button");
                        //Toast.makeText(this, "Play Button", Toast.LENGTH_SHORT).show();

                        String contentURI =(String) intent.getExtras().get(constants.MEDIA_URI);
                        String songID =(String) intent.getExtras().get(constants.SONG_ID);
                        Log.d(TAG, "onStartCommand: contentURI: "+contentURI);
                        Log.d(TAG, "onStartCommand: songID: "+songID);
                        UpdateNowPlaying(songID,true);
                        PlaySong(Uri.parse(contentURI));
                    //    UpdateNotification();

                    } else if (intent.getAction().equals(constants.PAUSE_ACTION)) {
                        Log.d(TAG, "onStartCommand: Pause Button");
                  //      Toast.makeText(this, "Pause Button", Toast.LENGTH_SHORT).show();
                        UpdateNowPlaying(false);
                        PauseSong();

                    }else{

                    }

                    UpdateNotification();

                }

            }


        } catch (Exception e) {
            Log.e(TAG, "onStartCommand: " + e);
        }


        showNotification();
        setMediaController();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        releasePlayer();

        if(appDatabase!=null){
            if(appDatabase.isOpen()){
                appDatabase.close();
            }
        }
        super.onDestroy();


    }


    private void showNotification() {


        createNotificationChannel();


        // TODO: Podcast Cover Pic in Notification with Glide
  /*      NotificationTarget notificationTarget = new NotificationTarget(
                this,
                R.id.PodcastCoverImageView,
                notificationLayout,
                notification,
                NOTIFICATION);
        Glide.with(this)
                .asBitmap()
                .load(NowPlayingPodcast.CoverPic)

                .into(notificationTarget);
*/

        if(nowPlayingSess==null){
            nowPlayingSess=new NowPlayingSess(this);
        }
     //   Music music=nowPlayingSess.getNowPlaying().music;

        startForeground(NOTIFICATION, getNotification());
        //notificationManager.notify(NOTIFICATION, notification);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Now Playing",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    void UpdateNotification(){
        Log.d(TAG, "UpdateNotification: ");
       /* List<Music> ll=new ArrayList<>();
        ll=appDatabase.musicDAO().getMusicByID(songID);
        Music music=ll.get(0);
        */
   /**     Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        // Play Pause Button
        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(constants.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_IMMUTABLE);

        notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);

        notificationLayout.setTextViewText(R.id.SongNameTextView, music.Song_Title);
        notificationLayout.setTextViewText(R.id.ArtistNameTextView, music.Album_Artist);
        notificationLayout.setOnClickPendingIntent(R.id.PausePlayButton, pplayIntent);


        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                //    .setContentTitle("Title")
                //   .setContentText("PodcastTitle")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                // .setStyle(mediaStyle)
                .setCustomContentView(notificationLayout)
                // .setCustomBigContentView(notificationLayoutExpanded)
                .build();

*/
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(NOTIFICATION,getNotification());
    }

    private void releasePlayer() {
        if (mediaSession != null) {
            mediaSession.setActive(false);

            mediaSession.release();
        }

    }

    void setMediaController() {
        mediaSessionConnector.setPlayer(player);
        mediaSessionConnector.setEnabledPlaybackActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_FAST_FORWARD
                        | PlaybackStateCompat.ACTION_REWIND
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                        | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
        );

        mediaSession.setActive(true);

    }

    void PlaySong(Uri contentURI){
 mediaItem = MediaItem.fromUri(contentURI);
        player.setMediaItem(mediaItem);
// Prepare the player.
        player.prepare();

        notificationLayout.setTextViewText(R.id.SongNameTextView, mediaItem.mediaMetadata.title);
        notificationLayout.setTextViewText(R.id.ArtistNameTextView, mediaItem.mediaMetadata.albumArtist);


// Start the playback.
        player.play();

    }

    void PauseSong(){
        player.pause();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    Notification getNotification()  {
        AlbumArtFromId albumArtFromId=new AlbumArtFromId();

        String Title="";
        String SubTitle="";
        Bitmap AlbumArt=null;
        Music music=new Music();
        try{
            music=nowPlayingSess.getNowPlaying().music;
        }catch (Exception e){

        }

        boolean isPlaying=false;

        if(music!=null){

            Title= music.Song_Title;
            SubTitle=music.Album_Artist;
            try {
                AlbumArt=albumArtFromId.getAlbumArtwork(this,music.Album_ID);
            }catch (Exception e){
                Log.e(TAG, "getNotification: "+e );
            }
try {
    isPlaying=nowPlayingSess.getNowPlaying().isPlaying;
}catch (Exception e){
    Log.e(TAG, "getNotification: "+e );
}

            Log.d(TAG, "getNotification: MUSIC #2");
            Log.d(TAG, "getNotification: isPlaying: "+isPlaying);

        }else{
            Log.d(TAG, "getNotification: MUSIC NULL #4345");
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //  notificationManager = NotificationManagerCompat.from(this);
        // Play Pause Button

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        if(isPlaying){
            playIntent.setAction(constants.PAUSE_ACTION);
            Log.d(TAG, "getNotification: constants.PAUSE_ACTION");
        }else{
            playIntent.setAction(constants.PLAY_ACTION);
            playIntent.putExtra(constants.MEDIA_URI, music.ContentUri);
            playIntent.putExtra(constants.SONG_ID, music.Song_ID);

            Log.d(TAG, "getNotification: constants.PLAY_ACTION");
        }



        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);
        //RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_layout);

        notificationLayout.setTextViewText(R.id.SongNameTextView,Title);
        notificationLayout.setTextViewText(R.id.ArtistNameTextView,SubTitle );
        notificationLayout.setOnClickPendingIntent(R.id.PausePlayButton, pplayIntent);
        if(AlbumArt!=null){
            notificationLayout.setImageViewBitmap(R.id.AlbumArtImageView3,AlbumArt);
        }

        if(isPlaying){
            notificationLayout.setImageViewResource(R.id.PlayPauseBTNImageView, devesh.app.common.R.drawable.pause_circle_48px);
        }else{
            notificationLayout.setImageViewResource(R.id.PlayPauseBTNImageView, devesh.app.common.R.drawable.play_circle_48px);
        }


        Log.d(TAG, "getNotification: UPDATED TEXT: Title:"+Title+"\nSubTitle:"+SubTitle+"\n");

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                //    .setContentTitle("Title")
                //   .setContentText("PodcastTitle")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                // .setStyle(mediaStyle)
                .setCustomContentView(notificationLayout)
                // .setCustomBigContentView(notificationLayoutExpanded)
                .build();

return notification;
    }

    private void UpdateNowPlaying(String SongID, boolean isPlaying){
        Music m;


             m=appDatabase.musicDAO().getMusicByID(SongID).get(0);
            NowPlayingSess.MusicSess ms=nowPlayingSess.getNowPlaying();
            ms.id=m.Song_ID;
            ms.music=m;
            ms.AlbumName=m.Album_Title;
            ms.SongName=m.Song_Title;
            ms.Artist=m.Album_Artist;


        if(ms!=null){

            ms.isPlaying=isPlaying;

            int SP=NowPlayingSess.STATE_PAUSE;
            if(isPlaying){
                SP=NowPlayingSess.STATE_PLAYING;
            }
            nowPlayingSess.setNowPlaying(ms,SP);
        }

    }

    private void UpdateNowPlaying(boolean isPlaying){
        NowPlayingSess.MusicSess ms=nowPlayingSess.getNowPlaying();

        if(ms!=null){
            ms.isPlaying=isPlaying;
            int SP=NowPlayingSess.STATE_PAUSE;
            if(isPlaying){
                SP=NowPlayingSess.STATE_PLAYING;
            }
            nowPlayingSess.setNowPlaying(ms,SP);
        }

    }


}