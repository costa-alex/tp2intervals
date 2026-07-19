package io.github.costaalex.workoutrelay.rest.configuration

data class ConfigurationUpdateErrorResponseDTO(
    val message: String,
    val errors: List<ConfigurationUpdateErrorDTO>,
)

data class ConfigurationUpdateErrorDTO(
    val platform: String,
    val code: String,
    val message: String,
)