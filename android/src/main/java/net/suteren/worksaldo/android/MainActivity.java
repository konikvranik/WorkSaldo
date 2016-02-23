package net.suteren.worksaldo.android;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView lv = (ListView) rootView.findViewById(R.id.listing);
            lv.setAdapter(new ArrayAdapter<JSONObject>(inflater.getContext(), R.layout.row));
            new DownloadASyncTask().execute(new JSONObject());
            return rootView;
        }

        private class DownloadASyncTask extends AsyncTask<JSONObject, Void, JSONObject> {

            public static final String USERNAME = "a1b6c4c9842b505be686d421a3082964";
            public static final String PASSWORD = "api_token";

            @Override
            protected JSONObject doInBackground(JSONObject... auth) {
                try {

                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .authority("toggl.com")
                            .path("/reports/api/v2/weekly")
                            .appendQueryParameter("user_agent", "petr@vranik.name")
                            .appendQueryParameter("workspace_id", "1111388")
                            .appendQueryParameter("since", "2016-02-22")
                            .build();

                    String userPassword = USERNAME + ":" + PASSWORD;
                    String encoding = new String(Base64.encode(userPassword.getBytes(), Base64.DEFAULT));
                    URL url = new URL(uri.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "Basic " + encoding);
                    conn.connect();
                    return (JSONObject) conn.getContent();
                } catch (IOException e) {
                    return null;
                }
            }
        }
    }
}
