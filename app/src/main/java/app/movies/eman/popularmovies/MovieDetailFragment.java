package app.movies.eman.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import app.movies.eman.popularmovies.data.MoviesContract;

/**
 * Created by user on 02/08/2015.
 */

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //movie
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private Uri mUri;
    static final String DETAIL_URI = "URI";
    String baseURL = "http://image.tmdb.org/t/p/w185/";

    //review
    private Uri mUriReview;
    ReviewAdapter reviewAdapter;
    ListView reviewListView;
    Loader<Cursor> reviewCursor;


    //video
    private Uri mUriVideo;
    VideoAdapter videoAdapter;
    ListView videoListView;
    Loader<Cursor> videoCursor;


    private static final String[] DETAIL_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE
    };

    private static final String[] DETAIL_COLUMNS_REVIEW= {
            MoviesContract.ReviewEntry.TABLE_NAME + "." + MoviesContract.ReviewEntry._ID,
            MoviesContract.ReviewEntry.COLUMN_REVIEW,
            MoviesContract.ReviewEntry.COLUMN_AUTHOR

    };

    private static final String[] DETAIL_COLUMNS_VIDEO = {
            MoviesContract.VideoEntry.TABLE_NAME + "." + MoviesContract.VideoEntry._ID,
            MoviesContract.VideoEntry.COLUMN_ADDRESS

    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_MOVIE_ID = 0;
    public static final int COL_IMAGE_PATH = 1;
    public static final int COL_MOVIE_NAME = 2;
    public static final int COL_MOVIE_OVERVIEW = 3;
    public static final int COL_VOTE_AVERAGE = 4;
    public static final int COL_MOVIES_ID = 5;
    public static final int COL_RELEASE_DATE = 6;

    public static final int COL_REVIEW_ID = 0;
    public static final int COL_REVIEW = 1;
    public static final int COL_AUTHOR = 2;

    public static final int COL_VIDEO_ID = 0;
    public static final int COL_ADDRESS = 1;



    private ImageView mMovieImage;
    private TextView mMovieTitle;
    private TextView mReleaseYear;
    private TextView mVoteAverage;
    private TextView mMovieOverview;


     public MovieDetailFragment() {
     }



    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
         Bundle arguments = getArguments();
         if (arguments != null) {
             mUri = arguments.getParcelable(MovieDetailFragment.DETAIL_URI);
             int movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(mUri);
             mUriVideo = MoviesContract.VideoEntry.buildVideoURL(movieId);
             mUriReview = MoviesContract.ReviewEntry.buildReviewURL(movieId);
         }



         View rootView = inflater.inflate(R.layout.fragment_movies_detail, container, false);
        //movie
         mMovieImage = (ImageView) rootView.findViewById(R.id.detail_icon);
         mMovieTitle = (TextView) rootView.findViewById(R.id.title_textview);
         mReleaseYear = (TextView) rootView.findViewById(R.id.release_year_textview);
         mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average_textview);
         mMovieOverview = (TextView) rootView.findViewById(R.id.movie_overview_textview);

        //review
        reviewAdapter = new ReviewAdapter(getActivity(), null, 0);

        reviewListView = (ListView) rootView.findViewById(R.id.review_list);
        reviewListView.setAdapter(reviewAdapter);


        //video
        videoAdapter = new VideoAdapter(getActivity(), null, 0);
        videoListView = (ListView) rootView.findViewById(R.id.video_play_list);
        videoListView.setAdapter(videoAdapter);

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            }

        });

         return rootView;
     }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if(mUriVideo != null){

            reviewCursor = new CursorLoader(getActivity(),
                    mUriVideo,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);

            return null;
        }

        if(mUriReview != null){

            videoCursor = new CursorLoader(getActivity(),
                    mUriReview,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null);

            return null;
        }


        if (mUri != null) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader( getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            String movieTitle = data.getString(COL_MOVIE_NAME);
            mMovieTitle.setText(movieTitle);

            String image = data.getString(COL_IMAGE_PATH);
            String imageURL = baseURL + image;
            Picasso.with(getActivity()).load(imageURL).into(mMovieImage);

            String movieDate = data.getString(COL_RELEASE_DATE);
            mReleaseYear.setText(movieDate.substring(0, 4));


            String voteAverage = data.getString(COL_VOTE_AVERAGE);
            mVoteAverage.setText(voteAverage + "/10");






            String movieOverview = data.getString(COL_MOVIE_OVERVIEW);
            mMovieOverview.setText(movieOverview);

            //review
            reviewAdapter.swapCursor((Cursor) reviewCursor);

            //video
            videoAdapter.swapCursor((Cursor) videoCursor);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        //review
        reviewAdapter.swapCursor(null);

        //video
        videoAdapter.swapCursor(null);

    }
}
