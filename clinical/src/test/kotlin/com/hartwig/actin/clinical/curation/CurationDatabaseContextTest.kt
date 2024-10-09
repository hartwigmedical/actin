package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
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
        val expectedUnusedConfig = IntRange(0, 18).map {
            CurationConfigValidationError(
                NOT_IMPORTANT,
                NOT_IMPORTANT,
                NOT_IMPORTANT, it.toString(),
                NOT_IMPORTANT
            )
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
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[13]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[14]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[15]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[16]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[17]),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[18])
        )
        assertThat(context.validate()).containsExactlyElementsOf(expectedUnusedConfig)
    }

    @Test
    fun `Should combine all unused configs in curation databases, except cyp, qt and blood transfusions`() {
        val expectedUnusedConfig = IntRange(0, 20).map { UnusedCurationConfig(CurationCategory.TOXICITY.name, it.toString()) }
        val cypInteractionCuration = mockk<CurationDatabase<CypInteractionConfig>>()
        val qtProlongingCuration = mockk<CurationDatabase<QTProlongatingConfig>>()
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
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[13]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[14]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[15]),
            cypInteractionCuration,
            qtProlongingCuration,
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[16]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[17]),
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[18]),
            bloodTransfusionTranslation,
            translationDatabaseWithUnusedConfig(expectedUnusedConfig[19]),
            curationDatabaseWithUnusedConfig(expectedUnusedConfig[20])
        )
        assertThat(context.allUnusedConfig(listOf(CurationExtractionEvaluation()))).containsExactlyInAnyOrderElementsOf(expectedUnusedConfig)
        verify { cypInteractionCuration wasNot Called }
        verify { qtProlongingCuration wasNot Called }
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