package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import org.freekode.tp2intervals.domain.workout.Workout

@Service
class WorkoutService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    private fun hasSameExternalId(source: ExternalData, target: ExternalData): Boolean {
        return (source.trainerRoadId != null && source.trainerRoadId == target.trainerRoadId)
            || (source.trainingPeaksId != null && source.trainingPeaksId == target.trainingPeaksId)
            || (source.intervalsId != null && source.intervalsId == target.intervalsId)
    }
    
    private data class SaveWorkoutsResult(
        val copied: Int,
        val failures: List<WorkoutSyncFailure>
    )
    
    private fun saveWorkoutsIndividually(
        repository: WorkoutRepository,
        workouts: List<Workout>
    ): SaveWorkoutsResult {

        var copied = 0
        val failures = mutableListOf<WorkoutSyncFailure>()

        workouts.forEach { workout ->
            try {
                repository.saveWorkoutToCalendar(workout)
                copied++
            } catch (exception: Exception) {
                log.error(
                    "Failed to sync workout '${workout.details.name}' " +
                        "on ${workout.date}",
                    exception
                )

                failures += WorkoutSyncFailure(
                    workoutName = workout.details.name,
                    workoutDate = workout.date,
                    message = exception.message
                        ?.lineSequence()
                        ?.firstOrNull()
                        ?.take(300)
                        ?: exception.javaClass.simpleName
                )
            }
        }

        return SaveWorkoutsResult(
            copied = copied,
            failures = failures
        )
    }
    
    fun copyWorkoutsC2C(
        request: CopyFromCalendarToCalendarRequest
    ): CopyWorkoutsResponse {

        log.info("Received request for copy calendar to calendar: $request")

        val sourceWorkoutRepository =
            workoutRepositoryMap[request.sourcePlatform]!!

        val targetWorkoutRepository =
            workoutRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getWorkoutsFromCalendar(
            request.startDate,
            request.endDate
        )

        val workoutsAfterTypeFilter = allWorkouts.filter {
            request.types.contains(it.details.type)
        }

        val skippedByType =
            allWorkouts.size - workoutsAfterTypeFilter.size

        val workoutsToSync = if (
            request.skipSynced &&
            workoutsAfterTypeFilter.isNotEmpty()
        ) {
            val plannedWorkouts =
                targetWorkoutRepository.getWorkoutsFromCalendar(
                    request.startDate,
                    request.endDate
                )

            workoutsAfterTypeFilter.filter { sourceWorkout ->
                plannedWorkouts.none { targetWorkout ->
                    hasSameExternalId(
                        sourceWorkout.details.externalData,
                        targetWorkout.details.externalData
                    )
                }
            }
        } else {
            workoutsAfterTypeFilter
        }

        val skippedAlreadySynced =
            workoutsAfterTypeFilter.size - workoutsToSync.size

        val saveResult = saveWorkoutsIndividually(
            repository = targetWorkoutRepository,
            workouts = workoutsToSync
        )

        val response = CopyWorkoutsResponse(
            copied = saveResult.copied,
            filteredOut = skippedByType + skippedAlreadySynced,
            skippedByType = skippedByType,
            skippedAlreadySynced = skippedAlreadySynced,
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = ExternalData.empty(),
            failed = saveResult.failures.size,
            failedWorkouts = saveResult.failures
        )

        log.info(
            "Calendar sync completed. copied={}, skippedByType={}, " +
                "skippedAlreadySynced={}, failed={}",
            response.copied,
            response.skippedByType,
            response.skippedAlreadySynced,
            response.failed
        )

        return response
    }

    fun copyWorkoutsC2L(request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to library: $request")
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts.filter { request.types.contains(it.details.type) }

        val newPlan = targetPlanRepository.createLibraryContainer(request.name, request.isPlan, request.startDate)
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, filteredWorkouts)
        val skippedByType = allWorkouts.size - filteredWorkouts.size

        return CopyWorkoutsResponse(
            copied = filteredWorkouts.size,
            filteredOut = skippedByType,
            skippedByType = skippedByType,
            skippedAlreadySynced = 0,
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = newPlan.externalData
        )
    }

    fun copyWorkoutL2L(request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy library to library: $request")
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workout = sourceWorkoutRepository.getWorkoutFromLibrary(request.workoutExternalData)
        targetWorkoutRepository.saveWorkoutsToLibrary(request.targetLibraryContainer, listOf(workout))
        return CopyWorkoutsResponse(
            copied = 1,
            filteredOut = 0,
            skippedByType = 0,
            skippedAlreadySynced = 0,
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            externalData = request.targetLibraryContainer.externalData
        )
    }

    fun findWorkoutsByName(platform: Platform, name: String): List<WorkoutDetails> {
        log.info("Received request for find workouts by name, platform: $platform, name: $name")
        return workoutRepositoryMap[platform]!!.findWorkoutsFromLibraryByName(name)
    }

    fun deleteWorkoutsFromCalendar(request: DeleteWorkoutRequestDTO) {
        log.info("Received request to delete workouts from calendar: $request")
        val workoutRepository = workoutRepositoryMap[request.platform]!!
        workoutRepository.deleteWorkoutsFromCalendar(request.startDate, request.endDate)
    }
}
