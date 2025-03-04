package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val NOT_IMPORTANT = "not important"

class CurationDatabaseContextTest {

    @Test
    fun `Should combine all databases validation errors`() {
        val expectedUnusedConfig = IntRange(0, 12).map {
            CurationConfigValidationError(NOT_IMPORTANT, NOT_IMPORTANT, NOT_IMPORTANT, it.toString(), NOT_IMPORTANT)
        }
        val context = CurationDatabaseContext(
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[0]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[1]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[2]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[3]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[4]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[5]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[6]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[7]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[8]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[9]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[10]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[11]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[12]),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
        )
        assertThat(context.validate()).containsExactlyElementsOf(expectedUnusedConfig)
    }

    @Test
    fun `Should combine all unused configs in curation databases, except blood transfusions`() {
        val expectedUnusedConfig = IntRange(0, 15).map { UnusedCurationConfig(CurationCategory.TOXICITY.name, it.toString()) }
        val bloodTransfusionTranslation = mockk<TranslationDatabase<String>>()
        val context = CurationDatabaseContext(
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[0]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[1]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[2]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[3]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[4]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[5]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[6]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[7]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[8]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[9]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[10]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[11]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[12]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[13]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[14]),
            bloodTransfusionTranslation,
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[15]),
        )
        assertThat(context.allUnusedConfig(listOf(CurationExtractionEvaluation()))).containsExactlyInAnyOrderElementsOf(expectedUnusedConfig)
        verify { bloodTransfusionTranslation wasNot Called }
    }

    private fun <T : CurationConfig> curationDatabaseWithUnusedConfig(vararg errors: CurationConfigValidationError): CurationDatabase<T> {
        val database = mockk<CurationDatabase<T>>()
        every { database.validationErrors } returns errors.toList()
        return database
    }

    private fun <T : CurationConfig> curationDatabaseWithUnusedConfig(vararg unused: UnusedCurationConfig): CurationDatabase<T> {
        val database = mockk<CurationDatabase<T>>()
        every { database.reportUnusedConfig(any()) } returns unused.toList()
        return database
    }

    private fun <T> translationDatabaseWithUnusedConfig(vararg unused: UnusedCurationConfig): TranslationDatabase<T> {
        val database = mockk<TranslationDatabase<T>>()
        every { database.reportUnusedTranslations(any()) } returns unused.toList()
        return database
    }
}