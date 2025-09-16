package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val INDICATION = TestServeFactory.createIndicationWithDoid("1520")
private val MOLECULAR_CRITERIUM =
    TestServeMolecularFactory.createGeneCriterium(gene = "BRAF", geneEvent = GeneEvent.AMPLIFICATION, sourceEvent = "BRAF amp")
private val EFFICACY_EVIDENCE = TestServeEvidenceFactory.create(
    source = ActionabilityConstants.EVIDENCE_SOURCE,
    treatment = "pembrolizumab",
    indication = INDICATION,
    molecularCriterium = MOLECULAR_CRITERIUM,
    evidenceLevel = EvidenceLevel.A,
    evidenceDirection = EvidenceDirection.RESISTANT
)
private val DOID_MODEL = TestDoidModelFactory.createMinimalTestDoidModel()
private val TUMOR_DOIDS = setOf(INDICATION.applicableType().doid())
private val TREATMENT_DATABASE = TestTreatmentDatabaseFactory.createProper()
private val MOLECULAR_HISTORY = TestMolecularFactory.createMinimalMolecularTests()

class ResistanceEvidenceMatcherTest {

    @Test
    fun `Should match resistance evidence to SOC treatments`() {
        val socTreatment =
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY, setOf(DrugType.TOPO1_INHIBITOR))

        val actualResistanceEvidence = resistanceEvidenceMatcher().match(socTreatment)
        val expectedResistanceEvidence = listOf(
            ResistanceEvidence(
                event = "BRAF amp",
                treatmentName = "PEMBROLIZUMAB",
                resistanceLevel = "A",
                isTested = null,
                isFound = false,
                evidenceUrls = emptySet()
            )
        )

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should return empty resistance evidence list for SOC treatment without resistance evidence`() {
        val socTreatment = TreatmentTestFactory.drugTreatment("capecitabine+oxaliplatin", TreatmentCategory.CHEMOTHERAPY)
        val actualResistanceEvidence = resistanceEvidenceMatcher().match(socTreatment)
        val expectedResistanceEvidence = emptyList<ResistanceEvidence>()

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should find actionable gene in molecular history`() {
        val amplificationWithResistanceEvidence =
            TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.AMPLIFICATION)
        val hasAmplification = MolecularTestFactory.withCopyNumber(
            TestCopyNumberFactory.createMinimal().copy(
                gene = "BRAF",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN),
                isReportable = true
            )
        ).molecularTests

        val hasDel = MolecularTestFactory.withCopyNumber(
            TestCopyNumberFactory.createMinimal().copy(
                gene = "BRAF",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL),
                isReportable = true
            )
        ).molecularTests

        val resistanceEvidenceMatcher =
            resistanceEvidenceMatcher(efficacyEvidence = listOf(amplificationWithResistanceEvidence), molecularTests = hasAmplification)

        val amplificationFound = resistanceEvidenceMatcher.isFound(amplificationWithResistanceEvidence)
        assertThat(amplificationFound).isTrue()

        val anotherResistanceEvidenceMatcher =
            resistanceEvidenceMatcher(efficacyEvidence = listOf(amplificationWithResistanceEvidence), molecularTests = hasDel)
        val amplificationNotFound = anotherResistanceEvidenceMatcher.isFound(amplificationWithResistanceEvidence)
        assertThat(amplificationNotFound).isFalse()
    }

    @Test
    fun `Should find actionable hotspot in molecular history`() {
        val hotspotWithResistanceEvidence =
            TestServeEvidenceFactory.createEvidenceForHotspot(TestServeMolecularFactory.createVariantAnnotation("gene 1", "X", 2, "A", "G"))
        val hasHotspot = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal()
                .copy(
                    gene = "gene 1",
                    chromosome = "X",
                    position = 2,
                    ref = "A",
                    alt = "G",
                    driverLikelihood = DriverLikelihood.HIGH,
                    isReportable = true
                )
        ).molecularTests


        val hasOtherHotspot = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal()
                .copy(
                    gene = "gene 2",
                    chromosome = "X",
                    position = 2,
                    ref = "A",
                    alt = "G",
                    driverLikelihood = DriverLikelihood.HIGH,
                    isReportable = true
                )
        ).molecularTests

        val resistanceEvidenceMatcher =
            resistanceEvidenceMatcher(efficacyEvidence = listOf(hotspotWithResistanceEvidence), molecularTests = hasHotspot)
        val hotspotFound = resistanceEvidenceMatcher.isFound(hotspotWithResistanceEvidence)
        assertThat(hotspotFound).isTrue()

        val anotherResistanceEvidenceMatcher =
            resistanceEvidenceMatcher(efficacyEvidence = listOf(hotspotWithResistanceEvidence), molecularTests = hasOtherHotspot)
        val anotherHotspotFound = anotherResistanceEvidenceMatcher.isFound(hotspotWithResistanceEvidence)
        assertThat(anotherHotspotFound).isFalse()
    }

    @Test
    fun `Should find actionable fusion in molecular history`() {
        val fusionWithResistanceEvidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.FUSION)
        val fusion =
            TestFusionFactory.createMinimal().copy(geneStart = "gene 1", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        val hasFusion = MolecularTestFactory.withFusion(fusion).molecularTests

        val otherFusion = TestFusionFactory.createMinimal()
            .copy(geneStart = "gene 2", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        val hasOtherFusion = MolecularTestFactory.withFusion(otherFusion).molecularTests

        val fusionMatcher = resistanceEvidenceMatcher(
            efficacyEvidence = listOf(fusionWithResistanceEvidence),
            molecularTests = hasFusion
        )
        val fusionFound = fusionMatcher.isFound(fusionWithResistanceEvidence)
        assertThat(fusionFound).isTrue()

        val otherFusionMatcher = resistanceEvidenceMatcher(
            efficacyEvidence = listOf(fusionWithResistanceEvidence),
            molecularTests = hasOtherFusion
        )
        val anotherFusionFound = otherFusionMatcher.isFound(fusionWithResistanceEvidence)
        assertThat(anotherFusionFound).isFalse()
    }

    @Test
    fun `Should find actionable range in molecular history`() {

        val rangeWithResistanceEvidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createExonCriterium(
                gene = "gene 1",
                chromosome = "X",
                start = 4,
                end = 8,
                applicableMutationType = MutationType.ANY
            )
        )
        val hasRange = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "gene 1",
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE),
                chromosome = "X",
                position = 6,
                driverLikelihood = DriverLikelihood.HIGH,
                isReportable = true
            )
        ).molecularTests

        val hasAnotherRange = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "gene 2",
                chromosome = "X",
                position = 6,
                isReportable = true,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE)
            )
        ).molecularTests

        val resistanceEvidenceMatcher = resistanceEvidenceMatcher(
            efficacyEvidence = listOf(rangeWithResistanceEvidence),
            molecularTests = hasRange
        )

        val rangeFound = resistanceEvidenceMatcher.isFound(rangeWithResistanceEvidence)
        assertThat(rangeFound).isTrue()

        val anotherResistanceEvidenceMatcher = resistanceEvidenceMatcher(
            efficacyEvidence = listOf(rangeWithResistanceEvidence),
            molecularTests = hasAnotherRange
        )
        val anotherRangeFound = anotherResistanceEvidenceMatcher.isFound(rangeWithResistanceEvidence)
        assertThat(anotherRangeFound).isFalse()
    }

    private fun resistanceEvidenceMatcher(
        efficacyEvidence: List<EfficacyEvidence> = emptyList(),
        molecularTests: List<MolecularTest> = MOLECULAR_HISTORY
    ) = ResistanceEvidenceMatcher.create(
        DOID_MODEL,
        TUMOR_DOIDS,
        listOf(EFFICACY_EVIDENCE),
        TREATMENT_DATABASE,
        molecularTests,
        ActionabilityMatcher(evidences = efficacyEvidence, emptyList())
    )
}
