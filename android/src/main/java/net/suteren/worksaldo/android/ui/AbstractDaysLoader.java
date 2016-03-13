package net.suteren.worksaldo.android.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import net.suteren.worksaldo.Period;
import net.suteren.worksaldo.android.provider.TogglCachedProvider;
import org.joda.time.LocalDate;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;

/**
 * Created by hpa on 7.3.16.
 */
