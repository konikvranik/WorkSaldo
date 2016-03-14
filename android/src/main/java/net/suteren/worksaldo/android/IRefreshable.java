package net.suteren.worksaldo.android;

/**
 * Interface which provides methods to refresh data from local database.
 */
public interface IRefreshable {

    /**
     * Refresh data from local database.
     */
    void refresh();

    /**
     * Set action to be executed after refresh.
     *
     * @param action Action to be executed after refresh completes.
     */
    void onRefresh(Runnable action);
}
