package org.freekode.tp2intervals.app.workout.schedule

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class C2CTodayScheduledRequest(
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
) : Schedulable {
    fun forToday() = CopyFromCalendarToCalendarRequest(
        startDate = LocalDate.now(),
        endDate = LocalDate.now(),
        types = types,
        skipSynced = skipSynced,
        sourcePlatform = sourcePlatform,
        targetPlatform = targetPlatform,
        replaceChangedWorkouts =
            sourcePlatform == Platform.TRAINER_ROAD &&
                targetPlatform == Platform.TRAINING_PEAKS
    )
}