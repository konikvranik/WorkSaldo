package net.suteren.worksaldo;

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
     *
     * @return
     */
    float getHoursPerDay();

    /**
     * @return Sum of worked hours except today.
     */
    float getWorkedHours();

    /**
     * @return Hours worked today.
     */
    float getWorkedHoursToday();

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
        float getHours();
    }
}
