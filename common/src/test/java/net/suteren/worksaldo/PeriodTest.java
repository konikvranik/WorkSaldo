package net.suteren.worksaldo;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by hpa on 13.3.16.
 */
public class PeriodTest {

    @Test(dataProvider = "variants")
    public void testFrom(Period p, LocalDate date, LocalDate from, LocalDate to, Days days, Days daysBefore) throws Exception {

        assertEquals(p.from(date), from);

    }

    @Test(dataProvider = "variants")
    public void testTo(Period p, LocalDate date, LocalDate from, LocalDate to, Days days, Days daysBefore) throws Exception {
        assertEquals(p.to(date), to);
    }

    @Test(dataProvider = "variants")
    public void testGetDayCount(Period p, LocalDate date, LocalDate from, LocalDate to, Days days, Days daysBefore) throws Exception {
        assertEquals(p.getDayCount(date), days);
    }

    @Test(dataProvider = "variants")
    public void testGetDaysBefore(Period p, LocalDate date, LocalDate from, LocalDate to, Days days, Days daysBefore) throws Exception {
        assertEquals(p.getDaysBefore(date), daysBefore);
    }

    @DataProvider
    public Object[][] variants() {
        return new Object[][]{
                {Period.WEEK, LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(0)},
                {Period.WEEK, LocalDate.parse("2016-03-08"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(1)},
                {Period.WEEK, LocalDate.parse("2016-03-09"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(2)},
                {Period.WEEK, LocalDate.parse("2016-03-10"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(3)},
                {Period.WEEK, LocalDate.parse("2016-03-11"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(4)},
                {Period.WEEK, LocalDate.parse("2016-03-12"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(5)},
                {Period.WEEK, LocalDate.parse("2016-03-13"), LocalDate.parse("2016-03-07"), LocalDate.parse("2016-03-13"), Days.days(5), Days.days(5)}
        };
    }
}