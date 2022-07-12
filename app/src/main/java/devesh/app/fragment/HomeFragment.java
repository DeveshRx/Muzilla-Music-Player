package devesh.app.fragment;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.IOException;
import java.util.List;

import devesh.app.Constants;
import devesh.app.MainActivity;
import devesh.app.R;


import devesh.app.database.AppDatabase;
import devesh.app.database.MusicMain.Music;
import devesh.app.databinding.FragmentHomeBinding;
import devesh.app.common.tools.NowPlayingSess;


public class HomeFragment extends Fragment {
    String TAG = "AlbumFragment";

    Constants constants=new Constants();

    AppDatabase appDatabase;
    NowPlayingSess nowPlayingSess;

    FragmentHomeBinding mBinding;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //        mParam1 = getArguments().getString(ARG_PARAM1);
            //        mParam2 = getArguments().getString(ARG_PARAM2);
        }

        nowPlayingSess=new NowPlayingSess(getActivity());


        appDatabase = Room
                .databaseBuilder(getActivity(), AppDatabase.class, getString(R.string.DATABASE_NAME))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        LoadMusic();

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onStart() {
        super.onStart();


        mBinding.SongChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d(TAG, "onStart: SongChipGroup: "+checkedId);
            if(checkedId==R.id.ChipAlbum){
                Log.d(TAG, "onStart: Chip: Album ");
                LoadAlbumView();
            }else if(checkedId==R.id.ChipAllSongs){
                Log.d(TAG, "onStart: Chip: Song");
                LoadSongsView();
            }else if(checkedId==R.id.ChipArtist){
                Log.d(TAG, "onStart: Chip: Artist");
                LoadArtistView();
            }else if(checkedId==R.id.ChipGenre){
                Log.d(TAG, "onStart: Chip: Genre");
                LoadGenreView();
            }


        });

        int selectedChip=mBinding.SongChipGroup.getCheckedChipId();
        if(selectedChip==R.id.ChipAlbum){
            Log.d(TAG, "onStart: Chip: Album ");
            LoadAlbumView();
        }else if(selectedChip==R.id.ChipAllSongs){
            Log.d(TAG, "onStart: Chip: Song");
            LoadSongsView();
        }else if(selectedChip==R.id.ChipArtist){
            Log.d(TAG, "onStart: Chip: Artist");
            LoadArtistView();
        }else if(selectedChip==R.id.ChipGenre){
            Log.d(TAG, "onStart: Chip: Genre");
            LoadGenreView();
        }

    }


    List<Music> AlbumList;
    List<Music> SongsList;
    List<Music> GenereList;
    List<Music> ArtistList;

    MusicLibraryAdapter mAdapterAlbum;
    MusicLibraryAdapter mAdapterArtist;
    MusicLibraryAdapter mAdapterGenre;
    MusicLibraryAdapter mAdapterSong;

    void LoadMusic(){
        AlbumList = appDatabase.musicDAO().getAllAlbums();
        SongsList = appDatabase.musicDAO().getAll();
        GenereList= appDatabase.musicDAO().getAllGenre();
        ArtistList= appDatabase.musicDAO().getAllArtist();

        mAdapterAlbum = new MusicLibraryAdapter(AlbumList,constants.ADAPTER_MODE_ALBUM);
        mAdapterArtist= new MusicLibraryAdapter(ArtistList,constants.ADAPTER_MODE_ARTIST);
        mAdapterGenre= new MusicLibraryAdapter(GenereList,constants.ADAPTER_MODE_GENRE);
        mAdapterSong= new MusicLibraryAdapter(SongsList,constants.ADAPTER_MODE_SONGS);


    }

    void LoadAlbumView() {
        if(AlbumList.isEmpty()){
            LoadMusic();
        }
        // RecyclerView mRecyclerView;
        //mRecyclerView = mView.findViewById(R.id.SocietyCultureRecycleview);
        //mBinding.RecycleviewMain.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        //Collections.shuffle(list);
           mBinding.RecycleviewMain.setAdapter(mAdapterAlbum);
    }
    void LoadArtistView() {

        if(ArtistList.isEmpty()){
            LoadMusic();
        }
        // RecyclerView mRecyclerView;
        //mRecyclerView = mView.findViewById(R.id.SocietyCultureRecycleview);
        //mBinding.RecycleviewMain.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        //Collections.shuffle(list);
        mBinding.RecycleviewMain.setAdapter(mAdapterArtist);
    }
    void LoadGenreView() {

        if(GenereList.isEmpty()){
            LoadMusic();
        }
        // RecyclerView mRecyclerView;
        //mRecyclerView = mView.findViewById(R.id.SocietyCultureRecycleview);
        //mBinding.RecycleviewMain.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        //Collections.shuffle(list);
        mBinding.RecycleviewMain.setAdapter(mAdapterGenre);
    }
    void LoadSongsView() {

        if(SongsList.isEmpty()){
            LoadMusic();
        }
        // RecyclerView mRecyclerView;
        //mRecyclerView = mView.findViewById(R.id.SocietyCultureRecycleview);
        //mBinding.RecycleviewMain.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        //Collections.shuffle(list);
        mBinding.RecycleviewMain.setAdapter(mAdapterSong);
    }



    public class MusicLibraryAdapter extends RecyclerView.Adapter<MusicLibraryAdapter.ViewHolder> {
        public String TAG = "MusicLibraryAdapter";
        public List<Music> localDataSet;
        public String ADAPTER_MODE;
        public Constants constants = new Constants();

        public MusicLibraryAdapter(List<Music> dataSet, String MODE) {
            localDataSet = dataSet;
            ADAPTER_MODE = MODE;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MusicLibraryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recycleview_item_music_album, viewGroup, false);

            return new MusicLibraryAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(MusicLibraryAdapter.ViewHolder viewHolder, int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            try {

                String mTitle = "";
                String mSubTitle = "";
                if (ADAPTER_MODE.equals(constants.ADAPTER_MODE_ALBUM)) {
                    mTitle = localDataSet.get(position).Album_Title;
                    mSubTitle = localDataSet.get(position).Artist_Name;
                } else if (ADAPTER_MODE.equals(constants.ADAPTER_MODE_SONGS)) {
                    mTitle = localDataSet.get(position).Song_Title;
                    mSubTitle = localDataSet.get(position).Artist_Name;
                }else if(ADAPTER_MODE.equals(constants.ADAPTER_MODE_ARTIST) ){
                    mTitle = localDataSet.get(position).Artist_Name;
                    mSubTitle =null;
                }else if(ADAPTER_MODE.equals(constants.ADAPTER_MODE_GENRE)){
                    mTitle = localDataSet.get(position).Genre;
                    mSubTitle = null;
                }


                viewHolder.getMusicTitle().setText(mTitle);
                if(mSubTitle!=null){
                    viewHolder.getMusicArtist().setText(mSubTitle);
                }else{
                    viewHolder.getMusicArtist().setVisibility(View.GONE);
                }

                viewHolder.getLLItem().setTag(position);


try {
    viewHolder.getAlbumArtimageView().setImageBitmap(viewHolder.getAlbumArtwork(localDataSet.get(position).Album_ID));

}catch (Exception e){
    Log.e(TAG, "onBindViewHolder: "+e );
}




            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: #53645645 " + e);
            }


            viewHolder.getLLItem().setOnClickListener(view -> {
                String id = view.getTag().toString();
                Log.d(TAG, "onClick: music selected  " + id);
                //  String pid = localDataSet.get(Integer.parseInt(id)).Song_ID;
                Music mMusic=localDataSet.get(Integer.parseInt(id));
                if (viewHolder.getView().getContext() instanceof MainActivity) {

                    ((MainActivity) viewHolder.getView().getContext()).SelectItem(mMusic,ADAPTER_MODE);


                }
            });


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return localDataSet.size();
        }


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView MusicTitle;
            private final TextView MusicArtist;
            private final ImageView AlbumArtimageView;
            //private final CardView MusicCardView;
            private final LinearLayout LLItem;
            private final View mView;
            public String TAG = "MusicLibAdapter";

            public ViewHolder(View view) {
                super(view);
                mView = view;
                // Define click listener for the ViewHolder's View
  /*          view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "onClick: " + v.getTag().toString() + "\nID:" + v.getId());
                    Log.d(TAG, "onClick: ");

                    String id = v.getTag().toString();
                    Log.d(TAG, "onClick: " + id);


                    // Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");


                }
            });
*/

  /*          LLMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "onClick: " + v.getTag().toString() + "\nID:" + v.getId());
                    String id = v.getTag().toString();
                    Log.d(TAG, "onClick: LLMusic " + id);

                }
            });
            downloadIMG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "onClick: " + v.getTag().toString() + "\nID:" + v.getId());
                    String id = v.getTag().toString();
                    Log.d(TAG, "onClick: downloadIMG " + id);
                }
            });
            playIMG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d(TAG, "onClick: " + v.getTag().toString() + "\nID:" + v.getId());
                    String id = v.getTag().toString();
                    Log.d(TAG, "onClick: playIMG" + id);
                }
            });
*/

                MusicTitle = view.findViewById(R.id.NametextView);
                MusicArtist = view.findViewById(R.id.ArtistTextView2);
                AlbumArtimageView = view.findViewById(R.id.AlbumArtimageView);
                //   MusicCardView = view.findViewById(R.id.MusicCardView);
                LLItem = view.findViewById(R.id.LLItem);


            }

            public TextView getMusicTitle() {
                return MusicTitle;
            }

            public TextView getMusicArtist() {
                return MusicArtist;
            }

            public ImageView getAlbumArtimageView() {
                return AlbumArtimageView;
            }

            public LinearLayout getLLItem() {
                return LLItem;
            }


            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            private Bitmap getAlbumArtwork(String album_Id) throws IOException {

                long albumId = Long.parseLong(album_Id);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        albumId
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return mView.getContext().getApplicationContext().getContentResolver().loadThumbnail(contentUri, new Size(640, 480), null);
                } else {
                    return null;
                }
            }

            public View getView() {
                return mView;
            }

        }


    }

    
}