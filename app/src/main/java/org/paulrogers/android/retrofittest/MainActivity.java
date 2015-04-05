package org.paulrogers.android.retrofittest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Streaming;
import retrofit.mime.TypedInput;


public class MainActivity extends ActionBarActivity {

    private final static String TAG = "MainActivity";

    EditText mUrlText;
    String mEndpoint;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUrlText = (EditText) findViewById(R.id.url_text);

        Button urlButton = (Button) findViewById(R.id.fetch_button);
        urlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEndpoint = mUrlText.getText().toString();
                Log.d(TAG, "Got URL: " + mEndpoint);
                testRetroCall();
            }
        });

        mImageView = (ImageView) findViewById(R.id.imageView);

    }



    private void testRetroCall( ){
        new Thread( new Runnable() {
            @Override
            public void run() {
                InputStream in = null;

                try {
                    RestAdapter restAdapter;
                    Log.d(TAG, "Creating RestAdapter.Builder(), setting endpoint & calling build");
                    restAdapter = new RestAdapter.Builder().setEndpoint(mEndpoint)
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
                    final Bitmap bitmap = Bitmap.createBitmap(source, 0, 0,
                                                        source.getWidth(),
                                                        source.getHeight(), matrix, true);


                    // Post a runnable with the bitmap
                    mImageView.post(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setImageBitmap(bitmap);
                        }
                    });

                }
                catch (Exception e) {
                    final String msg = e.getMessage();
                    MainActivity.this.runOnUiThread( new Runnable() {
                                                         @Override
                                                         public void run() {
                                         Toast t = Toast.makeText(MainActivity.this,
                                                 "Failed: " + msg,
                                                 Toast.LENGTH_LONG);
                                         t.show();
                                                         } });
                    Log.e(TAG, "Failed: " + e.getMessage(),e);
                }
                finally {
                    try { in.close(); } catch (Exception ignore){}
                }
        }
        }).start();

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    interface DoorImageService {
        // http://71.236.0.176/door/camera?foo=bar
        // asynchronously with a callback
        @GET("/static/garage/image.jpg")
        @Streaming
        Response getImage() throws DoorImageServiceException;

    }



    class DoorImageServiceException extends Exception {
        @Override
        public String getMessage() {return "request timed out"; };
    }

}


