package org.paulrogers.android.retrofittest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit.RestAdapter;
import retrofit.http.GET;


public class MainActivity extends ActionBarActivity {

    private final static String TAG = "MainActivity";

    EditText mUrlText;
    String mEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUrlText = (EditText) findViewById(R.id.url_text);

        Button urlButton = (Button) findViewById(R.id.fetch_button);
        urlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Click!");
                mEndpoint = mUrlText.getText().toString();
                Log.d(TAG, "Got URL: " + mEndpoint);
                testRetroCall();
            }
        });






    }

    private void testRetroCall() {
        try {
            RestAdapter restAdapter;
            restAdapter = new RestAdapter.Builder().setEndpoint(mEndpoint).build();

            DoorImageService service = restAdapter.create(DoorImageService.class);
            restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
            Object image = service.getImage();
        }
        catch (Exception e) {
            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


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
        @GET("/door/camera")
        Object getImage();


    }
}


