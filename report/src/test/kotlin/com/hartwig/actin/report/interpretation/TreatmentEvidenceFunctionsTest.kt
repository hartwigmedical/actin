package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class TreatmentEvidenceFunctionsTest {

    private val onLabelCategoryLevelA = createTreatmentEvidence(treatment = "onLabel category level", isCategoryVariant = true)
    private val offLabelCategoryLevelA = onLabelCategoryLevelA.copy(treatment = "offLabel category level", onLabel = false)
    private val onLabelCategoryLevelB = onLabelCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)
    private val onLabelNonCategoryLevelA = onLabelCategoryLevelA.copy(treatment = "onLabel non-category level", isCategoryVariant = false)
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
    fun `Should correctly filter treatment preclinical level D evidence`() {
        val preclinicalEvidence = createTreatmentEvidence("preclinical", evidenceLevel = EvidenceLevel.D, approvalStatus = "PRECLINICAL")
        val evidence = setOf(onLabelCategoryLevelA, preclinicalEvidence)
        val result = TreatmentEvidenceFunctions.filterOutPreClinicalEvidence(evidence)
        val expected = setOf(onLabelCategoryLevelA)

        assertThat(result).containsExactlyElementsOf(expected)
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

    private fun createTreatmentEvidence(
        treatment: String = "treatment",
        onLabel: Boolean = true,
        evidenceLevel: EvidenceLevel = EvidenceLevel.A,
        isCategoryVariant: Boolean = true,
        approvalStatus: String = "CLINICAL_STUDY"
    ): TreatmentEvidence {
        return TreatmentEvidence(
            treatment,
            evidenceLevel,
            onLabel,
            EvidenceDirection(),
            LocalDate.of(2024, 9, 5),
            "",
            isCategoryVariant,
            "sourceEvent",
            approvalStatus,
            ApplicableCancerType("", emptySet())
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