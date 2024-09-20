package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.serve.datamodel.EvidenceLevelDetails
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TREATMENT = "treatment"

class TreatmentEvidenceFunctionsTest {

    private val onLabelCategoryLevelA = createTreatmentEvidence(treatment = "onLabel category level", isCategoryEvent = true)
    private val offLabelCategoryLevelA = onLabelCategoryLevelA.copy(treatment = "offLabel category level", onLabel = false)
    private val offLabelCategoryLevelB = offLabelCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)
    private val onLabelCategoryLevelB = onLabelCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)
    private val onLabelNonCategoryLevelA = onLabelCategoryLevelA.copy(treatment = "onLabel non-category level", isCategoryEvent = false)
    private val onLabelNonCategoryLevelB = onLabelNonCategoryLevelA.copy(evidenceLevel = EvidenceLevel.B)

    @Test
    fun `Should correctly map each treatment to its highest evidence level`() {
        val evidence = listOf(onLabelCategoryLevelA, onLabelCategoryLevelB, onLabelNonCategoryLevelB)
        val expected = mapOf(onLabelCategoryLevelA.treatment to EvidenceLevel.A, onLabelNonCategoryLevelB.treatment to EvidenceLevel.B)
        val result = TreatmentEvidenceFunctions.getHighestEvidenceLevelPerTreatment(evidence)
        assertThat(result).containsExactlyEntriesOf(expected)
    }

    @Test
    fun `Should filter out off label evidence if there is on label evidence of the same treatment with the same or higher evidence`() {
        val evidence = listOf(
            onLabelCategoryLevelA.copy("treatment"),
            offLabelCategoryLevelA.copy("treatment"),
            offLabelCategoryLevelB.copy("treatment")
        )
        val onLabel = evidence.filter { it.onLabel }
        val onLabelHighestEvidencePerTreatment = TreatmentEvidenceFunctions.getHighestEvidenceLevelPerTreatment(onLabel)
        val result = TreatmentEvidenceFunctions.filterOffLabelEvidence(evidence, onLabelHighestEvidencePerTreatment)
        assertThat(result).containsExactlyElementsOf(emptySet())
    }

    @Test
    fun `Should correctly filter treatment evidence by onLabel property`() {
        val evidence = setOf(offLabelCategoryLevelA, onLabelCategoryLevelA)
        val resultOnLabel = TreatmentEvidenceFunctions.filterTreatmentEvidence(evidence, true)
        val resultOffLabel = TreatmentEvidenceFunctions.filterTreatmentEvidence(evidence, false)
        val resultAllLabels = TreatmentEvidenceFunctions.filterTreatmentEvidence(evidence, null)

        val expectedOnLabel = setOf(onLabelCategoryLevelA)
        val expectedOffLabel = setOf(offLabelCategoryLevelA)
        val expectedAllLabels = setOf(onLabelCategoryLevelA, offLabelCategoryLevelA)

        assertThat(resultOffLabel).containsExactlyElementsOf(expectedOffLabel)
        assertThat(resultOnLabel).containsExactlyElementsOf(expectedOnLabel)
        assertThat(resultAllLabels).containsExactlyElementsOf(expectedAllLabels)
    }

    @Test
    fun `Should correctly filter treatment without direction hasBenefit or isResistant`() {
        val noBenefitEvidence =
            createTreatmentEvidence("noBenefit", evidenceLevel = EvidenceLevel.A, direction = EvidenceDirection())
        val evidence = setOf(onLabelCategoryLevelA, noBenefitEvidence)
        val result = TreatmentEvidenceFunctions.onlyIncludeBenefitAndResistanceEvidence(evidence)
        val expected = setOf(onLabelCategoryLevelA)

        assertThat(result).containsExactlyElementsOf(expected)
    }


    @Test
    fun `Should correctly filter treatment with preclinical level D evidence`() {
        val preclinicalEvidence =
            createTreatmentEvidence("preclinical", evidenceLevel = EvidenceLevel.D, evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL)
        val evidence = setOf(onLabelCategoryLevelA, preclinicalEvidence)
        val result = TreatmentEvidenceFunctions.filterPreClinicalEvidence(evidence)
        val expected = setOf(onLabelCategoryLevelA)

        assertThat(result).containsExactlyElementsOf(expected)
    }

    @Test
    fun `Should correctly filter treatment with level D evidence if level A or B evidence present for same source event`() {
        val levelDEvidence = createTreatmentEvidence("level D", evidenceLevel = EvidenceLevel.D, sourceEvent = "event 1")
        val levelAEvidence = createTreatmentEvidence("level A", evidenceLevel = EvidenceLevel.A, sourceEvent = "event 1")
        val otherLevelDEvidence = createTreatmentEvidence("level D2", evidenceLevel = EvidenceLevel.D, sourceEvent = "event 2")

        val evidence = setOf(levelDEvidence, levelAEvidence, otherLevelDEvidence)
        val result = TreatmentEvidenceFunctions.filterLevelDWhenAorBExists(evidence)
        val expected = setOf(levelAEvidence, otherLevelDEvidence)

        assertThat(result).containsExactlyElementsOf(expected)
    }

    @Test
    fun `Should correctly group treatment evidence by combination of treatment name and applicable cancer type`() {
        val treatment1 = createTreatmentEvidence(
            "treatment 1",
            evidenceLevel = EvidenceLevel.A,
            applicableCancerType = ApplicableCancerType("cancer type 1", emptySet())
        )
        val otherTreatment1 = treatment1.copy(evidenceLevel = EvidenceLevel.B)
        val treatment2 = treatment1.copy("treatment 2")

        val evidence = listOf(treatment1, otherTreatment1, treatment2)
        val result = TreatmentEvidenceFunctions.groupByTreatmentAndCancerType(evidence)
        val expected = mapOf(
            Pair(treatment1.treatment, treatment1.applicableCancerType.cancerType) to listOf(treatment1, otherTreatment1),
            Pair(treatment2.treatment, treatment2.applicableCancerType.cancerType) to listOf(treatment2)
        )
        assertThat(result).containsExactlyEntriesOf(expected)
    }

    @Test
    fun `Should keep only highest evidence for specific treatment-cancertype combination and should prioritize non-categorical`() {
        val levelACategory = onLabelCategoryLevelA.copy(
            "treatment",
            sourceEvent = "category event",
            applicableCancerType = ApplicableCancerType("cancer 1", emptySet())
        )
        val levelBCategory = levelACategory.copy(evidenceLevel = EvidenceLevel.B)
        val levelBNonCategory = levelACategory.copy(sourceEvent = "nonCat event", isCategoryEvent = false, evidenceLevel = EvidenceLevel.B)
        val levelCNonCategory = levelBNonCategory.copy(evidenceLevel = EvidenceLevel.C)

        val evidence = setOf(levelACategory, levelBCategory, levelBNonCategory, levelCNonCategory)
        val result = TreatmentEvidenceFunctions.prioritizeNonCategoryEvidence(evidence)
        val expected = setOf(levelACategory, levelBNonCategory)
        assertThat(result).containsExactlyElementsOf(expected)
    }

    @Test
    fun `Should generate TreatmentEvidenceContent objects with treatment name, cancer types with dates, and isResistant Boolean`() {
        val date = LocalDate.of(2024, 1, 1)
        val cancerType = ApplicableCancerType("Cancer type 1", emptySet())
        val treatmentEvidence = createTreatmentEvidence(date = date, applicableCancerType = cancerType)
        val evidence = listOf(
            treatmentEvidence,
            treatmentEvidence.copy(ckbEntryDate = date.minusYears(1), applicableCancerType = cancerType.copy("Cancer type 2")),
            treatmentEvidence.copy(treatment = "other treatment", direction = EvidenceDirection(isResistant = true))
        )
        val result = TreatmentEvidenceFunctions.generateEvidenceCellContents(evidence)
        val expected = listOf(
            TreatmentEvidenceFunctions.TreatmentEvidenceContent(TREATMENT, "Cancer type 1 (2024), Cancer type 2 (2023)", false),
            TreatmentEvidenceFunctions.TreatmentEvidenceContent("other treatment", "Cancer type 1 (2024)", true)
        )
        assertThat(expected).isEqualTo(result)
    }

    @Test
    fun `Should correctly sort with highest level of evidence first, then by non-category first, then by onLabel first`() {
        val nonCategoryD = onLabelNonCategoryLevelA.copy(evidenceLevel = EvidenceLevel.D)
        val evidence = listOf(
            offLabelCategoryLevelA,
            onLabelCategoryLevelA,
            onLabelCategoryLevelB,
            onLabelNonCategoryLevelB,
            nonCategoryD,
            onLabelNonCategoryLevelA
        )

        val result = TreatmentEvidenceFunctions.sortTreatmentEvidence(evidence)
        val expected = setOf(
            onLabelNonCategoryLevelA,
            onLabelCategoryLevelA,
            offLabelCategoryLevelA,
            onLabelNonCategoryLevelB,
            onLabelCategoryLevelB,
            nonCategoryD
        )

        assertThat(result).containsExactlyElementsOf(expected)
    }

    private fun createTreatmentEvidence(
        treatment: String = TREATMENT,
        onLabel: Boolean = true,
        direction: EvidenceDirection = EvidenceDirection(hasBenefit = true),
        evidenceLevel: EvidenceLevel = EvidenceLevel.A,
        date: LocalDate = LocalDate.EPOCH,
        isCategoryEvent: Boolean = true,
        sourceEvent: String = "sourceEvent",
        evidenceLevelDetails: EvidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
        applicableCancerType: ApplicableCancerType = ApplicableCancerType("", emptySet())
    ): TreatmentEvidence {
        return TreatmentEvidence(
            treatment,
            evidenceLevel,
            onLabel,
            direction,
            date,
            "",
            2021,
            isCategoryEvent,
            sourceEvent,
            evidenceLevelDetails,
            applicableCancerType
        )
    }
}