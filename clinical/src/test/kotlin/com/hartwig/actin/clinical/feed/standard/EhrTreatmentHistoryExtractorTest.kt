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

private const val PREVIOUS_CONDITION = "previous_condition"

private val PRIOR_CONDITION = EhrPriorOtherCondition(
    name = PREVIOUS_CONDITION,
    startDate = LocalDate.of(2024, 2, 27),
    endDate = LocalDate.of(2024, 2, 28),
)

private val TREATMENT = DrugTreatment("drug", drugs = emptySet())

private val TREATMENT_HISTORY = EhrTreatmentHistory(
    treatmentName = TREATMENT_NAME,
    administeredCycles = 1,
    intendedCycles = 1,
    startDate = LocalDate.of(2024, 2, 23),
    administeredInStudy = false,
)

private val TREATMENT_HISTORY_ENTRY_CONFIG = TreatmentHistoryEntryConfig(
    TREATMENT_NAME,
    false,
    TreatmentHistoryEntry(
        treatments = setOf(TREATMENT),
        treatmentHistoryDetails = TreatmentHistoryDetails(
            bodyLocations = setOf("bone"),
            bodyLocationCategories = setOf(BodyLocationCategory.BONE),
            maintenanceTreatment = TreatmentStage(treatment = TREATMENT, cycles = 1, startYear = 2024, startMonth = 2),
        ),
        trialAcronym = "trialAcronym",
    )
)

class EhrTreatmentHistoryExtractorTest {

    private val treatmentCurationDatabase = mockk<CurationDatabase<TreatmentHistoryEntryConfig>>()
    private val nonOncologicalHistoryCuration = mockk<CurationDatabase<NonOncologicalHistoryConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = EhrTreatmentHistoryExtractor(treatmentCurationDatabase, nonOncologicalHistoryCuration)
    private val minimalEhrPatientRecord = createEhrPatientRecord()

    @Test
    fun `Should filter treatment history entry and warn when no curation for treatment name`() {
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns emptySet()
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = listOf(
                    TREATMENT_HISTORY
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
        every { treatmentCurationDatabase.find(TREATMENT_NAME) } returns setOf(TREATMENT_HISTORY_ENTRY_CONFIG)
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns setOf(TREATMENT_HISTORY_ENTRY_CONFIG)
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = listOf(
                    TREATMENT_HISTORY.copy(
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
                    treatments = TREATMENT_HISTORY_ENTRY_CONFIG.curated!!.treatments,
                    treatmentHistoryDetails = TreatmentHistoryDetails(
                        stopYear = 2024,
                        stopMonth = 2,
                        stopReason = StopReason.TOXICITY,
                        bestResponse = TreatmentResponse.COMPLETE_RESPONSE,
                        bodyLocations = setOf("bone"),
                        bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                        switchToTreatments = listOf(TreatmentStage(treatment = TREATMENT, cycles = 2, startYear = 2024, startMonth = 2)),
                        cycles = 1,
                        maintenanceTreatment = TreatmentStage(treatment = TREATMENT, cycles = 1, startYear = 2024, startMonth = 2),
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
            TREATMENT_HISTORY_ENTRY_CONFIG
        )
        every { treatmentCurationDatabase.find(MODIFICATION_NAME) } returns emptySet()
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = listOf(
                    TREATMENT_HISTORY.copy(
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

    @Test
    fun `Should consider previous conditions as treatment history if not present in non-oncological history curation and is present in oncological history curation`() {
        every { treatmentCurationDatabase.find(PREVIOUS_CONDITION) } returns setOf(TREATMENT_HISTORY_ENTRY_CONFIG)
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(
                    PRIOR_CONDITION
                )
            )
        )
        assertThat(result.extracted).containsExactly(
            TreatmentHistoryEntry(
                startYear = 2024,
                startMonth = 2,
                treatments = TREATMENT_HISTORY_ENTRY_CONFIG.curated!!.treatments,
                treatmentHistoryDetails = TreatmentHistoryDetails(
                    stopYear = 2024,
                    stopMonth = 2,
                    stopReason = null,
                    bestResponse = null,
                    bodyLocations = setOf("bone"),
                    bodyLocationCategories = setOf(BodyLocationCategory.BONE),
                    switchToTreatments = null,
                    cycles = null,
                    maintenanceTreatment = TreatmentStage(treatment = TREATMENT, cycles = 1, startYear = 2024, startMonth = 2),
                ),
                isTrial = false,
                trialAcronym = "trialAcronym"
            )
        )
    }

    @Test
    fun `Should skip previous conditions present in the non-oncological curation`() {
        every { nonOncologicalHistoryCuration.find(PREVIOUS_CONDITION) } returns setOf(
            NonOncologicalHistoryConfig(
                input = PREVIOUS_CONDITION,
                ignore = false
            )
        )
        val result = extractor.extract(
            minimalEhrPatientRecord.copy(
                treatmentHistory = emptyList(),
                priorOtherConditions = listOf(PRIOR_CONDITION)
            )
        )
        assertThat(result.extracted).isEmpty()
    }
}