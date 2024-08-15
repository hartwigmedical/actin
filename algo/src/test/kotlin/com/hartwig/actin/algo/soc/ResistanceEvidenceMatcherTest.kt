package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val ACTIONABLE_EVENTS: ActionableEvents = ImmutableActionableEvents.builder().addGenes(
    TestServeActionabilityFactory.geneBuilder().direction(EvidenceDirection.RESISTANT)
        .intervention(TestServeActionabilityFactory.treatmentBuilder().name("pembrolizumab").build())
        .applicableCancerType(TestServeActionabilityFactory.cancerTypeBuilder().doid("1520").build())
        .sourceEvent("BRAF amp")
        .event(GeneEvent.AMPLIFICATION)
        .gene("BRAF")
        .level(EvidenceLevel.A).build()
).build()
private val DOID_MODEL = TestDoidModelFactory.createMinimalTestDoidModel()
private val TUMOR_DOIDS = setOf("1520")
private val TREATMENT_DATABASE = TestTreatmentDatabaseFactory.createProper()
private val MOLECULAR_HISTORY = TestMolecularFactory.createMinimalTestMolecularHistory()
private val RESISTANCE_EVIDENCE_MATCHER =
    ResistanceEvidenceMatcher.create(DOID_MODEL, TUMOR_DOIDS, ACTIONABLE_EVENTS, TREATMENT_DATABASE, MOLECULAR_HISTORY)

class ResistanceEvidenceMatcherTest {

    @Test
    fun `Should match resistance evidence to SOC treatments`() {
        val socTreatment =
            TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY, setOf(DrugType.TOPO1_INHIBITOR))

        val actualResistanceEvidence = RESISTANCE_EVIDENCE_MATCHER.match(socTreatment)
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
        val actualResistanceEvidence = RESISTANCE_EVIDENCE_MATCHER.match(socTreatment)
        val expectedResistanceEvidence = emptyList<ResistanceEvidence>()

        assertThat(actualResistanceEvidence).isEqualTo(expectedResistanceEvidence)
    }

    @Test
    fun `Should find actionable gene in molecular history`() {
        val amplificationWithResistanceEvidence =
            TestServeActionabilityFactory.geneBuilder().sourceEvent("BRAF amp").event(GeneEvent.AMPLIFICATION).gene("BRAF").build()
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
        val amplificationFound = RESISTANCE_EVIDENCE_MATCHER.isFound(amplificationWithResistanceEvidence, hasAmplification)
        assertThat(amplificationFound).isTrue()
        val amplificationNotFound = RESISTANCE_EVIDENCE_MATCHER.isFound(amplificationWithResistanceEvidence, hasLoss)
        assertThat(amplificationNotFound).isFalse()
    }

    @Test
    fun `Should find actionable hotspot in molecular history`() {
        val hotspotWithResistanceEvidence =
            TestServeActionabilityFactory.hotspotBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("G").build()
        val hasHotspot = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal()
                .copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        ).molecularHistory
        val hasOtherHotspot = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal()
                .copy(gene = "gene 2", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        ).molecularHistory
        val hotspotFound = RESISTANCE_EVIDENCE_MATCHER.isFound(hotspotWithResistanceEvidence, hasHotspot)
        assertThat(hotspotFound).isTrue()
        val anotherHotspotFound = RESISTANCE_EVIDENCE_MATCHER.isFound(hotspotWithResistanceEvidence, hasOtherHotspot)
        assertThat(anotherHotspotFound).isFalse()
    }

    @Test
    fun `Should find actionable fusion in molecular history`() {
        val fusionWithResistanceEvidence = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.FUSION).gene("gene 1").build()
        val hasFusion = MolecularTestFactory.withFusion(
            TestFusionFactory.createMinimal()
                .copy(geneStart = "gene 1", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        ).molecularHistory
        val hasOtherFusion = MolecularTestFactory.withFusion(
            TestFusionFactory.createMinimal()
                .copy(geneStart = "gene 2", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        ).molecularHistory
        val fusionFound = RESISTANCE_EVIDENCE_MATCHER.isFound(fusionWithResistanceEvidence, hasFusion)
        assertThat(fusionFound).isTrue()
        val anotherFusionFound = RESISTANCE_EVIDENCE_MATCHER.isFound(fusionWithResistanceEvidence, hasOtherFusion)
        assertThat(anotherFusionFound).isFalse()
    }

    @Test
    fun `Should find actionable range in molecular history`() {
        val rangeWithResistanceEvidence = TestServeActionabilityFactory.rangeBuilder().gene("gene 1").chromosome("X").start(4).end(8)
            .applicableMutationType(MutationType.ANY).build()
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
        val rangeFound = RESISTANCE_EVIDENCE_MATCHER.isFound(rangeWithResistanceEvidence, hasRange)
        assertThat(rangeFound).isTrue()
        val anotherRangeFound = RESISTANCE_EVIDENCE_MATCHER.isFound(rangeWithResistanceEvidence, hasAnotherRange)
        assertThat(anotherRangeFound).isFalse()
    }
}