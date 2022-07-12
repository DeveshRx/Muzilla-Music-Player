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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.IOException;
import java.util.List;

import devesh.app.Constants;
import devesh.app.MainActivity;
import devesh.app.R;
import devesh.app.common.EpochLib;
import devesh.app.common.tools.AlbumArtFromId;
import devesh.app.database.AppDatabase;
import devesh.app.database.MusicMain.Music;
import devesh.app.databinding.FragmentArtistUiBinding;


public class ArtistDetailsFragment extends Fragment {
    static String TAG = "ArtistDetailsFragment: ";
    FragmentArtistUiBinding mBinding;
    AppDatabase appDatabase;
    Constants constants = new Constants();

    String selectedArtist;

    Music mMusic = new Music();
    List<Music> AlbumSongs;

    AlbumSongListAdapter mAdapter;
    AlbumArtFromId albumArtFromId=new AlbumArtFromId();

    public ArtistDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appDatabase = Room
                .databaseBuilder(getActivity(), AppDatabase.class, getString(R.string.DATABASE_NAME))
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();


    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentArtistUiBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        selectedArtist = requireArguments().getString(constants.ADAPTER_MODE_ARTIST);


        Log.d(TAG, "onCreate: selectedArtist " + selectedArtist);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
    @Override
    public void onStart() {
        super.onStart();
        AlbumSongs = appDatabase.musicDAO().getMusicAlbumsByArtist(selectedArtist);

        if (!AlbumSongs.isEmpty()) {
            LoadDetails();

Music music=AlbumSongs.get(getRandomNo(0,AlbumSongs.size()-1));
mBinding.ArtistNameTextview.setText(music.Artist_Name);



            try {
                mBinding.ArtistPhotoImageView.setImageBitmap(albumArtFromId.getAlbumArtwork(getContext(),music.Album_ID));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

int getRandomNo(int min,int max){
    //int min = 50;
    //int max = 100;
if(max<0){
    max=0;
}
    //Generate random int value from 50 to 100
   // System.out.println("Random value in int from "+min+" to "+max+ ":");
    int random_int = (int)Math.floor(Math.random()*(max-min+1)+min);

    return random_int;
}
    void LoadDetails() {
        mAdapter = new AlbumSongListAdapter(AlbumSongs);
        mBinding.AlbumsRecycleview.setAdapter(mAdapter);


        //  mBinding.AlbumArtImageView.setImageBitmap(getAlbumArtwork(AlbumSongs.get(0).Album_ID));


        //mBinding.MainRelativeLayout.setBackground();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Bitmap getAlbumArtwork(String album_Id) throws IOException {

        long albumId = Long.parseLong(album_Id);
        Uri contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumId
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getContext().getApplicationContext().getContentResolver().loadThumbnail(contentUri, new Size(640, 480), null);
        } else {
            return null;
        }
    }

    class AlbumSongListAdapter extends RecyclerView.Adapter<AlbumSongListAdapter.ViewHolder> {
        public String TAG = "MusicLibraryAdapter";
        public List<Music> localDataSet;
        Constants constants = new Constants();
        EpochLib epochLib = new EpochLib();

        public AlbumSongListAdapter(List<Music> dataSet) {
            localDataSet = dataSet;

        }

        // Create new views (invoked by the layout manager)
        @Override
        public AlbumSongListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recycleview_item_music_album, viewGroup, false);

            return new AlbumSongListAdapter.ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(AlbumSongListAdapter.ViewHolder viewHolder, int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            try {

                String mTitle = "";
                String mSubTitle = "";

                mTitle = localDataSet.get(position).Album_Title;


                long duration = localDataSet.get(position).Duration;
                String t1sec = String.valueOf(epochLib.convert2Seconds(duration));
                if (t1sec.length() > 2) {
                    if (t1sec.length() == 3) {
                        StringBuffer sb = new StringBuffer(t1sec);
                        sb.deleteCharAt(sb.length() - 1);
                        t1sec = sb.toString();
                    }


                }
                String timeFormatted = epochLib.convert2Minutes(duration) + ":" + t1sec;

                mSubTitle = timeFormatted;


                viewHolder.getMusicTitle().setText(mTitle);

                viewHolder.getMusicArtist().setText(mSubTitle);


                viewHolder.getLLItem().setTag(position);


                viewHolder.getAlbumArt().setImageBitmap(getAlbumArtwork(localDataSet.get(position).Album_ID));



            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder: #53645645 " + e);
            }

            viewHolder.getLLItem().setOnClickListener(view -> {
                String id = view.getTag().toString();
                Log.d(TAG, "onClick: music selected  " + id);
                //  String pid = localDataSet.get(Integer.parseInt(id)).Song_ID;
                Music mMusic = localDataSet.get(Integer.parseInt(id));
                if (viewHolder.getView().getContext() instanceof MainActivity) {
                    ((MainActivity) viewHolder.getView().getContext()).SelectItem(mMusic, constants.ADAPTER_MODE_ALBUM);
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

            //private final CardView MusicCardView;
            private final LinearLayout LLItem;
            private final View mView;
            private final ImageView AlbumArt;
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
                AlbumArt = view.findViewById(R.id.AlbumArtimageView);
                //   MusicCardView = view.findViewById(R.id.MusicCardView);
                LLItem = view.findViewById(R.id.LLItem);


            }

            public TextView getMusicTitle() {
                return MusicTitle;
            }

            public TextView getMusicArtist() {
                return MusicArtist;
            }


            public LinearLayout getLLItem() {
                return LLItem;
            }

            public ImageView getAlbumArt() {
                return AlbumArt;
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