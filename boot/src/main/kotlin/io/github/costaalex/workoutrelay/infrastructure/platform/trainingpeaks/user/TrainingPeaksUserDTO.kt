package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user

import com.fasterxml.jackson.annotation.JsonProperty

class TrainingPeaksUserDTO(
    var userId: String?,
    val accountStatus: TPUserAccountStatusDTO,
) {

    @JsonProperty("user")
    private fun mapUserId(map: Map<*, *>) {
        userId = (map["userId"] as Number).toLong().toString()
    }

    class TPUserAccountStatusDTO(
        val isAthlete: Boolean,
        val isPremium: Boolean = false,
    )
}