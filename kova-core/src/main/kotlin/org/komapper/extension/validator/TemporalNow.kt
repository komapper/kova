package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
