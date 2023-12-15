package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurationDatabaseContextTest {

    @Test
    fun `Should combine all databases validation errors`() {
        val expectedUnusedConfig = IntRange(0, 15).map { CurationConfigValidationError(it.toString()) }
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
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[13]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[14]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[15]),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk()
        )
        assertThat(context.validate()).containsExactlyElementsOf(expectedUnusedConfig)
    }

    @Test
    fun `Should combine all unused configs in curation databases`() {
        val expectedUnusedConfig = IntRange(0, 20).map { UnusedCurationConfig(CurationCategory.TOXICITY, it.toString()) }
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
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[13]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[14]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[15]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[16]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[17]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[18]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[19]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[20])
        )
        assertThat(context.allUnusedConfig(listOf(ExtractionEvaluation()))).containsExactlyInAnyOrderElementsOf(expectedUnusedConfig)
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