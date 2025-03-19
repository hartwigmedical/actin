package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceApprovalPhase
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentRankerTest {

    @Test
    fun `Should add scoring for guideline, benefit, on-label, exact`() {
        val ranker = TreatmentRanker()
        val patientRecord = patientRecord(
            createVariant(
                isOnLabel = true,
                isCategoryEvent = false,
                approvalStage = EvidenceApprovalPhase.PHASE_I,
                hasBenefit = true,
                treatment = "treatment1"
            ),
            createVariant(
                isOnLabel = true,
                isCategoryEvent = false,
                approvalStage = EvidenceApprovalPhase.PRECLINICAL,
                hasBenefit = true,
                treatment = "treatment2"
            ),
            createVariant(
                isOnLabel = true,
                isCategoryEvent = true,
                approvalStage = EvidenceApprovalPhase.PHASE_III,
                hasBenefit = true,
                treatment = "treatment3"
            ),
            createVariant(
                isOnLabel = true,
                isCategoryEvent = false,
                approvalStage = EvidenceApprovalPhase.GUIDELINE,
                hasBenefit = true,
                treatment = "treatment4"
            )
        )
        val rank = ranker.rank(patientRecord).sorted()
        assertThat(rank[0].treatment).isEqualTo("treatment4")
        assertThat(rank[0].score).isEqualTo(1900.0)
        assertThat(rank[1].treatment).isEqualTo("treatment3")
        assertThat(rank[1].score).isEqualTo(1710.0)
        assertThat(rank[2].treatment).isEqualTo("treatment1")
        assertThat(rank[2].score).isEqualTo(700.0)
        assertThat(rank[3].treatment).isEqualTo("treatment2")
        assertThat(rank[3].score).isEqualTo(200.0)
    }

    private fun patientRecord(
        vararg variants: Variant
    ) = TestPatientFactory.createProperTestPatientRecord().copy(
        molecularHistory = MolecularHistory(
            listOf(
                TestMolecularFactory.createProperTestOrangeRecord().copy(
                    drivers = Drivers(
                        variants = variants.toList()
                    ),
                    characteristics = MolecularCharacteristics()
                )
            )
        )
    )

    private fun createVariant(
        isOnLabel: Boolean, isCategoryEvent: Boolean, approvalStage: EvidenceApprovalPhase, hasBenefit: Boolean, treatment: String
    ) = TestVariantFactory.createMinimal().copy(
        evidence = TestClinicalEvidenceFactory.createEmpty().copy(
            treatmentEvidence = setOf(
                TestTreatmentEvidenceFactory.create(
                    treatment = treatment,
                    sourceEvent = "BRAF V600E",
                    isOnLabel = isOnLabel,
                    isCategoryEvent = isCategoryEvent,
                    evidenceLevelDetails = approvalStage,
                    evidenceDirection = EvidenceDirection(hasBenefit, hasBenefit, !hasBenefit, true),
                    evidenceLevel = EvidenceLevel.A
                )
            )
        )
    )

}