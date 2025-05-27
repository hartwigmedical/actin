package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.actionability.CombinedEvidenceMatcher.Companion.successWhenNotEmpty
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val brafActionableHotspot =
    TestServeMolecularFactory.hotspot(TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A")
    )

private val krasActionableHotspot =
    TestServeMolecularFactory.hotspot(TestServeMolecularFactory.createVariantAnnotation(
        gene = "KRAS",
        chromosome = "12",
        position = 25245350,
        ref = "C",
        alt = "T")
    )

private val brafMolecularTestVariant = TestVariantFactory.createMinimal()
    .copy(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A",
        driverLikelihood = DriverLikelihood.HIGH,
        isReportable = true
    )
private val krasMolecularTestVariant = TestVariantFactory.createMinimal()
    .copy(
        gene = "KRAS",
        chromosome = "12",
        position = 25245350,
        ref = "C",
        alt = "T",
        driverLikelihood = DriverLikelihood.HIGH,
        isReportable = true
    )

private val actionableFusion: ActionableFusion = ImmutableActionableFusion.builder()
    .from(TestServeMolecularFactory.createActionableEvent())
    .geneUp("EGFR")
    .geneDown("RAD51")
    .build()

private val molecularTestFusion = TestFusionFactory.createMinimal()
    .copy(geneStart = "EGFR", geneEnd = "RAD51", driverLikelihood = DriverLikelihood.HIGH, isReportable = true)

// TODO: tests for applicability filtering, e.g. variant on non-applicable gene TP53 should be filtered out
class CombinedEvidenceMatcherTest {

    @Test
    fun `Should match combined evidence having multiple hotspots when also present in panel`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot, krasActionableHotspot))
                .build(),
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant, krasMolecularTestVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isNotEmpty
        assertThat(matches).hasSize(2)
        assertThat(matches.get(brafMolecularTestVariant as Actionable)).isEqualTo(setOf(evidence))
        assertThat(matches.get(krasMolecularTestVariant as Actionable)).isEqualTo(setOf(evidence))
    }

    @Test
    fun `Should not match combined evidence having multiple hotspots when only partially present in molecular test`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot, krasActionableHotspot))
                .build(),
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should include all evidences for same actionable`() {
        val criterium = ImmutableMolecularCriterium.builder()
            .addAllHotspots(listOf(brafActionableHotspot))
            .build()

        val evidence1 = TestServeEvidenceFactory.create(
            treatment = "Treatment 1",
            molecularCriterium = criterium,
        )

        val evidence2 = TestServeEvidenceFactory.create(
            treatment = "Treatment 2",
            molecularCriterium = criterium,
        )

        val matcher = matcherFactory(listOf(evidence1, evidence2))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches.get(brafMolecularTestVariant as Actionable)).isEqualTo(setOf(evidence1, evidence2))
    }

    @Test
    fun `Should match combined evidence having hotspot and fusion`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot))
                .addFusions(actionableFusion)
                .build()
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    variants = listOf(brafMolecularTestVariant),
                    fusions = listOf(molecularTestFusion))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isNotEmpty
        assertThat(matches).hasSize(2)
        assertThat(matches.get(brafMolecularTestVariant as Actionable)).isEqualTo(setOf(evidence))
        assertThat(matches.get(molecularTestFusion as Actionable)).isEqualTo(setOf(evidence))
    }

    @Test
    fun `Should not match combined evidence having hotspot and fusion when only partially present in molecular test`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot))
                .addFusions(actionableFusion)
                .build()
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    variants = listOf(krasMolecularTestVariant),  // different variant
                    fusions = listOf(molecularTestFusion))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match combined evidence with multiple hotspots when only single hotspot matches`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot, krasActionableHotspot))
                .build()
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match virus characteristics`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .characteristics(
                    listOf(
                        TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.HPV_POSITIVE)
                    )
                )
                .build()
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    viruses = listOf(
                        TestMolecularFactory.minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS)
                    )
                )
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isNotEmpty
        assertThat(matches).hasSize(1)
        assertThat(matches.values.flatten()).contains(evidence)
    }

    @Test
    fun `Should match hla`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHlaCriterium(
                baseActionableEvent = TestServeMolecularFactory.createActionableEvent(),
                hlaAllele = "A*02:01"
            )
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()

        // Immunology is only present in MolecularRecord (Orange) but not MolecularTest (shared interface),
        // so we won't be able to match this for now
        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match multiple evidences to same hotspot`() {
        val evidence1 = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot))
                .build(),
            treatment = "Treatment 1",
        )

        val evidence2 = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafActionableHotspot))
                .build(),
            treatment = "Treatment 2",
        )

        val matcher = CombinedEvidenceMatcher(listOf(evidence1, evidence2))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches.get(brafMolecularTestVariant as Actionable)).isEqualTo(setOf(evidence1, evidence2))
    }

    @Test
    fun `Should match msi stable evidence with msi stable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(panelTestWithMsi(isUnstable = false))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).contains(evidence)
    }

    @Test
    fun `Should not match msi stable evidence with msi unstable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(panelTestWithMsi(isUnstable = true))
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should not match msi stable evidence with tumor of unknown msi stability`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val matcher = matcherFactory(listOf(evidence))

        val emptyTest = TestMolecularFactory.createMinimalTestPanelRecord()
        assertThat(emptyTest.characteristics.microsatelliteStability).isNull()  // test precondition

        val evidenceMatches = matcher.match(emptyTest)
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should match combined msi and driver evidence when both present`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addHotspots(brafActionableHotspot)
                .addCharacteristics(TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE))
                .build(),
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = panelTestWithMsi(isUnstable = false)
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers()
                    .copy(variants = listOf(brafMolecularTestVariant))
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(2)
        assertThat(evidenceMatches.get(brafMolecularTestVariant as Actionable)).isEqualTo(setOf(evidence))
        assertThat(evidenceMatches.get(molecularTest.characteristics.microsatelliteStability as Actionable)).isEqualTo(setOf(evidence))
    }

    @Test
    fun `Should not match combined msi and driver evidence if only one present`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addHotspots(brafActionableHotspot)
                .addCharacteristics(TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE))
                .build(),
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should match msi unstable evidence with msi unstable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(panelTestWithMsi(isUnstable = true))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should not match msi unstable evidence with msi stable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(panelTestWithMsi(isUnstable = false))
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should match high tml evidence with high tml tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                tumorMutationalLoad = TumorMutationalLoad(
                    score = 185,
                    isHigh = true,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        ))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match low tml evidence with low tml tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                tumorMutationalLoad = TumorMutationalLoad(
                    score = 185,
                    isHigh = false,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        ))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match high tmb evidence with high tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                tumorMutationalLoad = TumorMutationalLoad(
                    score = 185,
                    isHigh = true,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        ))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match low tmb evidence with low tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                tumorMutationalLoad = TumorMutationalLoad(
                    score = 185,
                    isHigh = false,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        ))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match hrd evidence with hrd tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
        val matcher = matcherFactory(listOf(evidence))

        val evidenceMatches = matcher.match(TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                homologousRecombination = minimalHrdCharacteristic().copy(isDeficient = true)
            )
        ))
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match gene evidence with variant on that gene`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    variants = listOf(brafMolecularTestVariant.copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)))
                )
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match promiscuous fusion`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "EGFR", geneEvent = GeneEvent.FUSION)
        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    fusions = listOf(molecularTestFusion))
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match disruption`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    disruptions = listOf(
                        TestMolecularFactory.minimalDisruption().copy(gene = "BRAF", isReportable = true, geneRole = GeneRole.UNKNOWN)
                    )
                )
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match homozygous disruption`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    disruptions = listOf(
                        TestMolecularFactory.minimalDisruption().copy(gene = "BRAF", isReportable = true, geneRole = GeneRole.UNKNOWN)
                    )
                )
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match copy number amplification`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "EGFR", geneEvent = GeneEvent.AMPLIFICATION)
        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    copyNumbers = listOf(
                        TestMolecularFactory.minimalCopyNumber().copy(
                            gene = "EGFR",
                            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN),
                        )
                    )
                )
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should match copy number deletion`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "EGFR", geneEvent = GeneEvent.DELETION)
        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                    copyNumbers = listOf(
                        TestMolecularFactory.minimalCopyNumber().copy(
                            gene = "EGFR",
                            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.DEL),
                        )
                    )
                )
            )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).hasSize(1)
        assertThat(evidenceMatches.values.flatten()).containsExactly(evidence)
    }

    @Test
    fun `Should create Success MatchResult from Actionable`() {
        assertThat(successWhenNotEmpty(listOf(brafMolecularTestVariant))).isEqualTo(ActionabilityMatchResult.Success(listOf(brafMolecularTestVariant)))
    }

    @Test
    fun `Should create Failure MatchResult from empty list`() {
        assertThat(successWhenNotEmpty(emptyList())).isEqualTo(ActionabilityMatchResult.Failure)
    }

    fun matcherFactory(evidences: List<EfficacyEvidence>): CombinedEvidenceMatcher {
        return CombinedEvidenceMatcher(evidences)
    }

    fun panelTestWithMsi(isUnstable: Boolean): PanelRecord {
        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                microsatelliteStability = MicrosatelliteStability(
                    microsatelliteIndelsPerMb = 0.0,
                    isUnstable = isUnstable,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }

    fun minimalTranscriptImpact(): TranscriptVariantImpact {
        return TranscriptVariantImpact(
            transcriptId = "",
            hgvsCodingImpact = "",
            hgvsProteinImpact = "",
            affectedCodon = 0,
            isSpliceRegion = false,
            effects = emptySet(),
            codingEffect = CodingEffect.NONE,
            affectedExon = null
        )
    }

    fun minimalHrdCharacteristic(): HomologousRecombination {
        return HomologousRecombination(
            score = 0.0,
            isDeficient = false,
            type = HomologousRecombinationType.NONE,
            brca1Value = 0.0,
            brca2Value = 0.0,
            evidence = TestClinicalEvidenceFactory.createEmpty()
        )
    }
}
