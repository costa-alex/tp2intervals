package io.github.costaalex.workoutrelay.app.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfiguration
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.DebugModeService
import io.github.costaalex.workoutrelay.domain.config.PlatformConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformInfo
import io.github.costaalex.workoutrelay.domain.config.PlatformInfoRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import io.github.costaalex.workoutrelay.infrastructure.PlatformErrorCode

class ConfigurationServiceTest {

    private lateinit var platformConfigurationRepository:
        PlatformConfigurationRepository

    private lateinit var platformInfoRepository:
        PlatformInfoRepository

    private lateinit var appConfigurationRepository:
        AppConfigurationRepository

    private lateinit var debugModeService:
        DebugModeService

    private lateinit var cacheManager:
        CacheManager

    private lateinit var configurationService:
        ConfigurationService

    @BeforeEach
    fun setUp() {
        platformConfigurationRepository =
            mock(PlatformConfigurationRepository::class.java)

        platformInfoRepository =
            mock(PlatformInfoRepository::class.java)

        appConfigurationRepository =
            mock(AppConfigurationRepository::class.java)

        debugModeService =
            mock(DebugModeService::class.java)

        cacheManager =
            mock(CacheManager::class.java)

        `when`(platformConfigurationRepository.platform())
            .thenReturn(Platform.INTERVALS)

        `when`(platformInfoRepository.platform())
            .thenReturn(Platform.INTERVALS)

        configurationService =
            ConfigurationService(
                platformConfigurationRepositories =
                    listOf(platformConfigurationRepository),
                platformInfoRepositories =
                    listOf(platformInfoRepository),
                appConfigurationRepository =
                    appConfigurationRepository,
                debugModeService = debugModeService,
                cacheManager = cacheManager,
            )
    }

    @Test
    fun `should update platform configuration`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to "my-api-key",
                    "intervals.athlete-id" to "12345",
                )
            )

        val errors =
            configurationService.updateConfiguration(request)

        assertTrue(errors.isEmpty())

        verify(platformConfigurationRepository)
            .updateConfig(request)

        verify(debugModeService)
            .handleDebugMode(request.configMap)
    }

    @Test
    fun `should return platform error when update fails`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to "invalid-key",
                )
            )

        doThrow(
            PlatformException(
                platform = Platform.INTERVALS,
                code = PlatformErrorCode.AUTHENTICATION_FAILED,
                message = "Invalid credentials",
            )
        )
            .`when`(platformConfigurationRepository)
            .updateConfig(request)

        val errors =
            configurationService.updateConfiguration(request)

        assertEquals(
            listOf(
                ConfigurationUpdateError(
                    platform = Platform.INTERVALS,
                    code = PlatformErrorCode.AUTHENTICATION_FAILED,
                    message = "Invalid credentials",
                )
            ),
            errors,
        )

        verify(debugModeService)
            .handleDebugMode(request.configMap)
    }

    @Test
    fun `should return stored configurations`() {
        val expected =
            AppConfiguration(
                mapOf(
                    "intervals.api-key" to "my-api-key",
                )
            )

        `when`(
            appConfigurationRepository.getConfigurations()
        ).thenReturn(expected)

        val result =
            configurationService.getConfigurations()

        assertSame(expected, result)
    }

    @Test
    fun `should return platform information`() {
        val expected =
            PlatformInfo(
                mapOf(
                    "isValid" to true,
                    "athleteId" to "12345",
                )
            )

        `when`(
            platformInfoRepository.platformInfo()
        ).thenReturn(expected)

        val result =
            configurationService.platformInfo()

        assertSame(
            expected,
            result[Platform.INTERVALS],
        )
    }

    @Test
    fun `should mark platform as invalid when validation fails`() {
        `when`(
            platformInfoRepository.platformInfo()
        ).thenThrow(
            RuntimeException("Unable to decode response")
        )

        val result =
            configurationService.platformInfo()

        assertEquals(
            false,
            result[Platform.INTERVALS]
                ?.infoMap
                ?.get("isValid"),
        )
    }

    @Test
    fun `should clear platform information cache`() {
        val cache =
            mock(Cache::class.java)

        val platformInfo =
            PlatformInfo(
                mapOf("isValid" to true)
            )

        `when`(
            cacheManager.getCache("platformInfoCache")
        ).thenReturn(cache)

        `when`(
            platformInfoRepository.platformInfo()
        ).thenReturn(platformInfo)

        configurationService.refreshPlatformInfo()

        verify(cache).clear()
    }

    @Test
    fun `should return unexpected error when configuration update fails`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to "invalid-key",
                )
            )

        doThrow(
            RuntimeException("Database unavailable")
        )
            .`when`(platformConfigurationRepository)
            .updateConfig(request)

        val errors =
            configurationService.updateConfiguration(request)

        assertEquals(
            listOf(
                ConfigurationUpdateError(
                    platform = Platform.INTERVALS,
                    code = PlatformErrorCode.REQUEST_FAILED,
                    message = "Database unavailable",
                )
            ),
            errors,
        )

        verify(debugModeService)
            .handleDebugMode(request.configMap)
    }

    @Test
    fun `should return fallback error when exception has no message`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to "invalid-key",
                )
            )

        doThrow(
            RuntimeException()
        )
            .`when`(platformConfigurationRepository)
            .updateConfig(request)

        val errors =
            configurationService.updateConfiguration(request)

        assertEquals(
            listOf(
                ConfigurationUpdateError(
                    platform = Platform.INTERVALS,
                    code = PlatformErrorCode.REQUEST_FAILED,
                    message = "Unexpected configuration error",
                )
            ),
            errors,
        )

        verify(debugModeService)
            .handleDebugMode(request.configMap)
    }

    @Test
    fun `should fail with clear error when platform repository is missing`() {
        val exception =
            assertThrows<IllegalStateException> {
                configurationService.platformInfo(
                    Platform.TRAINING_PEAKS
                )
            }

        assertEquals("No PlatformInfoRepository registered for platform TRAINING_PEAKS", exception.message)
    }
    @Test
    fun `should mask sensitive configuration values`() {
        val storedConfiguration =
            AppConfiguration(
                mapOf(
                    "intervals.api-key" to
                        "secret-intervals-key",
                    "training-peaks.auth-cookie" to
                        "Production_tpAuth=secret",
                    "trainer-road.auth-cookie" to
                        "SharedTrainerRoadAuth=secret",
                    "intervals.athlete-id" to
                        "i12345",
                )
            )

        `when`(
            appConfigurationRepository
                .getConfigurations()
        ).thenReturn(storedConfiguration)

        val result =
            configurationService
                .getConfigurationsForDisplay()

        assertEquals(
            "********",
            result.configMap["intervals.api-key"],
        )

        assertEquals(
            "********",
            result.configMap[
                "training-peaks.auth-cookie"
            ],
        )

        assertEquals(
            "********",
            result.configMap[
                "trainer-road.auth-cookie"
            ],
        )

        assertEquals(
            "i12345",
            result.configMap[
                "intervals.athlete-id"
            ],
        )
    }
    
    @Test
    fun `should ignore masked sensitive values on update`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to
                        "********",
                    "intervals.athlete-id" to
                        "i12345",
                )
            )

        configurationService
            .updateConfiguration(request)

        val requestCaptor =
            argumentCaptor<UpdateConfigurationRequest>()

        verify(
            platformConfigurationRepository
        ).updateConfig(
            requestCaptor.capture()
        )

        val sanitizedRequest =
            requestCaptor.firstValue

        assertFalse(
            sanitizedRequest.configMap
                .containsKey("intervals.api-key")
        )

        assertEquals(
            "i12345",
            sanitizedRequest.configMap[
                "intervals.athlete-id"
            ],
        )
    }
    
    @Test
    fun `should forward new sensitive value on update`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to
                        "new-secret-key",
                )
            )

        configurationService
            .updateConfiguration(request)

        val requestCaptor =
            argumentCaptor<UpdateConfigurationRequest>()

        verify(
            platformConfigurationRepository
        ).updateConfig(
            requestCaptor.capture()
        )

        assertEquals(
            "new-secret-key",
            requestCaptor.firstValue.configMap[
                "intervals.api-key"
            ],
        )
    }
}