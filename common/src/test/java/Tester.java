import net.suteren.worksaldo.adp.Base64;
import net.suteren.worksaldo.adp.TogglProvider;
import net.suteren.worksaldo.adp.UriBuilder;
import net.suteren.worksaldo.model.Client;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by petr on 23.02.2016.
 */
public class Tester {

    static public void main(String... args) throws IOException {

        TogglProvider.setEncoder(
                new Base64() {
                    @Override
                    public byte[] encode(byte[] bytes) {
                        return java.util.Base64.getEncoder().encode(bytes);
                    }
                }
        );
        TogglProvider.setUriBuilder(new UriBuilder() {
            URIBuilder builder;

            @Override
            public UriBuilder scheme(String https) {
                builder = builder.setScheme(https);
                return this;
            }

            @Override
            public UriBuilder authority(String s) {
                builder = builder.setHost(s);
                return this;
            }

            @Override
            public UriBuilder path(String path) {
                builder = builder.setPath(path);
                return this;
            }

            @Override
            public URL build() {
                try {
                    return builder.build().toURL();
                } catch (URISyntaxException | MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public UriBuilder appendQueryParameter(String key, String value) {
                builder = builder.addParameter(key, value);
                return this;
            }
        });

        TogglProvider tp = new TogglProvider();
        List<Client> c = tp.getClients();

    }

}
