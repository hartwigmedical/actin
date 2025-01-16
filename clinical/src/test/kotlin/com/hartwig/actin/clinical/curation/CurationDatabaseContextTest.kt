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
        val expectedUnusedConfig = IntRange(0, 13).map {
            CurationConfigValidationError(NOT_IMPORTANT, NOT_IMPORTANT, NOT_IMPORTANT, it.toString(), NOT_IMPORTANT)
        }
        val context = CurationDatabaseContext(
            primaryTumorCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[0]),
            treatmentHistoryEntryCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[1]),
            secondPrimaryCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[2]),
            lesionLocationCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[3]),
            comorbidityCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[4]),
            ecgCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[5]),
            infectionCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[6]),
            periodBetweenUnitCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[7]),
            molecularTestIhcCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[8]),
            molecularTestPdl1Curation = curationDatabaseWithUnusedConfig(expectedUnusedConfig[9]),
            sequencingTestCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[10]),
            medicationNameCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[11]),
            medicationDosageCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[12]),
            administrationRouteTranslation = mockk(),
            laboratoryTranslation = mockk(),
            toxicityTranslation = mockk(),
            bloodTransfusionTranslation = mockk(),
            dosageUnitTranslation = mockk(),
            surgeryNameCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[13])
        )
        assertThat(context.validate()).containsExactlyElementsOf(expectedUnusedConfig)
    }

    @Test
    fun `Should combine all unused configs in curation databases, except blood transfusions`() {
        val expectedUnusedConfig = IntRange(0, 17).map { UnusedCurationConfig(CurationCategory.TOXICITY.name, it.toString()) }
        val bloodTransfusionTranslation = mockk<TranslationDatabase<String>>()
        val context = CurationDatabaseContext(
            primaryTumorCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[0]),
            treatmentHistoryEntryCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[1]),
            secondPrimaryCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[2]),
            lesionLocationCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[3]),
            comorbidityCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[4]),
            ecgCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[5]),
            infectionCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[6]),
            periodBetweenUnitCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[7]),
            molecularTestIhcCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[8]),
            molecularTestPdl1Curation = curationDatabaseWithUnusedConfig(expectedUnusedConfig[9]),
            sequencingTestCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[10]),
            medicationNameCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[11]),
            medicationDosageCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[12]),
            administrationRouteTranslation = translationDatabaseWithUnusedConfig(expectedUnusedConfig[13]),
            laboratoryTranslation = translationDatabaseWithUnusedConfig(expectedUnusedConfig[14]),
            toxicityTranslation = translationDatabaseWithUnusedConfig(expectedUnusedConfig[15]),
            bloodTransfusionTranslation = bloodTransfusionTranslation,
            dosageUnitTranslation = translationDatabaseWithUnusedConfig(expectedUnusedConfig[16]),
            surgeryNameCuration = curationDatabaseWithUnusedConfig(expectedUnusedConfig[17])
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