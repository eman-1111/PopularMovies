package app.movies.eman.popularmovies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.GridView;

import app.movies.eman.popularmovies.data.MoviesContract;


/**
 * Created by user on 29/07/2015.
 */

public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    MoviesAdapter adapter;
    GridView gridView;
    public static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";
    private static final int MOVIE_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL__ID = 0;
    public static final int COL_IMAGE_PATH = 1;
    public static final int COL_MOVIE_ID = 2;


    public MoviesFragment() {
    }


    public interface Callback {
        /**
         * MovieDetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        adapter = new MoviesAdapter(getActivity(), null, 0);

        gridView = (GridView) rootView.findViewById(R.id.movie_image_gridview);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int movieId = cursor.getInt(COL_MOVIE_ID);
                    ((Callback) getActivity())
                            .onItemSelected(MoviesContract.MoviesEntry.buildMovieURL(movieId));


                }
                mPosition = position;
            }

        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The GridView probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to gridView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != gridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onSortByChange() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.


        Uri MoviesUri = MoviesContract.MoviesEntry.buildMoviesURL();
        String sortOrder;
        String selection;
        String[] selectionArgs;

        if (MoviesAdapter.getSortBy(getActivity()).equals("vote_average.desc")) {
            sortOrder = MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " DESC";
            selection = null;
            selectionArgs = null;

        } else if (MoviesAdapter.getSortBy(getActivity()).equals("favorite_movie.desc")) {
            sortOrder = MoviesContract.MoviesEntry.COLUMN_FAVORITE + " DESC";
             selection = MoviesContract.MoviesEntry.TABLE_NAME +
                     "." + MoviesContract.MoviesEntry.COLUMN_FAVORITE + " = ? ";
             selectionArgs = new String[]{Integer.toString(1)};

        } else {
            sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC";
            selection = null;
            selectionArgs = null;

        }


        return new CursorLoader(getActivity(),
                MoviesUri,
                DETAIL_COLUMNS,
                selection,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {

            gridView.smoothScrollToPosition(mPosition);
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}