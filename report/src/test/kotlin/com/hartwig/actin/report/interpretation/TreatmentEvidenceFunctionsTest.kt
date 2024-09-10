package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TREATMENT = "treatment"

class TreatmentEvidenceFunctionsTest {

    private val onLabelCategoryLevelA = createTreatmentEvidence(treatment = "onLabel category level", isCategoryEvent = true)
    private val offLabelCategoryLevelA = onLabelCategoryLevelA.copy(treatment = "offLabel category level", onLabel = false)
    private val onLabelCategoryLevelB = onLabelCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)
    private val onLabelNonCategoryLevelA = onLabelCategoryLevelA.copy(treatment = "onLabel non-category level", isCategoryEvent = false)
    private val onLabelNonCategoryLevelB = onLabelNonCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)

    @Test
    fun `Should correctly filter treatment evidence by onLabel property`() {
        val evidence = setOf(offLabelCategoryLevelA, onLabelCategoryLevelA)
        val resultOnLabel = TreatmentEvidenceFunctions.filterOnLabel(evidence, true)
        val resultOffLabel = TreatmentEvidenceFunctions.filterOnLabel(evidence, false)

        val expectedOnLabel = setOf(onLabelCategoryLevelA)
        val expectedOffLabel = setOf(offLabelCategoryLevelA)

        assertThat(resultOffLabel).containsExactlyElementsOf(expectedOffLabel)
        assertThat(resultOnLabel).containsExactlyElementsOf(expectedOnLabel)
    }

    @Test
    fun `Should correctly group treatment evidence based on all properties except level of evidence`() {
        val offLabelB = offLabelCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)
        val otherOnLabelA = onLabelCategoryLevelA.copy(treatment = "other")

        val evidenceSet = setOf(onLabelCategoryLevelA, onLabelCategoryLevelB, offLabelCategoryLevelA, offLabelB, otherOnLabelA)
        val result = TreatmentEvidenceFunctions.groupTreatmentsIgnoringEvidenceLevel(evidenceSet)
        val expected = mapOf(
            createGroupingKey("onLabel category level", true) to listOf(onLabelCategoryLevelA, onLabelCategoryLevelB),
            createGroupingKey("offLabel category level", false) to listOf(offLabelCategoryLevelA, offLabelB),
            createGroupingKey("other", true) to listOf(otherOnLabelA),
        )
        assertThat(result).containsAllEntriesOf(expected)
    }

    @Test
    fun `Should only create clinical details for category evidence with highest level`() {
        assertClinicalDetails(
            listOf(onLabelCategoryLevelA, onLabelCategoryLevelB),
            listOf(createClinicalDetails(onLabelCategoryLevelA, levelA = true))
        )
    }

    @Test
    fun `Should only create clinical details for non-category evidence with highest level`() {
        assertClinicalDetails(
            listOf(onLabelNonCategoryLevelA, onLabelNonCategoryLevelB),
            listOf(createClinicalDetails(onLabelNonCategoryLevelA, levelA = true))
        )
    }

    @Test
    fun `Should filter out non-category evidence if there is category evidence with higher or equal level for same variant`() {
        assertClinicalDetails(
            listOf(onLabelCategoryLevelA, onLabelNonCategoryLevelA),
            listOf(createClinicalDetails(onLabelNonCategoryLevelA, levelA = true))
        )
    }

    @Test
    fun `Should filter out all details except of highest level of evidence`() {
        assertClinicalDetails(
            listOf(onLabelCategoryLevelA, onLabelCategoryLevelB),
            listOf(createClinicalDetails(onLabelCategoryLevelA, levelA = true))
        )
    }

    @Test
    fun `Should generate correct triples of treatment name, string of cancer types with dates, and isResistant Boolean`() {
        val date = LocalDate.of(2024, 1, 1)
        val cancerType = ApplicableCancerType("Cancer type 1", emptySet())
        val treatmentEvidence = createTreatmentEvidence(date = date, applicableCancerType = cancerType)
        val evidence = listOf(
            treatmentEvidence,
            treatmentEvidence.copy(date = date.minusYears(1), applicableCancerType = cancerType.copy("Cancer type 2")),
            treatmentEvidence.copy(treatment = "other treatment", direction = EvidenceDirection(isResistant = true))
        )
        val result = TreatmentEvidenceFunctions.generateEvidenceCellContents(evidence)
        val expected = listOf(
            Triple(TREATMENT, "Cancer type 1 (2024), Cancer type 2 (2023)", false),
            Triple("other treatment", "Cancer type 1 (2024)", true)
        )
        assertThat(expected).containsExactlyElementsOf(result)
    }

    private fun createTreatmentEvidence(
        treatment: String = TREATMENT,
        onLabel: Boolean = true,
        evidenceLevel: EvidenceLevel = EvidenceLevel.A,
        date: LocalDate = LocalDate.EPOCH,
        isCategoryEvent: Boolean = true,
        applicableCancerType: ApplicableCancerType = ApplicableCancerType("", emptySet())
    ): TreatmentEvidence {
        return TreatmentEvidence(
            treatment,
            evidenceLevel,
            onLabel,
            EvidenceDirection(),
            date,
            "",
            isCategoryEvent,
            "sourceEvent",
            applicableCancerType
        )
    }

    private fun createGroupingKey(treatment: String, onLabel: Boolean): TreatmentEvidenceGroupingKey {
        return TreatmentEvidenceGroupingKey(
            treatment,
            onLabel,
            EvidenceDirection(),
            true,
            "sourceEvent",
            ApplicableCancerType("", emptySet())
        )
    }

    private fun createClinicalDetails(
        evidence: TreatmentEvidence, levelA: Boolean = false, levelB: Boolean = false, levelC: Boolean = false, levelD: Boolean = false
    ): ClinicalDetails {
        return ClinicalDetails(evidence, levelA, levelB, levelC, levelD)
    }

    private fun assertClinicalDetails(inputList: List<TreatmentEvidence>, expectedResult: List<ClinicalDetails>) {
        assertThat(TreatmentEvidenceFunctions.treatmentEvidenceToClinicalDetails(inputList))
            .containsExactlyElementsOf(expectedResult)
    }
}