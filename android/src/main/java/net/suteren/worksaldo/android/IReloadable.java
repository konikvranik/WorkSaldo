package net.suteren.worksaldo.android;

/**
 * Interface which provides methods to reload data from backend.
 */
public interface IReloadable {

    /**
     * Causes reloading data from backend.
     */
    void reload();

    /**
     * Sets action to be executed when reload is finished.
     *
     * @param action Action to execute when reload is finished.
     */
    void onReload(Runnable action);
}
