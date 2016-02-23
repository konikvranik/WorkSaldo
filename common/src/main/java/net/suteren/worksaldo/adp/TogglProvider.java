package net.suteren.worksaldo.adp;

import android.net.Uri;
import android.util.Base64;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import net.suteren.worksaldo.model.Client;
import net.suteren.worksaldo.model.UserDetail;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by petr on 23.02.2016.
 */
public class TogglProvider {

    public static final String USERNAME = "a1b6c4c9842b505be686d421a3082964";
    public static final String PASSWORD = "api_token";

    public Client getClients() throws IOException, JSONException {

        Map<String, String> params=new HashMap<>();
        UserDetail ud = download(RequestMethod.GET, "/api/v8/me", params, USERNAME, PASSWORD);

     /*   Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("toggl.com")
                .path(path)
                        //.path("/reports/api/v2/weekly")
                .appendQueryParameter("user_agent", "petr@vranik.name")
                .appendQueryParameter("workspace_id", "1111388")
                .appendQueryParameter("since", "2016-02-22")
                .build();
*/
    }


    public static <T> T download(RequestMethod method, String path, Map<String, String> params, String username,
                                 String password) throws IOException, JSONException {
        JSONObject param = new JSONObject();
        Uri.Builder ub = new Uri.Builder()
                .scheme("https")
                .authority("toggl.com")
                .path(path);
        makeParams(method, ub, param, params);
        Uri uri = ub.build();

        String userPassword = username + ":" + password;
        String encoding = new String(Base64.encode(userPassword.getBytes(), Base64.DEFAULT));
        URL url = new URL(uri.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Basic " + encoding);
        conn.connect();
        Gson gson = new Gson();
        TypeAdapter<TogglDataWrapper> cad = gson.getAdapter(TogglDataWrapper.class);
        TogglDataWrapper<T> r = cad.fromJson(new InputStreamReader((InputStream) conn.getContent()));
        return r.data;
    }

    private static void makeParams(RequestMethod method, Uri.Builder ub, JSONObject param, Map<String, String> params) throws JSONException {
        for (Map.Entry<String, String> e : params.entrySet()) {
            switch (method) {
                case GET:
                    ub.appendQueryParameter(e.getKey(), e.getValue());
                    break;
                case POST:
                    param.put(e.getKey(), e.getValue());
                    break;
            }
        }
    }

    public enum RequestMethod {
        GET, POST
    }

}
