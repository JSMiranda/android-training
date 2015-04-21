package com.example.miranda.lab4_ex1;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {
    private Button mButton;
    private TextView mTextView;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        mButton = (Button) findViewById(R.id.button);
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

    public void downloadOnClick(View v) {
        counter++;
        Log.d(this.getLocalClassName(), "Button was clicked " + counter + " times.");
        if(counter>1) return;

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //mButton.setVisibility(View.GONE);
            mButton.setEnabled(false);
            //mButton.setClickable(false);
            mTextView.setText("Getting html file...");
            // if we use simple http, we will need to handle redirect status code
            new DownloadWebpageTask().execute("https://www.google.com/");
        } else {
            mTextView.setText("No network connection available.");
        }
    }


    /**
     * AsyncTask to fetch the data in the background away from the UI thread
     */
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        private HttpURLConnection mConnection;

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.setReadTimeout(10000 /* milliseconds */);
                mConnection.setConnectTimeout(15000 /* milliseconds */);
                mConnection.setRequestMethod("GET");
                mConnection.setDoInput(true);

                mConnection.connect();
                int statusCode = mConnection.getResponseCode();
                if (statusCode != HttpURLConnection.HTTP_OK) {
                    return "Error: Failed getting update notes";
                }

                return readTextFromServer(mConnection);

            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
        }

        private String readTextFromServer(HttpURLConnection connection) throws IOException {
            InputStreamReader stream = null;
            try {
                stream = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(stream);
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = br.readLine();
                }
                return sb.toString();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mTextView.setText(result);
            // Can not reactivate button / cancel (pending?) events....
            //mButton.setVisibility(View.VISIBLE);
            mButton.setEnabled(true);
            //mButton.setClickable(true);
        }
    }
}


