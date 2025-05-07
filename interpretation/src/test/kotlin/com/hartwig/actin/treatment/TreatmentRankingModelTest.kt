package com.hartwig.actin.treatment

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceType
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentRankingModelTest {

    @Test
    fun `Should add scoring for guideline, benefit, patient's tumor, exact variant`() {
        val ranker = TreatmentRankingModel(EvidenceScoringModel())
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.PHASE_I,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            ),
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.PRECLINICAL,
                    hasBenefit = true,
                    treatment = "treatment2",
                )
            ),
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = true,
                    approvalStage = EvidenceLevelDetails.PHASE_III,
                    hasBenefit = true,
                    treatment = "treatment3",
                )
            ),
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = true,
                    treatment = "treatment4",
                )
            )
        )
        val rank = ranker.rank(patientRecord).ranking
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
        val ranker = TreatmentRankingModel(EvidenceScoringModel())
        val patientRecord = patientRecord(
            createVariant(
                gene = "KRAS",
                treatmentEvidence =
                treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = true,
                    treatment = "treatment1",
                    event = "KRAS G12C"
                )
            ), createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                    event = "BRAF V600E"
                )
            )
        )
        val rank = ranker.rank(patientRecord)
        assertThat(rank.ranking[0].treatment).isEqualTo("treatment1")
        assertThat(rank.ranking[0].score).isEqualTo(3900.0)
    }

    @Test
    fun `Should sum scores for a single treatment on different tumor types`() {
        val ranker = TreatmentRankingModel(EvidenceScoringModel())
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1"
                )
            ),
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.ALL_TYPES,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            ),
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.OTHER_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            )
        )
        val rank = ranker.rank(patientRecord)
        assertThat(rank.ranking[0].treatment).isEqualTo("treatment1")
        assertThat(rank.ranking[0].score).isEqualTo(5100.0)
    }

    @Test
    fun `Should sum scores for a single treatment with both benefit and resistance`() {
        val ranker = TreatmentRankingModel(EvidenceScoringModel())
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1"
                )
            ), createVariant(
                treatmentEvidence =
                treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = false,
                    treatment = "treatment1",
                )
            )
        )
        val rank = ranker.rank(patientRecord)
        assertThat(rank.ranking[0].treatment).isEqualTo("treatment1")
        assertThat(rank.ranking[0].score).isEqualTo(100.0)
    }

    @Test
    fun `Should diminish returns single treatment, same tumor type, variant`() {
        val ranker = TreatmentRankingModel(EvidenceScoringModel())
        val patientRecord = patientRecord(
            createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.GUIDELINE,
                    hasBenefit = true,
                    treatment = "treatment1"
                )
            ), createVariant(
                treatmentEvidence = treatmentEvidence(
                    cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
                    isCategoryEvent = false,
                    approvalStage = EvidenceLevelDetails.FDA_APPROVED,
                    hasBenefit = true,
                    treatment = "treatment1",
                )
            )
        )
        val rank = ranker.rank(patientRecord).ranking
        assertThat(rank[0].treatment).isEqualTo("treatment1")
        assertThat(rank[0].score).isEqualTo(2950.0)
    }


    private fun patientRecord(
        vararg variants: Variant
    ) = TestPatientFactory.createProperTestPatientRecord().copy(
        molecularHistory = MolecularHistory(
            listOf(
                TestMolecularFactory.createMinimalTestMolecularRecord().copy(
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

    private fun createVariant(gene: String = "BRAF", treatmentEvidence: TreatmentEvidence) = TestVariantFactory.createMinimal().copy(
        gene = gene,
        evidence = TestClinicalEvidenceFactory.createEmpty().copy(
            treatmentEvidence = setOf(treatmentEvidence)
        )
    )

    private fun treatmentEvidence(
        treatment: String,
        cancerTypeMatchApplicability: CancerTypeMatchApplicability,
        isCategoryEvent: Boolean,
        approvalStage: EvidenceLevelDetails,
        hasBenefit: Boolean,
        event: String = "BRAF V600E"
    ) = TestTreatmentEvidenceFactory.create(
        treatment = treatment,
        sourceEvent = event,
        cancerTypeMatchApplicability = cancerTypeMatchApplicability,
        evidenceType = if (isCategoryEvent) EvidenceType.ANY_MUTATION else EvidenceType.HOTSPOT_MUTATION,
        evidenceLevelDetails = approvalStage,
        evidenceDirection = EvidenceDirection(hasBenefit, hasBenefit, !hasBenefit, true),
        evidenceLevel = EvidenceLevel.A,
    )

}