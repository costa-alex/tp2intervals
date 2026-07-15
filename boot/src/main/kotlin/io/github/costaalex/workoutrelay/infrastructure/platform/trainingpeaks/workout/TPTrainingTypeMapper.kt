package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

import io.github.costaalex.workoutrelay.domain.TrainingType

object TPTrainingTypeMapper {

    private val workoutTypeMap = mapOf(
        TrainingType.SWIM to TPWorkoutType.SWIM,
        TrainingType.BIKE to TPWorkoutType.BIKE,
        //TrainingType.VIRTUAL_BIKE to TPWorkoutType.BIKE,
        TrainingType.MTB to TPWorkoutType.MTB_BIKE,
        TrainingType.RUN to TPWorkoutType.RUN,
        TrainingType.WALK to TPWorkoutType.RUN,
        TrainingType.STRENGTH to TPWorkoutType.STRENGTH,
        TrainingType.NOTE to TPWorkoutType.DAY_OFF,
        TrainingType.UNKNOWN to TPWorkoutType.OTHER
    )

    private val workoutSubTypeMap = mapOf(
        TrainingType.SWIM to TPWorkoutSubType.POOL_SWIM,
        TrainingType.BIKE to TPWorkoutSubType.ROAD_BIKE,
        //TrainingType.VIRTUAL_BIKE to TPWorkoutSubType.VIRTUAL_BIKE,
        TrainingType.MTB to TPWorkoutSubType.XC_MTB,
        TrainingType.RUN to TPWorkoutSubType.ROAD_RUN,
        TrainingType.WALK to TPWorkoutSubType.HIKE,
        TrainingType.STRENGTH to TPWorkoutSubType.STRENGTH,
        TrainingType.NOTE to TPWorkoutSubType.DAY_OFF,
        TrainingType.UNKNOWN to TPWorkoutSubType.OTHER
    )

    fun getWorkoutType(
        trainingType: TrainingType
    ): TPWorkoutType =
        workoutTypeMap[trainingType] ?: TPWorkoutType.OTHER

    fun getWorkoutSubType(
        trainingType: TrainingType
    ): TPWorkoutSubType =
        workoutSubTypeMap[trainingType] ?: TPWorkoutSubType.OTHER

    fun getWorkoutTypeValueId(
        trainingType: TrainingType
    ): Int =
        getWorkoutType(trainingType).valueId

    fun getWorkoutSubTypeValueId(
        trainingType: TrainingType
    ): Int =
        getWorkoutSubType(trainingType).valueId

    fun getByValue(value: Int): TrainingType =
        workoutTypeMap.entries
            .firstOrNull { it.value.valueId == value }
            ?.key
            ?: TrainingType.UNKNOWN

    fun getSubtypeByValue(value: Int): TrainingType =
        workoutSubTypeMap.entries
            .firstOrNull { it.value.valueId == value }
            ?.key
            ?: TrainingType.UNKNOWN
}