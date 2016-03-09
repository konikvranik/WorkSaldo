package net.suteren.worksaldo;

/**
 * Created by vranikp on 9.3.16.
 *
 * @author vranikp
 */
public interface IWorkEstimator {

    /**
     * Returns balance cumulated over all days except today.
     *
     * @return whole balance
     */
    float getSaldo();

    /**
     * Returns balance for current day (worked today - hours per day).
     *
     * @return balance of today
     */
    float getSaldoToday();

    /**
     * Returns balance of today + balance cumulated over other days.
     *
     * @return
     */
    float getRemainingToday();

    /**
     * Returns remainder to work to have all hours done. Demanded sum of hours - hours worked.
     *
     * @return
     */
    float getRemainingTotal();

    /**
     * Returns desired worked hours per average day. Demanded hours for period / count of days od period.
     * @return
     */
    float getHoursPerDay();

    float getWorkedHours();

    float getWorkedHoursToday();

    IWorkEstimator addDay(Day day);

    interface Day {

        boolean isToday();

        float getHours();
    }
}
