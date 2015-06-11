package org.paulrogers.android.retrofittest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity implements DownloadFragment.DownloadResultsListener {

    private final static String TAG = "MainActivity";

    private static final String DOWNLOAD_FRAGMENT_TAG = "download-fragment";

    private static final String IS_DOWNLOADING_TAG = "is-downloading";


    private boolean mDownloading = false;

    private EditText mUrlText;
    private String mEndpoint;
    private ImageView mImageView;
    private Button mFetchButton;
    private ImageProxy mImageProxy;
    private DownloadFragment mDownloadFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        mUrlText = (EditText) findViewById(R.id.url_text);

        mFetchButton = (Button) findViewById(R.id.fetch_button);
        mFetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doImageDownload();
            }
        });

        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageProxy = ImageProxy.getInstance(getApplicationContext());
        if ( mImageProxy.getBitmap() != null ) {
            mImageView.setImageBitmap( mImageProxy.getBitmap());
        }

        loadDownloadFragment();
    }

    /**
     * Request image download
     */
    private void doImageDownload() {
        hideKeyboard(this, mUrlText.getWindowToken());
        mEndpoint = mUrlText.getText().toString();
        Log.d(TAG, "Downloading " + mEndpoint);
        Toast.makeText(MainActivity.this, "Starting to download", Toast.LENGTH_SHORT).show();
        mDownloading = true;
        mFetchButton.setEnabled( false );
        mDownloadFragment.requestDownload(mUrlText.getText().toString());
    }

    /**
     * This method is used to hide a keyboard after a user has
     * finished typing the url.
     */
    public void hideKeyboard(Activity activity,
                             IBinder windowToken) {
        InputMethodManager mgr =
                (InputMethodManager) activity.getSystemService
                        (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }



    /**
     * Load the fragment that will handle download requsts
     */
    private void loadDownloadFragment() {
        if( mDownloadFragment == null ) {
            Log.d(TAG, "Fragment is null, try to get from frag mgr");
            FragmentManager fm = getSupportFragmentManager();
            mDownloadFragment = (DownloadFragment) fm.findFragmentByTag(DOWNLOAD_FRAGMENT_TAG);
            if (mDownloadFragment == null) {
                Log.d(TAG, "Fragment is null, creating new one");
                mDownloadFragment = new DownloadFragment();

                fm.beginTransaction().add(mDownloadFragment, DOWNLOAD_FRAGMENT_TAG).commit();
            }
        }
    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if( savedInstanceState != null ) {
            mDownloading = savedInstanceState.getBoolean(IS_DOWNLOADING_TAG);
            Log.d(TAG, "On restore instance state - downloading: " + mDownloading);
            if( mDownloading ) {
                mFetchButton.setEnabled( false );
            }
            else {
                mFetchButton.setEnabled( true );
            }
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "On save instance state - downloading: " + mDownloading);
        outState.putBoolean(IS_DOWNLOADING_TAG, mDownloading);
    }


    @Override
    public void setResults(DownloadFragment.DownloadResults downloadResults) {
        mDownloading = false;
        mFetchButton.setEnabled( true );
        if( !downloadResults.isSuccess() ) {
            Toast.makeText(MainActivity.this,
                    "Failed: " + downloadResults.getErrorMessage(),
                    Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Download completed", Toast.LENGTH_SHORT).show();
        Bitmap bm = downloadResults.getBitmap();
        if( bm == null ) {
            Toast.makeText(this, "Download failed - no image returned!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mImageView.setImageBitmap( bm );
        mImageProxy.setBitmap( bm );
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

}


