package org.freekode.tp2intervals.infrastructure.sync

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SyncExecutionRepository :
    CrudRepository<SyncExecutionEntity, Int> {

    fun findTop50ByOrderByStartedAtDesc():
        List<SyncExecutionEntity>
}