package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.actionability.CombinedEvidenceMatcher.Companion.successWhenNotEmpty
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
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

class CombinedEvidenceMatcherTest {

    @Test
    fun `Should aggregate all events when combining successful MatchResults`() {
        val actionabilityMatchResult1 = ActionabilityMatchResult.Success(listOf(brafMolecularTestVariant))
        val actionabilityMatchResult2 = ActionabilityMatchResult.Success(listOf(krasMolecularTestVariant))

        val result = ActionabilityMatchResult.combine(listOf(actionabilityMatchResult1, actionabilityMatchResult2))

        assertThat(result).isEqualTo(ActionabilityMatchResult.Success(listOf(brafMolecularTestVariant, krasMolecularTestVariant)))
    }

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

        // Seems like immunology is only present in MolecularRecord (Orange) but not MolecularTest (shared interface),
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
}
