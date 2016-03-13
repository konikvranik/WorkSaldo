package net.suteren.worksaldo;

import org.joda.time.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static net.suteren.worksaldo.StandardWorkEstimator.chunkOfWork;
import static org.testng.Assert.assertEquals;

/**
 * Created by hpa on 10.3.16.
 */
@Test
public class StandardWorkEstimatorTest {

    @Test(groups = {"unitTests"}, dataProvider = "variants")
    public void testGetSaldo(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {

        assertEquals(we.getSaldo(), saldo);

    }

    @Test(dataProvider = "variants")
    public void testGetSaldoToday(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getSaldoToday(), saldoToday);
    }

    @Test(dataProvider = "variants")
    public void testGetRemainingToday(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getRemainingToday(), remainingToday);
    }

    @Test(dataProvider = "variants")
    public void testGetRemainingTotal(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getRemainingTotal(), remainingTotal);
    }

    @Test(dataProvider = "variants")
    public void testGetHoursPerDay(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getHoursPerDay(), hoursPerDay);
    }

    @Test(dataProvider = "variants")
    public void testGetWorkedHours(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getWorkedHours(), workedHours);
    }

    @Test(dataProvider = "variants")
    public void testGetWorkedHoursToday(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getWorkedHoursToday(), workedToday);
    }

    @Test(dataProvider = "variants")
    public void testGetExpected(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
        assertEquals(we.getExpected(), expectedWork);
    }

    @Test
    public void testChunkOfWork() throws Exception {

    }

    @Test
    public void testChunkOfWork1() throws Exception {

    }

    @Test
    public void testChunkOfWork2() throws Exception {

    }

    @Test
    public void testToDateTime() throws Exception {

    }

    @Test(dataProvider = "variants")
    public void testAddHours(IWorkEstimator we, Duration saldo, Duration saldoToday, Duration remainingToday, Duration remainingTotal, Duration hoursPerDay, Duration workedHours, Duration workedToday, Duration expectedWork) throws Exception {
    }

    @DataProvider
    public Object[][] variants() {
        return new Object[][]{
                {new StandardWorkEstimator(Period.WEEK, LocalDateTime.parse("2016-03-13T08:00:00"), Duration.standardHours(40)) // Sunday
                        .addHours(chunkOfWork(LocalTime.parse("09:00"), LocalTime.parse("18:07"), Duration.standardMinutes(30), false)) // 09:07 - 00:30, 37
                        .addHours(chunkOfWork(LocalTime.parse("09:28"), LocalTime.parse("18:34"), Duration.standardMinutes(30), false)) // 09:06 - 00:30, 36
                        .addHours(chunkOfWork(LocalTime.parse("08:51"), LocalTime.parse("17:42"), Duration.standardMinutes(30), false)) // 08:51 - 00:30, 21
                        .addHours(chunkOfWork(LocalTime.parse("08:00"), LocalTime.parse("17:00"), Duration.standardMinutes(30), false)) // 09:00 - 00:30, 30
                        .addHours(chunkOfWork(LocalTime.parse("09:04"), LocalTime.parse("15:31"), Duration.standardMinutes(30), false)) // 06:27 - 00:30, -02:03
                        ,
                        Duration.standardMinutes(1), // Saldo
                        Duration.standardMinutes(0), // Saldo today
                        Duration.standardMinutes(0), // remaining today
                        Duration.standardMinutes(1), // remaining total
                        Duration.standardHours(0), // hours per day
                        Duration.standardMinutes(1).plus(Duration.standardHours(40)), // worked hours
                        Duration.standardMinutes(0), // worked today
                        Duration.standardHours(40)}, // expected work
                {new StandardWorkEstimator(Period.WEEK, LocalDateTime.parse("2016-03-12T08:00:00"), Duration.standardHours(40)) // Saturday
                        .addHours(chunkOfWork(LocalTime.parse("09:00"), LocalTime.parse("18:07"), Duration.standardMinutes(30), false)) // 09:07 - 00:30, 37
                        .addHours(chunkOfWork(LocalTime.parse("09:28"), LocalTime.parse("18:34"), Duration.standardMinutes(30), false)) // 09:06 - 00:30, 36
                        .addHours(chunkOfWork(LocalTime.parse("08:51"), LocalTime.parse("17:42"), Duration.standardMinutes(30), false)) // 08:51 - 00:30, 21
                        .addHours(chunkOfWork(LocalTime.parse("08:00"), LocalTime.parse("17:00"), Duration.standardMinutes(30), false)) // 09:00 - 00:30, 30
                        .addHours(chunkOfWork(LocalTime.parse("09:04"), LocalTime.parse("15:31"), Duration.standardMinutes(30), false)) // 06:27 - 00:30, -02:03
                        ,
                        Duration.standardMinutes(1), // Saldo
                        Duration.standardMinutes(0), // Saldo today
                        Duration.standardMinutes(0), // remaining today
                        Duration.standardMinutes(1), // remaining total
                        Duration.standardHours(0), // hours per day
                        Duration.standardHours(40).plus(Duration.standardMinutes(1)), // worked hours
                        Duration.standardMinutes(0), // worked today
                        Duration.standardHours(40)}, // expected work
                {new StandardWorkEstimator(Period.WEEK, LocalDateTime.parse("2016-03-11T08:00:00"), Duration.standardHours(40)) // Friday
                        .addHours(chunkOfWork(LocalTime.parse("09:00"), LocalTime.parse("18:07"), Duration.standardMinutes(30), false)) // 09:07 - 00:30, 37
                        .addHours(chunkOfWork(LocalTime.parse("09:28"), LocalTime.parse("18:34"), Duration.standardMinutes(30), false)) // 09:06 - 00:30, 36
                        .addHours(chunkOfWork(LocalTime.parse("08:51"), LocalTime.parse("17:42"), Duration.standardMinutes(30), false)) // 08:51 - 00:30, 21
                        .addHours(chunkOfWork(LocalTime.parse("08:00"), LocalTime.parse("17:00"), Duration.standardMinutes(30), false)) // 09:00 - 00:30, 30
                        .addHours(chunkOfWork(LocalTime.parse("09:04"), LocalTime.parse("15:31"), Duration.standardMinutes(30), true)) // 06:27 - 00:30, -02:03
                        ,
                        Duration.standardHours(2).plus(Duration.standardMinutes(4)), // Saldo
                        Duration.standardHours(-2).plus(Duration.standardMinutes(-3)), // Saldo today
                        Duration.standardHours(0).plus(Duration.standardMinutes(1)), // remaining today
                        Duration.standardHours(0).plus(Duration.standardMinutes(1)), // remaining total
                        Duration.standardHours(8), // hours per day
                        Duration.standardHours(34).plus(Duration.standardMinutes(4)), // worked hours
                        Duration.standardHours(5).plus(Duration.standardMinutes(57)), // worked today
                        Duration.standardHours(32)} // expected work
        };

    }
}