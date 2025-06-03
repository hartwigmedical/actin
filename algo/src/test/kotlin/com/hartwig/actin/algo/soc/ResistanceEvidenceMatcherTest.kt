package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory
import com.hartwig.actin.datamodel.algo.ResistanceEvidence
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
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
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.efficacy.EvidenceDirection
import com.hartwig.serve.datamodel.efficacy.EvidenceLevel
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import io.mockk.every
import io.mockk.mockk
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
private val MOLECULAR_HISTORY = TestMolecularFactory.createMinimalTestMolecularHistory()

class ResistanceEvidenceMatcherTest {

    private val actionabilityMatcher = mockk<ActionabilityMatcher>()
    
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
        ).molecularHistory

        val hasDel = MolecularTestFactory.withCopyNumber(
            TestCopyNumberFactory.createMinimal().copy(
                gene = "BRAF",
                canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL),
                isReportable = true
            )
        ).molecularHistory

        val resistanceEvidenceMatcher =
            resistanceEvidenceMatcher(listOf(amplificationWithResistanceEvidence))

        val amplificationFound = resistanceEvidenceMatcher.isFound(amplificationWithResistanceEvidence, hasAmplification)
        assertThat(amplificationFound).isTrue()

        val amplificationNotFound = resistanceEvidenceMatcher.isFound(amplificationWithResistanceEvidence, hasDel)
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
        ).molecularHistory

        val resistanceEvidenceMatcher =
            resistanceEvidenceMatcher(listOf(hotspotWithResistanceEvidence))

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
        ).molecularHistory

        val hotspotFound = resistanceEvidenceMatcher.isFound(hotspotWithResistanceEvidence, hasHotspot)
        assertThat(hotspotFound).isTrue()

        val anotherHotspotFound = resistanceEvidenceMatcher.isFound(hotspotWithResistanceEvidence, hasOtherHotspot)
        assertThat(anotherHotspotFound).isFalse()
    }

    @Test
    fun `Should find actionable fusion in molecular history`() {
        val fusionWithResistanceEvidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.FUSION)
        val fusion =
            TestFusionFactory.createMinimal().copy(geneStart = "gene 1", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        val hasFusion = MolecularTestFactory.withFusion(fusion).molecularHistory

        val otherFusion = TestFusionFactory.createMinimal()
            .copy(geneStart = "gene 2", driverType = FusionDriverType.PROMISCUOUS_5, isReportable = true)
        val hasOtherFusion = MolecularTestFactory.withFusion(otherFusion).molecularHistory

        every { actionabilityMatcher.match(hasFusion.molecularTests.first()) } returns mapOf(
            fusion to ActionabilityMatch(
                listOf(
                    fusionWithResistanceEvidence
                ), emptyMap()
            )
        )
        val fusionFound = resistanceEvidenceMatcher(listOf(fusionWithResistanceEvidence)).isFound(fusionWithResistanceEvidence, hasFusion)
        assertThat(fusionFound).isTrue()

        every { actionabilityMatcher.match(hasOtherFusion.molecularTests.first()) } returns emptyMap()
        val anotherFusionFound =
            resistanceEvidenceMatcher(listOf(fusionWithResistanceEvidence)).isFound(fusionWithResistanceEvidence, hasOtherFusion)
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
        ).molecularHistory

        val hasAnotherRange = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "gene 2",
                chromosome = "X",
                position = 6,
                isReportable = true,
                canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal().copy(codingEffect = CodingEffect.MISSENSE)
            )
        ).molecularHistory

        val resistanceEvidenceMatcher = resistanceEvidenceMatcher(listOf(rangeWithResistanceEvidence))

        val rangeFound = resistanceEvidenceMatcher.isFound(rangeWithResistanceEvidence, hasRange)
        assertThat(rangeFound).isTrue()

        val anotherRangeFound = resistanceEvidenceMatcher.isFound(rangeWithResistanceEvidence, hasAnotherRange)
        assertThat(anotherRangeFound).isFalse()
    }

    private fun resistanceEvidenceMatcher(efficacyEvidence: List<EfficacyEvidence> = emptyList()) =
        ResistanceEvidenceMatcher.create(
            DOID_MODEL,
            TUMOR_DOIDS,
            listOf(EFFICACY_EVIDENCE),
            TREATMENT_DATABASE,
            MOLECULAR_HISTORY,
            ActionabilityMatcher(evidences = efficacyEvidence, emptyList())
        )
}