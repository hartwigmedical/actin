package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val INDICATION = ImmutableIndication.builder().applicableType(
    ImmutableCancerType.builder().name("").doid("1520").build()
).build()
private val MOLECULAR_CRITERIUM = TestServeActionabilityFactory.createGene("BRAF", GeneEvent.AMPLIFICATION)
private val ACTIONABLE_EVENTS: EfficacyEvidence = TestServeActionabilityFactory.createEfficacyEvidence(
    Knowledgebase.CKB,
    "pembrolizumab",
    EvidenceDirection.RESISTANT,
    EvidenceLevel.A,
    MOLECULAR_CRITERIUM,
    INDICATION
)
private val DOID_MODEL = TestDoidModelFactory.createMinimalTestDoidModel()
private val TUMOR_DOIDS = setOf("1520")
private val TREATMENT_DATABASE = TestTreatmentDatabaseFactory.createProper()
private val MOLECULAR_HISTORY = TestMolecularFactory.createMinimalTestMolecularHistory()

class ResistanceEvidenceMatcherTest {

    private val resistanceEvidenceMatcher =
        ResistanceEvidenceMatcher.create(DOID_MODEL, TUMOR_DOIDS, listOf(ACTIONABLE_EVENTS), TREATMENT_DATABASE, MOLECULAR_HISTORY)

    @Test
    fun `Should match resistance evidence to SOC treatments`() {
        val socTreatment =
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY, setOf(DrugType.TOPO1_INHIBITOR))

        val actualResistanceEvidence = resistanceEvidenceMatcher.match(socTreatment)
        val expectedResistanceEvidence = listOf(
            ResistanceEvidence(
                event = "BRAF amp",
                isTested = null,
                isFound = false,
                resistanceLevel = "A",
                evidenceUrls = emptySet(),
                treatmentName = "PEMBROLIZUMAB"
            )
        )

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should return empty resistance evidence list for SOC treatment without resistance evidence`() {
        val socTreatment = TreatmentTestFactory.drugTreatment("capecitabine+oxaliplatin", TreatmentCategory.CHEMOTHERAPY)
        val actualResistanceEvidence = resistanceEvidenceMatcher.match(socTreatment)
        val expectedResistanceEvidence = emptyList<ResistanceEvidence>()

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should find actionable gene in molecular history`() {
        val amplification = TestServeActionabilityFactory.createGene("BRAF", GeneEvent.AMPLIFICATION)
        val amplificationWithResistanceEvidence = TestServeActionabilityFactory.createEfficacyEvidence(molecularCriterium = amplification)
        val hasAmplification = MolecularTestFactory.withCopyNumber(
            TestCopyNumberFactory.createMinimal().copy(
                gene = "BRAF", type = CopyNumberType.FULL_GAIN, isReportable = true
            )
        ).molecularHistory
        val hasLoss = MolecularTestFactory.withCopyNumber(
            TestCopyNumberFactory.createMinimal().copy(
                gene = "BRAF", type = CopyNumberType.LOSS, isReportable = true
            )
        ).molecularHistory
        val amplificationFound = resistanceEvidenceMatcher.isFound(amplificationWithResistanceEvidence, hasAmplification)
        assertThat(amplificationFound).isTrue()
        val amplificationNotFound = resistanceEvidenceMatcher.isFound(amplificationWithResistanceEvidence, hasLoss)
        assertThat(amplificationNotFound).isFalse()
    }

    @Test
    fun `Should find actionable hotspot in molecular history`() {
        val hotspotWithResistanceEvidence = TestServeActionabilityFactory.withHotspot("gene 1", "X", 2, "A", "G")
        val hasHotspot = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal()
                .copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        ).molecularHistory
        val hasOtherHotspot = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal()
                .copy(gene = "gene 2", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        ).molecularHistory
        val hotspotFound = resistanceEvidenceMatcher.isFound(hotspotWithResistanceEvidence, hasHotspot)
        assertThat(hotspotFound).isTrue()
        val anotherHotspotFound = resistanceEvidenceMatcher.isFound(hotspotWithResistanceEvidence, hasOtherHotspot)
        assertThat(anotherHotspotFound).isFalse()
    }

    @Test
    fun `Should find actionable fusion in molecular history`() {
        val fusionWithResistanceEvidence = TestServeActionabilityFactory.withGene(GeneEvent.FUSION, "gene 1")
        val hasFusion = MolecularTestFactory.withFusion(
            TestFusionFactory.createMinimal()
                .copy(geneStart = "gene 1", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        ).molecularHistory
        val hasOtherFusion = MolecularTestFactory.withFusion(
            TestFusionFactory.createMinimal()
                .copy(geneStart = "gene 2", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        ).molecularHistory
        val fusionFound = resistanceEvidenceMatcher.isFound(fusionWithResistanceEvidence, hasFusion)
        assertThat(fusionFound).isTrue()
        val anotherFusionFound = resistanceEvidenceMatcher.isFound(fusionWithResistanceEvidence, hasOtherFusion)
        assertThat(anotherFusionFound).isFalse()
    }

    @Test
    fun `Should find actionable range in molecular history`() {
        val rangeWithResistanceEvidence = TestServeActionabilityFactory.withExon("gene 1", "X", 4, 8, MutationType.ANY)
        val hasRange = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "gene 1",
                chromosome = "X",
                position = 6,
                isReportable = true,
                canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE)
            )
        ).molecularHistory
        val hasAnotherRange = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "gene 2",
                chromosome = "X",
                position = 6,
                isReportable = true,
                canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE)
            )
        ).molecularHistory
        val rangeFound = resistanceEvidenceMatcher.isFound(rangeWithResistanceEvidence, hasRange)
        assertThat(rangeFound).isTrue()
        val anotherRangeFound = resistanceEvidenceMatcher.isFound(rangeWithResistanceEvidence, hasAnotherRange)
        assertThat(anotherRangeFound).isFalse()
    }
}