package org.paulrogers.android.retrofittest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Streaming;
import retrofit.mime.TypedInput;

/**
 * Created by paulrogers on 4/27/15.
 */
public class DownloadFragment extends Fragment implements DownloadRequestHandler {

    private static final String TAG = "DownloadFragment";

    WeakReference<DownloadResultsListener> mDownloadResultsListener;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "Attaching to activity");

        if ( activity instanceof DownloadResultsListener ) {
            mDownloadResultsListener = new WeakReference<DownloadResultsListener>( (DownloadResultsListener)activity );
        }
        else {
            throw new ClassCastException("Received Activity does not implement DownloadResultsListener");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called");
        mDownloadResultsListener = null;
        super.onDestroy();
    }




    /**
     * Callback for Aysnych task passes results to listener
     * @param results
     */
    private void downloadCompleted( DownloadResults results) {
        DownloadResultsListener listener = mDownloadResultsListener.get();
        if( listener != null ) {
            listener.setResults(results);

        }
        else {
            Log.w(TAG, "Listener no longer present!");
        }
    }


    /**
     * Download
     */
    class DownloadTask extends AsyncTask<String, Void, DownloadResults> {
        String mErrorMsg = null;
        Boolean mSuccess = false;

        @Override
        protected DownloadResults doInBackground(String... strings) {

            Bitmap bitmap = null;
            InputStream in = null;
            try {
                String endpoint = strings[0];
                RestAdapter restAdapter;
                Log.d(TAG, "Creating RestAdapter.Builder(), setting endpoint & calling build");
                restAdapter = new RestAdapter.Builder().setEndpoint(endpoint)
                        .setErrorHandler(new ErrorHandler() {
                            @Override
                            public Throwable handleError(RetrofitError cause) {
                                String message = "Failed due to " + cause.getMessage();
                                Log.e(TAG, message);
                                return new DoorImageServiceException();
                            }
                        })
                        .build();

                DoorImageService service = restAdapter.create(DoorImageService.class);
                restAdapter.setLogLevel(RestAdapter.LogLevel.BASIC);

                Response response = service.getImage();

                TypedInput responseBody = response.getBody();
                in = responseBody.in();
                byte[] data = new byte[1024];
                int len = 0;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while ((len = in.read(data)) != -1) {
                    bos.write(data, 0, len);
                }
                byte[] imageBytes = bos.toByteArray();
                Bitmap source = BitmapFactory.decodeByteArray(imageBytes, 0,
                        imageBytes.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(180.0F);
                bitmap = Bitmap.createBitmap(source, 0, 0,
                        source.getWidth(),
                        source.getHeight(), matrix, true);
                // Done!
                return new DownloadResults(bitmap, true, null);

            }
            catch (Exception e) {
                final String msg = e.getMessage();
                Log.e(TAG, "Failed: " + msg,e);
                return new DownloadResults(null, false, msg);
            }
            finally {
                try { in.close(); } catch (Exception ignore){}
            }


        }

        @Override
        protected void onPostExecute(DownloadResults results) {
            super.onPostExecute(results);
            Log.d(TAG, "Download completed: " + results.mSuccess);
            DownloadFragment.this.downloadCompleted( results );

        }
    }



    class DownloadResults {
        private Bitmap mBitmap;
        private boolean mSuccess = false;
        private String mErrorMessage = "";
        DownloadResults(Bitmap bitmap, boolean success, String errorMessage) {
            mBitmap = bitmap;
            mSuccess = success;
            mErrorMessage = errorMessage;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public boolean isSuccess() {
            return mSuccess;
        }

        public String getErrorMessage() {
            return mErrorMessage;
        }
    }

    @Override
    public void requestDownload(String imageUrl) {
        Log.d(TAG, "Got image request for " + imageUrl);
        DownloadTask task = new DownloadTask();
        String[] urls = new String[1];
        urls[0] = imageUrl;
        task.execute(urls);
    }

    /**
     * The image callback interface for interested Activities
     * to receive results
     */
    interface DownloadResultsListener {

        void setResults(DownloadResults downloadResults);

    }




    /**
     * The RetroFit service definition
     */
    interface DoorImageService {
        // http://71.236.0.176/door/camera?foo=bar
        // asynchronously with a callback
        @GET("/static/garage/image.jpg")
        @Streaming
        Response getImage() throws DoorImageServiceException;

    }


    /**
     * RetroFit Service Exception definition
     */
    class DoorImageServiceException extends Exception {
        @Override
        public String getMessage() {return "request timed out"; };
    }

}

