package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.serve.datamodel.EvidenceLevelDetails
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ClinicalDetailsFunctionsTest {

    private val sourceA = "Source event A"
    private val sourceB = "Source event B"
    private val detailsA1 = createDetails(createTreatmentEvidence(treatment = "A1", sourceA), levelA = true)
    private val detailsA2 = createDetails(createTreatmentEvidence(treatment = "A2", sourceA), levelA = true)
    private val detailsB = createDetails(createTreatmentEvidence(treatment = "B", sourceB), levelB = true)

    @Test
    fun `Should correctly group clinical details by source event`() {
        val result = ClinicalDetailsFunctions.groupBySourceEvent(setOf(detailsA1, detailsB, detailsA2))
        val expected = mapOf(
            sourceA to listOf(detailsA1, detailsA2),
            sourceB to listOf(detailsB)
        )
        assertThat(result).containsExactlyEntriesOf(expected)
    }

    @Test
    fun `Should correctly separate clinical details into per level of evidence lists`() {
        val detailsC = createDetails(createTreatmentEvidence(treatment = "C"), levelC = true)

        val details = listOf(detailsA1, detailsA2, detailsB, detailsC)

        val result = ClinicalDetailsFunctions.mapTreatmentEvidencesToLevel(details)
        val expected = listOf(
            listOf(detailsA1.treatmentEvidence, detailsA2.treatmentEvidence),
            listOf(detailsB.treatmentEvidence),
            listOf(detailsC.treatmentEvidence),
            emptyList()
        )

        assertThat(result).containsExactlyElementsOf(expected)
    }

    private fun createTreatmentEvidence(treatment: String = "treatment", sourceEvent: String = ""): TreatmentEvidence {
        return TreatmentEvidence(
            treatment,
            EvidenceLevel.A,
            true,
            EvidenceDirection(),
            LocalDate.EPOCH,
            "",
            true,
            sourceEvent,
            EvidenceLevelDetails.CLINICAL_STUDY,
            ApplicableCancerType("", emptySet())
        )
    }

    private fun createDetails(
        treatmentEvidence: TreatmentEvidence,
        levelA: Boolean = false,
        levelB: Boolean = false,
        levelC: Boolean = false,
        levelD: Boolean = false
    ): ClinicalDetails {
        val level = when {
            levelA -> EvidenceLevel.A
            levelB -> EvidenceLevel.B
            levelC -> EvidenceLevel.C
            else -> EvidenceLevel.D
        }
        val evidence = treatmentEvidence.copy(evidenceLevel = level)
        return ClinicalDetails(evidence, levelA, levelB, levelC, levelD)
    }
}