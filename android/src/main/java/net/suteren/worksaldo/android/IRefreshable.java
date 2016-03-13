package net.suteren.worksaldo.android;

/**
 * Created by hpa on 7.3.16.
 */
public interface IRefreshable {

    void refresh();

    void onRefresh(Runnable action);
}
