package net.suteren.worksaldo;

import org.joda.time.*;
import org.joda.time.base.BaseLocal;

import java.util.HashSet;
import java.util.Set;

/**
 * Common work estimator.
 */
public class StandardWorkEstimator implements IWorkEstimator {

    private final Duration demandedHours;
    private final Period period;
    private final LocalDateTime now;
    private Set<ChunkOfWork> chunksOfWork = new HashSet<>();

    public StandardWorkEstimator(Period period, LocalDateTime now, Duration demandedHours) {
        this.period = period;
        this.demandedHours = demandedHours;
        this.now = now;
    }

    @Override
    public Duration getSaldo() {
        return getWorkedHours().minus(getExpected());
    }

    @Override
    public Duration getExpected() {
        final LocalDate now = this.now.toLocalDate();

        final int totalDays = period.getDayCount(now).getDays();
        final int daysBefore = period.getDaysBefore(now).getDays();
        return demandedHours.multipliedBy(daysBefore).dividedBy(totalDays);
    }


    @Override
    public Duration getSaldoToday() {
        return getWorkedHoursToday().minus(getHoursPerDay());
    }

    @Override
    public Duration getRemainingToday() {
        if (period.getDaysBefore(now.toLocalDate()).getDays() < period.getDayCount(now.toLocalDate()).getDays()) {
            return getWorkedHours().plus(getWorkedHoursToday()).minus(getExpected().plus(getHoursPerDay()));
        } else {
            return Duration.ZERO;
        }
    }

    @Override
    public Duration getRemainingTotal() {
        return getWorkedHours().plus(getWorkedHoursToday()).minus(demandedHours);
    }

    @Override
    public Duration getHoursPerDay() {
        if (period.getDaysBefore(now.toLocalDate()).getDays() < period.getDayCount(now.toLocalDate()).getDays()) {
            return demandedHours.dividedBy(period.getDayCount(this.now.toLocalDate()).getDays());
        } else {
            return Duration.ZERO;
        }

    }

    @Override
    public Duration getWorkedHours() {
        Duration sum = Duration.ZERO;
        for (ChunkOfWork ch : chunksOfWork) {
            if (!ch.isToday()) {
                sum = sum.plus(ch.getHours());
            }
        }
        return sum;
    }


    @Override
    public Duration getWorkedHoursToday() {
        Duration sum = Duration.ZERO;
        for (ChunkOfWork ch : chunksOfWork) {
            if (ch.isToday()) {
                sum = sum.plus(ch.getHours());
            }
        }
        return sum;
    }

    @Override
    public StandardWorkEstimator addHours(ChunkOfWork day) {
        chunksOfWork.add(day);
        return this;
    }

    /**
     * Helper for creating chunks of work for estimator.
     *
     * @param amount  Duration of work.
     * @param isToday Determines if this work was done today.
     * @return
     */
    public static ChunkOfWork chunkOfWork(final Duration amount, final boolean isToday) {
        return new ChunkOfWork() {
            @Override
            public boolean isToday() {
                return isToday;
            }

            @Override
            public Duration getHours() {
                return amount;
            }
        };
    }

    /**
     * Helper for creating chunks of work for estimator.
     *
     * @param from    Time when work was started.
     * @param to      Time when work was stopped.
     * @param isToday Determines if this work was done today.
     * @return
     */
    public static ChunkOfWork chunkOfWork(final BaseLocal from, final BaseLocal to, final boolean isToday) {
        return new ChunkOfWork() {
            @Override
            public boolean isToday() {
                return isToday;
            }

            @Override
            public Duration getHours() {
                return new Duration(toDateTime(from), toDateTime(to));
            }
        };
    }

    /**
     * Helper for creating chunks of work for estimator.
     *
     * @param from    Time when work was started.
     * @param to      Time when work was stopped.
     * @param pause   Mandatory work pause which will be subtracted.
     * @param isToday Determines if this work was done today.
     * @return
     */
    public static ChunkOfWork chunkOfWork(final BaseLocal from, final BaseLocal to, final Duration pause, final boolean isToday) {
        return new ChunkOfWork() {
            @Override
            public boolean isToday() {
                return isToday;
            }

            @Override
            public Duration getHours() {
                return new Duration(toDateTime(from), toDateTime(to)).minus(pause);
            }
        };
    }

    private static DateTime toDateTime(BaseLocal from) {
        DateTime start;
        if (from instanceof LocalDateTime) {
            start = ((LocalDateTime) from).toDateTime();
        } else if (from instanceof LocalTime) {
            start = ((LocalTime) from).toDateTimeToday();
        } else {
            start = new DateTime(from);
        }
        return start;
    }
}
