package app.movies.eman.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import app.movies.eman.popularmovies.services.MoviesSyncAdapter;


public class MainActivity extends ActionBarActivity implements MoviesFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;
    String mSortBy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSortBy = MoviesAdapter.getSortBy(this);

        if (findViewById(R.id.movie_detail_container) != null) {

            mTwoPane = true;

            Log.d(LOG_TAG, "Tablet.");
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            Log.d(LOG_TAG, "Phone");
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);

        }
        MoviesSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, contentUri);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MoviesDetail.class)
                    .setData(contentUri);
            startActivity(intent);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortBy = MoviesAdapter.getSortBy(this);
        if(sortBy != null && !sortBy.equals(mSortBy)){
            MoviesFragment mf = (MoviesFragment)getSupportFragmentManager().findFragmentById(R.id.movie_fragment);
            if(mf != null){
                mf.onSortByChange();
            }
        }
        mSortBy = sortBy;

    }


}

