package io.github.costaalex.workoutrelay.app.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.infrastructure.PlatformErrorCode

data class ConfigurationUpdateError(
    val platform: Platform,
    val code: PlatformErrorCode,
    val message: String,
)