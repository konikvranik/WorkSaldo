package net.suteren.worksaldo.adp;

import java.net.URL;

/**
 * Created by petr on 23.02.2016.
 */
public interface UriBuilder {
    UriBuilder scheme(String https);

    UriBuilder authority(String s);

    UriBuilder path(String path);

    URL build();

    UriBuilder appendQueryParameter(String key, String value);
}
