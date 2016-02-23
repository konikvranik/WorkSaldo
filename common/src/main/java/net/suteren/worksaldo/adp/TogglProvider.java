package net.suteren.worksaldo.adp;

import com.google.gson.Gson;
import net.suteren.worksaldo.model.Client;
import net.suteren.worksaldo.model.TogglDataWrapper;
import net.suteren.worksaldo.model.UserDetail;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by petr on 23.02.2016.
 */
public class TogglProvider {

    public static final String USERNAME = "a1b6c4c9842b505be686d421a3082964";
    public static final String PASSWORD = "api_token";
    protected static UriBuilder uriBuilder;
    private static Base64 encoder;

    public static UriBuilder getUriBuilder() {
        return uriBuilder;
    }

    public static Base64 getEncoder() {
        return encoder;
    }

    public static void setUriBuilder(UriBuilder uriBuilder) {
        TogglProvider.uriBuilder = uriBuilder;
    }

    public List<Client> getClients() throws IOException, JSONException {

        Map<String, String> params = new HashMap<>();
        UserDetail ud = download(UserDetail.class, RequestMethod.GET, "/api/v8/me", params, USERNAME, PASSWORD);
        return ud.clients;
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


    public static <T> T download(final Class<T> type, RequestMethod method, String path, Map<String, String> params, String username,
                                 String password) throws IOException, JSONException {
        JSONObject param = new JSONObject();
        UriBuilder ub = getUriBuilder()
                .scheme("https")
                .authority("toggl.com")
                .path(path);
        makeParams(method, ub, param, params);
        URL url = ub.build();

        String userPassword = username + ":" + password;
        String encoding = new String(getEncoder().encode(userPassword.getBytes()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Basic " + encoding);
        conn.connect();

        Type fooType =  new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{type};
            }

            @Override
            public Type getRawType() {
                return TogglDataWrapper.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }

        };
        TogglDataWrapper<T> r = new Gson().fromJson(new InputStreamReader((InputStream) conn.getContent()), fooType);
        return r.data;
    }

    private static void makeParams(RequestMethod method, UriBuilder ub, JSONObject param, Map<String, String> params) throws JSONException {
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

    public static void setEncoder(Base64 e) {
        encoder = e;
    }

    public enum RequestMethod {
        GET, POST
    }

}
