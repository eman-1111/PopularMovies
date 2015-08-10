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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import app.movies.eman.popularmovies.data.MoviesContract;

/**
 * Created by user on 02/08/2015.
 */

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private Uri mUri;
    static final String DETAIL_URI = "URI";
    String baseURL = "http://image.tmdb.org/t/p/w185/";
    private static final String[] DETAIL_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_OVERVIEW,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE
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
         }

         View rootView = inflater.inflate(R.layout.fragment_movies_detail, container, false);
         mMovieImage = (ImageView) rootView.findViewById(R.id.detail_icon);

         mMovieTitle = (TextView) rootView.findViewById(R.id.title_textview);
         mReleaseYear = (TextView) rootView.findViewById(R.id.release_year_textview);
         mVoteAverage = (TextView) rootView.findViewById(R.id.vote_average_textview);
         mMovieOverview = (TextView) rootView.findViewById(R.id.movie_overview_textview);

         return rootView;
     }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        if ( null != mUri) {
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


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
