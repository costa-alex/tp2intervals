package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.execution.SyncExecutionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SyncExecutionController(
    private val syncExecutionService: SyncExecutionService
) {

    @GetMapping("/api/sync-executions")
    fun getRecentExecutions() =
        syncExecutionService.getRecentExecutions()
}