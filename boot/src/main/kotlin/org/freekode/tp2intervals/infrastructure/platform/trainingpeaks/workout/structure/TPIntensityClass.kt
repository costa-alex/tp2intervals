package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout.structure

enum class TPIntensityClass(
    val apiValue: String
) {
    WARM_UP("warmUp"),
    ACTIVE("active"),
    REST("rest"),
    COOL_DOWN("coolDown")
}