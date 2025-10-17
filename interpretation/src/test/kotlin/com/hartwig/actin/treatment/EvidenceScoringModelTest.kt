package com.hartwig.actin.treatment

import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceType
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceScoringModelTest {

    private val scorer = EvidenceScoringModel(createScoringConfig())

    @Test
    fun `Should score direct evidence when category`() {
        val treatmentEvidence = treatmentEvidence(isCategoryEvent = true, isIndirect = false)

        val evidenceScore = scorer.score(treatmentEvidence)

        assertThat(evidenceScore.scoringMatch.variantMatch).isEqualTo(VariantMatch.CATEGORY)
    }

    @Test
    fun `Should score direct evidence with exact match when not category`() {
        val treatmentEvidence = treatmentEvidence(isCategoryEvent = false, isIndirect = false)

        val evidenceScore = scorer.score(treatmentEvidence)

        assertThat(evidenceScore.scoringMatch.variantMatch).isEqualTo(VariantMatch.EXACT)
    }

    @Test
    fun `Should score indirect evidence as functional effect match`() {
        val treatmentEvidence = treatmentEvidence(isCategoryEvent = false, isIndirect = true)

        val evidenceScore = scorer.score(treatmentEvidence)

        assertThat(evidenceScore.scoringMatch.variantMatch).isEqualTo(VariantMatch.FUNCTIONAL_EFFECT)
    }


    private fun treatmentEvidence(isCategoryEvent: Boolean, isIndirect: Boolean) = TestTreatmentEvidenceFactory.create(
        treatment = "treatment",
        sourceEvent = "Event",
        cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
        evidenceType = if (isCategoryEvent) EvidenceType.ANY_MUTATION else EvidenceType.HOTSPOT_MUTATION,
        evidenceLevelDetails = EvidenceLevelDetails.FDA_APPROVED,
        evidenceDirection = EvidenceDirection(hasPositiveResponse = true, hasBenefit = true, isResistant = false, isCertain = true),
        evidenceLevel = EvidenceLevel.A,
        isIndirect = isIndirect
    )
}
