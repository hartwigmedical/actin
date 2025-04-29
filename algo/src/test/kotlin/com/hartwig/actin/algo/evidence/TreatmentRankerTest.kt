package com.hartwig.actin.algo.evidence

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentRankerTest {

    @Test
    fun `Should add scoring for guideline, benefit, on-label, exact`() {
        val ranker = TreatmentRanker()
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.PHASE_I,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            ),
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.PRECLINICAL,
                    hasBenefit = true,
                    treatment = "treatment2",
                )
            ),
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = true,
                    approvalStage = EvidenceLevelDetails.PHASE_III,
                    hasBenefit = true,
                    treatment = "treatment3",
                )
            ),
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = true,
                    treatment = "treatment4",
                )
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

    @Test
    fun `Should sum scores for a single treatment on different events`() {
        val ranker = TreatmentRanker()
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = true,
                    treatment = "treatment1"
                ), treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                    event = "BRAF mut"
                )
            )
        )
        val rank = ranker.rank(patientRecord).sorted()
        assertThat(rank[0].treatment).isEqualTo("treatment1")
        assertThat(rank[0].score).isEqualTo(3900.0)
    }

    @Test
    fun `Should sum scores for a single treatment on different tumor types`() {
        val ranker = TreatmentRanker()
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1"
                ),
                treatmentEvidence(
                    isOnLabel = false,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            )
        )
        val rank = ranker.rank(patientRecord).sorted()
        assertThat(rank[0].treatment).isEqualTo("treatment1")
        assertThat(rank[0].score).isEqualTo(3700.0)
    }

    @Test
    fun `Should diminish returns single treatment, same tumor type, variant`() {
        val ranker = TreatmentRanker()
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = true,
                    treatment = "treatment1"
                ),
                treatmentEvidence(
                    isOnLabel = true,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            )
        )
        val rank = ranker.rank(patientRecord).sorted()
        assertThat(rank[0].treatment).isEqualTo("treatment1")
        assertThat(rank[0].score).isEqualTo(2950.0)
    }


    private fun patientRecord(
        vararg variants: Variant
    ) = TestPatientFactory.createProperTestPatientRecord().copy(
        molecularHistory = MolecularHistory(
            listOf(
                TestMolecularFactory.createExhaustiveTestMolecularRecord().copy(
                    drivers = Drivers(
                        variants = variants.toList(),
                        copyNumbers = emptyList(),
                        homozygousDisruptions = emptyList(),
                        disruptions = emptyList(),
                        fusions = emptyList(),
                        viruses = emptyList()
                    ),
                    characteristics = MolecularCharacteristics(
                        homologousRecombination = null,
                        purity = null,
                        ploidy = null,
                        predictedTumorOrigin = null,
                        microsatelliteStability = null,
                        tumorMutationalBurden = null,
                        tumorMutationalLoad = null
                    )
                )
            )
        )
    )

    private fun createVariant(vararg treatmentEvidence: TreatmentEvidence) = TestVariantFactory.createMinimal().copy(
        evidence = TestClinicalEvidenceFactory.createEmpty().copy(
            treatmentEvidence = treatmentEvidence.toSet()
        )
    )

    private fun treatmentEvidence(
        treatment: String,
        isOnLabel: Boolean,
        isCategoryEvent: Boolean,
        approvalStage: EvidenceLevelDetails,
        hasBenefit: Boolean,
        event: String = "BRAF V600E"
    ) = TestTreatmentEvidenceFactory.create(
        treatment = treatment,
        sourceEvent = event,
        isOnLabel = isOnLabel,
        isCategoryEvent = isCategoryEvent,
        evidenceLevelDetails = approvalStage,
        evidenceDirection = EvidenceDirection(hasBenefit, hasBenefit, !hasBenefit, true),
        evidenceLevel = EvidenceLevel.A,
    )

}