package net.suteren.worksaldo.android;

/**
 * Created by hpa on 7.3.16.
 */
public interface IReloadable {

    void reload();

    void onReload(Runnable action);
}
