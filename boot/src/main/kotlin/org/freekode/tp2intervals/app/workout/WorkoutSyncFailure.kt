package org.freekode.tp2intervals.app.workout

import java.time.LocalDate

data class WorkoutSyncFailure(
    val workoutName: String,
    val workoutDate: LocalDate?,
    val message: String
)