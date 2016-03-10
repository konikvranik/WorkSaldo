package net.suteren.worksaldo;

import org.joda.time.*;
import org.testng.annotations.Test;

import static net.suteren.worksaldo.StandardWorkEstimator.chunkOfWork;
import static org.testng.Assert.assertEquals;

/**
 * Created by hpa on 10.3.16.
 */
@Test
public class StandardWorkEstimatorTest {

    @Test(groups = {"unitTests"})
    public void testGetSaldo() throws Exception {

        LocalDateTime today = LocalDateTime.parse("2016-03-03T15:50:20.045"); // Thursday
        StandardWorkEstimator we = new StandardWorkEstimator(Period.WEEK, today, Duration.standardHours(40))
                .addHours(chunkOfWork(Duration.standardHours(7).plus(Duration.standardMinutes(30)), true))
                .addHours(chunkOfWork(LocalTime.parse("08:30:00"), LocalTime.parse("16:30:00"), false))
                .addHours(chunkOfWork(LocalDateTime.parse("2016-03-01T08:30:00"), LocalDateTime.parse("2016-03-01T16:00:00"), false))
                .addHours(chunkOfWork(LocalTime.parse("08:00:00"), LocalTime.parse("08:30:00"), true));

        assertEquals(Period.WEEK.from(today.toLocalDate()), LocalDate.parse("2016-02-29"));
        assertEquals(Period.WEEK.to(today.toLocalDate()), LocalDate.parse("2016-03-06"));
        assertEquals(Period.WEEK.getDayCount(today.toLocalDate()), Days.days(5));
        assertEquals(Period.WEEK.getDaysBefore(today.toLocalDate()), Days.days(3));

        assertEquals(we.getExpected(), Duration.standardHours(8+8+8));
        assertEquals(we.getWorkedHours(), Duration.standardHours(8+7).plus(Duration.standardMinutes(30)));
        assertEquals(we.getSaldo(), Duration.standardHours(-8).plus(Duration.standardMinutes(-30)));
    }

    @Test
    public void testGetSaldoToday() throws Exception {

    }

    @Test
    public void testGetRemainingToday() throws Exception {

    }

    @Test
    public void testGetRemainingTotal() throws Exception {

    }

    @Test
    public void testGetHoursPerDay() throws Exception {

    }

    @Test
    public void testGetWorkedHours() throws Exception {

    }

    @Test
    public void testGetWorkedHoursToday() throws Exception {

    }

    @Test
    public void testAddHours() throws Exception {

    }
}