package net.suteren.worksaldo;

import org.joda.time.Duration;

/**
 * Class for evaluating balance of worked time.
 *
 * @author vranikp
 */
public interface IWorkEstimator {

    /**
     * Returns balance cumulated over all days except today.
     *
     * @return whole balance
     */
    Duration getSaldo();

    /**
     * Returns balance for current day (worked today - hours per day).
     *
     * @return balance of today
     */
    Duration getSaldoToday();

    /**
     * Returns balance of today + balance cumulated over other days.
     *
     * @return
     */
    Duration getRemainingToday();

    /**
     * Returns remainder to work to have all hours done. Demanded sum of hours - hours worked.
     *
     * @return
     */
    Duration getRemainingTotal();

    /**
     * Returns desired worked hours per average day. Demanded hours for period / count of days od period.
     *
     * @return
     */
    Duration getHoursPerDay();

    /**
     * @return Sum of worked hours except today.
     */
    Duration getWorkedHours();

    /**
     * @return Hours worked today.
     */
    Duration getWorkedHoursToday();

    /**
     * Add hours to be counted in calculation.
     *
     * @param day
     * @return Instance of self for chaining.
     */
    IWorkEstimator addHours(ChunkOfWork day);

    /**
     * Chunk of work which determines worked hour and if it was worked "today".
     */
    interface ChunkOfWork {

        /**
         * @return true for day which is considered to be today.
         */
        boolean isToday();

        /**
         * @return Sum of hours worked this day.
         */
        Duration getHours();
    }
}
