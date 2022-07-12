package devesh.app.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import devesh.app.common.tools.NowPlayingSess;



public class AppLiveModel extends ViewModel {

    private MutableLiveData<NowPlayingSess.MusicSess> NowPlayingSong;
    public MutableLiveData<NowPlayingSess.MusicSess> getNowPlayingSong() {
        if (NowPlayingSong == null) {
            NowPlayingSong = new MutableLiveData<NowPlayingSess.MusicSess>();

        }
        return NowPlayingSong;
    }








}
