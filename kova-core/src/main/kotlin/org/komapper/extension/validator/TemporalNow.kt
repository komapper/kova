package org.komapper.extension.validator

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.temporal.Temporal

/**
 * Strategy interface for obtaining the current temporal value from a [Clock].
 *
 * This abstraction allows [TemporalValidator] to work with different temporal types
 * (LocalDate, LocalTime, LocalDateTime) which have different `now(Clock)` methods.
 *
 * @param T The temporal type
 */
interface TemporalNow<T : Temporal> {
    /**
     * Gets the current temporal value using the provided clock.
     *
     * @param clock The clock to use for determining "now"
     * @return The current temporal value
     */
    fun now(clock: Clock): T
}

/**
 * [TemporalNow] implementation for [LocalDate].
 *
 * Provides the current date using [LocalDate.now].
 */
object LocalDateNow : TemporalNow<LocalDate> {
    override fun now(clock: Clock): LocalDate = LocalDate.now(clock)
}

/**
 * [TemporalNow] implementation for [LocalTime].
 *
 * Provides the current time using [LocalTime.now].
 */
object LocalTimeNow : TemporalNow<LocalTime> {
    override fun now(clock: Clock): LocalTime = LocalTime.now(clock)
}

/**
 * [TemporalNow] implementation for [LocalDateTime].
 *
 * Provides the current date-time using [LocalDateTime.now].
 */
object LocalDateTimeNow : TemporalNow<LocalDateTime> {
    override fun now(clock: Clock): LocalDateTime = LocalDateTime.now(clock)
}

/**
 * [TemporalNow] implementation for [Instant].
 *
 * Provides the current instant using [Instant.now].
 */
object InstantNow : TemporalNow<Instant> {
    override fun now(clock: Clock): Instant = Instant.now(clock)
}

/**
 * [TemporalNow] implementation for [OffsetDateTime].
 *
 * Provides the current offset date-time using [OffsetDateTime.now].
 */
object OffsetDateTimeNow : TemporalNow<OffsetDateTime> {
    override fun now(clock: Clock): OffsetDateTime = OffsetDateTime.now(clock)
}

/**
 * [TemporalNow] implementation for [OffsetTime].
 *
 * Provides the current offset time using [OffsetTime.now].
 */
object OffsetTimeNow : TemporalNow<OffsetTime> {
    override fun now(clock: Clock): OffsetTime = OffsetTime.now(clock)
}

/**
 * [TemporalNow] implementation for [Year].
 *
 * Provides the current year using [Year.now].
 */
object YearNow : TemporalNow<Year> {
    override fun now(clock: Clock): Year = Year.now(clock)
}

/**
 * [TemporalNow] implementation for [YearMonth].
 *
 * Provides the current year-month using [YearMonth.now].
 */
object YearMonthNow : TemporalNow<YearMonth> {
    override fun now(clock: Clock): YearMonth = YearMonth.now(clock)
}

/**
 * [TemporalNow] implementation for [ZonedDateTime].
 *
 * Provides the current zoned date-time using [ZonedDateTime.now].
 */
object ZonedDateTimeNow : TemporalNow<ZonedDateTime> {
    override fun now(clock: Clock): ZonedDateTime = ZonedDateTime.now(clock)
}
