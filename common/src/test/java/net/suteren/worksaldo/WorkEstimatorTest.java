package net.suteren.worksaldo;


import android.database.MatrixCursor;
import android.util.Log;
import junit.framework.TestCase;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



/**
 * Created by vranikp on 8.3.16.
 *
 * @author vranikp
 */
@Test(groups = {})
public class WorkEstimatorTest  {

    public void testGetSaldoTest() {
        Calendar cal = Calendar.getInstance();
        cal.set(1975, 10, 28, 15, 30, 24); // Friday
        cal.set(Calendar.MILLISECOND, 320);
        String date = String.format("Date: %s", SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG,
                SimpleDateFormat.LONG).format(cal.getTime()));
        Log.d("Test", date);
        WorkEstimator we = new WorkEstimator(Period.WEEK, cal, 40, 30, false);

        Calendar caltest = Calendar.getInstance();
        caltest.setTimeInMillis(0);
        caltest.set(1975, 10, 24); // Monday

        DateFormat dateTimeInstance = SimpleDateFormat.getDateTimeInstance();
        Assert.assertEquals(String.format("%s ... %s", dateTimeInstance.format(caltest.getTime()), dateTimeInstance
                .format
                        (Period.WEEK.from(cal).getTime())), caltest.getTimeInMillis(), Period.WEEK.from(cal).getTimeInMillis());

        caltest.set(1975, 10, 30); // Sunday
        Assert.assertEquals(String.format("%s ... %s", dateTimeInstance.format(caltest.getTime()), dateTimeInstance
                .format
                        (Period.WEEK.to(cal).getTime())), caltest.getTimeInMillis(), Period.WEEK.to(cal).getTimeInMillis());

        assertEquals(4, Math.round(we.getPastDayCount()));

        assertEquals(75, Math.round(we.getCount(8f, "08:30:15", "10:15:15", false) * 60));

        //assertEquals(308, Math.round(we.getCount(8f, "08:30:15", null, false) * 60));

        Assert.assertEquals(-2 * 60, (int) we.getSaldo(30f, 4f) * 60);
        Assert.assertEquals(8 * 60, (int) we.getHoursPerDay() * 60);
        Assert.assertEquals(-3 * 60, (int) we.getTodayToAvg(5f) * 60);
        Assert.assertEquals(-5 * 60, (int) we.getTodayToWhole(new float[]{30f, 5f}) * 60);

        MatrixCursor c = new MatrixCursor(new String[]{"id", TogglCachedProvider.DATE_NAME, TogglCachedProvider.DAY_START_NAME, TogglCachedProvider.DAY_END_NAME, TogglCachedProvider.DAY_TOTAL_NAME,
                TogglCachedProvider.DAY_SALDO_NAME});
        c.newRow().add(1).add("2016-03-07").add("08:30:00").add("16:00:00").add(7.5).add(7.5);
        c.newRow().add(2).add("2016-03-08").add("08:00:00").add("17:00:00").add(9).add(9);
        c.newRow().add(3).add("2016-03-09").add("08:00:00").add("17:30:00").add(9.5).add(9.5);
        c.newRow().add(4).add("2016-03-10").add("08:00:00").add("17:30:00").add(9.5).add(9.5);
        c.newRow().add(5).add("2016-03-11").add("08:00:00").add("11:30:00").add(3.5).add(3.5);
        c.moveToFirst();

        float[] actual = we.countTotal(c, false);
        Assert.assertEquals(27.5, actual[0]);
        Assert.assertEquals(9.15, actual[1]);
    }

    public void testGetHoursPerDay() {

    }

    public void testGetPastDayCount() throws Exception {

    }

    public void testCountTotal() throws Exception {

    }

    public void testGetCount() throws Exception {

    }

    public void testGetPause() throws Exception {

    }

    public void testSetPause() throws Exception {

    }

    public void testGetSaldo1() throws Exception {

    }

    public void testGetTodayToAvg() throws Exception {

    }

    public void testGetTodayToWhole() throws Exception {

    }
}