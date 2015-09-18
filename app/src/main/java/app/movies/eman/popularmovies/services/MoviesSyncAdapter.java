package app.movies.eman.popularmovies.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import app.movies.eman.popularmovies.MoviesAdapter;
import app.movies.eman.popularmovies.R;
import app.movies.eman.popularmovies.data.MoviesContract;

/**
 * Created by user on 29/07/2015.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {


    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;


    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        String apiKey ="ce754b8d51f322f0c4dea3f43e34a771";
        String sort = MoviesAdapter.getSortBy(getContext());

        try {
            // Construct the URL for the api.themoviedb.org query

            final String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String KEY_PARAM = "api_key";

            //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sort)
                    .appendQueryParameter(KEY_PARAM, apiKey)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                // return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.

            }

            moviesJsonStr = buffer.toString();
            getMoviesDataFromJson(moviesJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        //     return;

        return ;
    }



    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        final String OWM_RESULT = "results";

        final String OWM_OVERVIEW = "overview";
        final String OWM_RELEASE_DATA = "release_date";
        final String OWM_TITLE = "title";
        final String OWM_VOTE_AVERAGE = "vote_average";
        final String OWM_POSTER_PATH = "poster_path";
        final String OWM_MOVIE_ID = "id";

        try {


            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULT);

            String[] resultStr = new String[moviesArray.length()];
            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

            for(int i = 0; i < moviesArray.length(); i++){
                String overview;
                String releaseDate;
                String title;
                String image;
                String voteAverage;
                int movieID;

                JSONObject fullMovie = moviesArray.getJSONObject(i);
                overview = fullMovie.getString(OWM_OVERVIEW);
                releaseDate = fullMovie.getString(OWM_RELEASE_DATA);
                title = fullMovie.getString(OWM_TITLE);
                image = fullMovie.getString(OWM_POSTER_PATH);
                voteAverage = fullMovie.getString(OWM_VOTE_AVERAGE);
                movieID = fullMovie.getInt(OWM_MOVIE_ID);

                ContentValues movieValues = new ContentValues();

                getContext().getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                        MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH + " = ?",
                        new String[]{image});

                movieValues.put( MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH, image);
                movieValues.put( MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME, title);
                movieValues.put( MoviesContract.MoviesEntry.COLUMN_MOVIE_OVERVIEW, overview);
                movieValues.put( MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put( MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put( MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieID);


                getContext().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, movieValues);

                cVVector.add(movieValues);
            }






            Log.d(LOG_TAG, "FetchMovie Complete. " + cVVector.size() + " Inserted");


           /* for (String s : resultStr) {
                 Log.v(LOG_TAG, "Movie entry: " + s);
            }*/

        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }



    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
