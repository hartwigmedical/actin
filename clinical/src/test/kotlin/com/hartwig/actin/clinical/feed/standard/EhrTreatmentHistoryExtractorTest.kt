package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentStage
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val TREATMENT_NAME = "treatmentName"

private const val MODIFICATION_NAME = "modificationName"

class EhrTreatmentHistoryExtractorTest {

    private val treatmentCurationDatabase = mockk<CurationDatabase<TreatmentHistoryEntryConfig>>()
    private val nonOncologicalHistoryCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = EhrTreatmentHistoryExtractor(treatmentCurationDatabase, nonOncologicalHistoryCuration)
    private val treatment = DrugTreatment("drug", drugs = emptySet())
    private val treatmentHistoryEntryConfig = TreatmentHistoryEntryConfig(
        TREATMENT_NAME,
        false,
        TreatmentHistoryEntry(
            treatments = setOf(treatment),
            treatmentHistoryDetails = TreatmentHistoryDetails(
                bodyLocations = setOf("bone"),
                bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                maintenanceTreatment = TreatmentStage(treatment = treatment, cycles = 1, startYear = 2024, startMonth = 2),
            ),
            trialAcronym = "trialAcronym",
        )
    )
    private val minimalTreatmentHistory = EhrTreatmentHistory(
        treatmentName = TREATMENT_NAME,
        administeredCycles = 1,
        intendedCycles = 1,
        startDate = LocalDate.of(2024, 2, 23),
        administeredInStudy = false,
    )
    private val minimalEhrPatientRecord = createEhrPatientRecord()

    @Test
    fun `Should filter treatment history entry and warn when no curation for treatment name`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns emptySet()
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = listOf(
                    minimalTreatmentHistory
                )
            )
        )
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                message = "Could not find treatment history config for input 'treatmentName'",
                patientId = "aGFzaGVkSWQ=",
                category = CurationCategory.ONCOLOGICAL_HISTORY,
                feedInput = TREATMENT_NAME
            )
        )
    }

    @Test
    fun `Should extract treatment with modifications using curated treatment`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(treatmentHistoryEntryConfig)
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(treatmentHistoryEntryConfig)
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = listOf(
                    minimalTreatmentHistory.copy(
                        stopReason = "TOXICITY",
                        endDate = LocalDate.of(2024, 2, 27),
                        response = "COMPLETE_RESPONSE",
                        modifications = listOf(
                            EhrTreatmentModification(
                                name = MODIFICATION_NAME, administeredCycles = 2, date = LocalDate.of(2024, 2, 23)
                            )
                        ),
                    )
                )
            )
        )
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).isEqualTo(
            listOf(
                TreatmentHistoryEntry(
                    startYear = 2024,
                    startMonth = 2,
                    treatments = treatmentHistoryEntryConfig.curated!!.treatments,
                    treatmentHistoryDetails = TreatmentHistoryDetails(
                        stopYear = 2024,
                        stopMonth = 2,
                        stopReason = StopReason.TOXICITY,
                        bestResponse = TreatmentResponse.COMPLETE_RESPONSE,
                        bodyLocations = setOf("bone"),
                        bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                        switchToTreatments = listOf(TreatmentStage(treatment = treatment, cycles = 2, startYear = 2024, startMonth = 2)),
                        cycles = 1,
                        maintenanceTreatment = TreatmentStage(treatment = treatment, cycles = 1, startYear = 2024, startMonth = 2),
                    ),
                    isTrial = false,
                    trialAcronym = "trialAcronym"
                )
            )
        )
    }

    @Test
    fun `Should filter treatment modification and warn when no curation for modification name`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(
            treatmentHistoryEntryConfig
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns emptySet()
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = listOf(
                    minimalTreatmentHistory.copy(
                        modifications = listOf(
                            EhrTreatmentModification(
                                name = MODIFICATION_NAME, administeredCycles = 1, date = LocalDate.of(2024, 2, 23)
                            )
                        )
                    )
                )
            )
        )
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                message = "Could not find treatment history config for input '$MODIFICATION_NAME'",
                patientId = "aGFzaGVkSWQ=",
                category = CurationCategory.ONCOLOGICAL_HISTORY,
                feedInput = MODIFICATION_NAME
            )
        )
    }
}