package devesh.app.common.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import devesh.app.common.R;
import devesh.app.database.MusicMain.Music;


public class NowPlayingSess {
    public static int STATE_PLAYING = 1;
    public static int STATE_PAUSE = 0;
    static String TAG = "NPS: ";
    static String MUSIC_FILE = "MUSIC_FILE";
    static String MUSIC_STATE_FILE = "MUSIC_STATE_FILE";
    Context mContext;
    SharedPreferences sharedPref;
    Gson gson;

    public NowPlayingSess(Context context) {
        mContext = context;
        sharedPref = context.getSharedPreferences(
                mContext.getString(R.string.NOW_PLAYING_SESS), Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void setNowPlaying(MusicSess music, int State) {

        String musicSTR = gson.toJson(music);
        setString(MUSIC_FILE, musicSTR);
        setInt(MUSIC_STATE_FILE,State);

    }

    public MusicSess getNowPlaying() {
        MusicSess musicSess = new MusicSess();

        try {
            musicSess = gson.fromJson(getString(MUSIC_FILE), MusicSess.class);

        } catch (Exception e) {
            Log.e(TAG, "getNowPlaying: " + e);
            musicSess = new MusicSess();
            musicSess.isPlaying=false;
            musicSess.music=new Music();
            musicSess.id="";
            musicSess.Artist="";
            musicSess.SongName="";
            musicSess.Artist="";
            musicSess.AlbumName="";
        }


        return musicSess;
    }

    public int getState(){
        int i=STATE_PAUSE;
        try{
            i=getInt(MUSIC_STATE_FILE);
        }catch (Exception e){
            Log.e(TAG, "getState: "+e );
        }
        return i;
    }



    private void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void setInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    private String getString(String key) {
        return sharedPref.getString(key, null);
    }

    private int getInt(String key) {
        return sharedPref.getInt(key, STATE_PAUSE);
    }



    public static class MusicSess {
        public String AlbumName="";
        public String Artist="";
        public String SongName="";
        public String id="";
        public boolean isPlaying=false;
        public Music music=new Music();
    }

}
