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
import java.util.ArrayList;
import java.util.Vector;

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
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    ArrayList<Integer> moviesID = new ArrayList<Integer>();

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
        String videoJsonStr = null;
        String reviewJsonStr = null;


        String apiKey = "####";
        String sort = "popularity.desc";

        try {
            // Construct the URL for the api.themoviedb.org query

            final String MOVES_BASE_URL =
                    "http://api.themoviedb.org/3/movie/top_rated?";
            final String SORT_PARAM = "sort_by";
            final String KEY_PARAM = "api_key";

            //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
            Uri builtUri = Uri.parse(MOVES_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sort)
                    .appendQueryParameter(KEY_PARAM, apiKey)
                    .build();

            Log.e("url", builtUri + "");
            URL url = new URL(builtUri.toString());

            // Create the request to themoviedb, and open the connection
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

                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.

            }

            moviesJsonStr = buffer.toString();
            ArrayList<Integer> moviesID = getMoviesDataFromJson(moviesJsonStr);

            for (int i = 0; i < moviesID.size(); i++) {


                //https://api.themoviedb.org/3/movie/211672/videos?api_key=[YOUR API KEY]
                final String VIDEO_BASE_URL =
                        "https://api.themoviedb.org/3/movie/" + moviesID.get(i) + "/videos?";

                Uri builtVideoUrl = Uri.parse(VIDEO_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, apiKey)
                        .build();

                URL videoUrl = new URL(builtVideoUrl.toString());
                //Log.d(LOG_TAG, builtVideoUrl.toString());

                urlConnection = (HttpURLConnection) videoUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream videoInputStream = urlConnection.getInputStream();
                StringBuffer videoBuffer = new StringBuffer();
                if (videoInputStream == null) {
                    // Nothing to do.
                    // return;
                }
                reader = new BufferedReader(new InputStreamReader(videoInputStream));

                String videoLine;
                while ((videoLine = reader.readLine()) != null) {

                    videoBuffer.append(videoLine + "\n");
                }

                if (videoBuffer.length() == 0) {
                    // Stream was empty.  No point in parsing.

                }
                videoJsonStr = videoBuffer.toString();
                getVideoFormJson(videoJsonStr, moviesID.get(i));

                ///////////////////////////////////////////////////////////////////

                //https://api.themoviedb.org/3/movie/211672/reviews?api_key=[YOUR API KEY]
                final String REVIEW_BASE_URL =
                        "https://api.themoviedb.org/3/movie/" + moviesID.get(i) + "/reviews?";
                Uri builtReviewUrl = Uri.parse(REVIEW_BASE_URL).buildUpon()
                        .appendQueryParameter(KEY_PARAM, apiKey)
                        .build();


                URL reviewUrl = new URL(builtReviewUrl.toString());


                urlConnection = (HttpURLConnection) reviewUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream reviewInputStream = urlConnection.getInputStream();
                StringBuffer reviewBuffer = new StringBuffer();
                if (reviewInputStream == null) {
                    // Nothing to do.
                    // return;
                }
                reader = new BufferedReader(new InputStreamReader(reviewInputStream));

                String reviewLine;
                while ((reviewLine = reader.readLine()) != null) {

                    reviewBuffer.append(reviewLine + "\n");
                }

                if (reviewBuffer.length() == 0) {
                    // Stream was empty.  No point in parsing.

                }
                reviewJsonStr = reviewBuffer.toString();
                getReviewFromJson(reviewJsonStr, moviesID.get(i));

            }
            Log.d(LOG_TAG, "Fetch is Complete. " + moviesID.size() + " Inserted");


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

        return;
    }

    private void getReviewFromJson(String reviewJsonStr, int movieId)
            throws JSONException {

        final String OWN_RESULT = "results";

        final String OWN_REVIEW_ID = "id";
        final String OWN_AUTHOR = "author";
        final String OWN_REVIEW = "content";
        try {
            JSONObject reviewJSON = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJSON.getJSONArray(OWN_RESULT);

            // Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewArray.length());
            if (reviewArray.length() > 0) {
                for (int i = 0; i < 1; i++) {

                    String review;
                    String author;
                    String reviewId;

                    JSONObject fullReview = reviewArray.getJSONObject(i);
                    review = fullReview.getString(OWN_REVIEW);
                    author = fullReview.getString(OWN_AUTHOR);
                    reviewId = fullReview.getString(OWN_REVIEW_ID);

                    ContentValues reviewValue = new ContentValues();


                    reviewValue.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
                    reviewValue.put(MoviesContract.ReviewEntry.COLUMN_REVIEW, review);
                    reviewValue.put(MoviesContract.ReviewEntry.COLUMN_REVIEW_ID, reviewId);
                    reviewValue.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);

                    getContext().getContentResolver().delete(MoviesContract.ReviewEntry.CONTENT_URI,
                            MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{Integer.toString(movieId)});

                    getContext().getContentResolver().insert(MoviesContract.ReviewEntry.CONTENT_URI, reviewValue);


                }
            } else {
                ContentValues reviewValue = new ContentValues();

                reviewValue.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, ".");
                reviewValue.put(MoviesContract.ReviewEntry.COLUMN_REVIEW, "Not Available");
                reviewValue.put(MoviesContract.ReviewEntry.COLUMN_REVIEW_ID, "Not Available");
                reviewValue.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);

                getContext().getContentResolver().delete(MoviesContract.ReviewEntry.CONTENT_URI,
                        MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{Integer.toString(movieId)});

                getContext().getContentResolver().insert(MoviesContract.ReviewEntry.CONTENT_URI, reviewValue);


            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


    }

    private void getVideoFormJson(String videoJsonStr, int movieId)
            throws JSONException {

        final String OWN_RESULT = "results";

        final String OWN_VIDEO_ID = "id";
        final String OWN_KEY = "key";
        final String OWN_NAME = "name";
        try {
            JSONObject videoJSON = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJSON.getJSONArray(OWN_RESULT);

            if (videoArray.length() > 0) {
                for (int i = 0; i < 1; i++) {

                    String key;
                    String name;
                    String videoId;

                    JSONObject fullVideo = videoArray.getJSONObject(i);
                    key = fullVideo.getString(OWN_KEY);
                    name = fullVideo.getString(OWN_NAME);
                    videoId = fullVideo.getString(OWN_VIDEO_ID);

                    ContentValues videoValue = new ContentValues();


                    videoValue.put(MoviesContract.VideoEntry.COLUMN_ADDRESS, key);
                    videoValue.put(MoviesContract.VideoEntry.COLUMN_MOVIE_NAME, name);
                    videoValue.put(MoviesContract.VideoEntry.COLUMN_VIDEO_ID, videoId);
                    videoValue.put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, movieId);

                    getContext().getContentResolver().delete(MoviesContract.VideoEntry.CONTENT_URI,
                            MoviesContract.VideoEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{Integer.toString(movieId)});

                    getContext().getContentResolver().insert(MoviesContract.VideoEntry.CONTENT_URI, videoValue);


                }
            } else {
                ContentValues videoValue = new ContentValues();


                videoValue.put(MoviesContract.VideoEntry.COLUMN_ADDRESS, "Not Available");
                videoValue.put(MoviesContract.VideoEntry.COLUMN_MOVIE_NAME, "Not Available");
                videoValue.put(MoviesContract.VideoEntry.COLUMN_VIDEO_ID, "Not Available");
                videoValue.put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, movieId);

                getContext().getContentResolver().delete(MoviesContract.VideoEntry.CONTENT_URI,
                        MoviesContract.VideoEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{Integer.toString(movieId)});

                getContext().getContentResolver().insert(MoviesContract.VideoEntry.CONTENT_URI, videoValue);

            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


    }

    private ArrayList<Integer> getMoviesDataFromJson(String moviesJsonStr)
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

            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

            for (int i = 0; i < moviesArray.length(); i++) {
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


                movieValues.put(MoviesContract.MoviesEntry.COLUMN_IMAGE_PATH, image);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME, title);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_OVERVIEW, overview);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieID);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITE, 0);


                if (!isOldMovie(moviesID, movieID)) {

                    moviesID.add(movieID);

                    cVVector.add(movieValues);
                }


            }

            if (cVVector.size() > 0) {

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);

            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return moviesID;

    }

    private boolean isOldMovie(ArrayList<Integer> moviesID, int movieID) {
        boolean found = false;
        for (int i = 0; i < moviesID.size(); i++) {
            if (moviesID.get(i) == movieID) {
                found = true;
            }

        }
        return found;
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
     *
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
        if (null == accountManager.getPassword(newAccount)) {

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