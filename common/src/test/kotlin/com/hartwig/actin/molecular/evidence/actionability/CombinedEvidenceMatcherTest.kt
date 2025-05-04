package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableVariantAnnotation
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val brafHotspot =
    hotspot(TestServeMolecularFactory.createVariantAnnotation(gene = "BRAF", chromosome = "7", position = 140453136, ref = "T", alt = "A"))
private val krasHotspot =
    hotspot(TestServeMolecularFactory.createVariantAnnotation(gene = "KRAS", chromosome = "12", position = 25245350, ref = "C", alt = "T"))

private val brafVariant = TestVariantFactory.createMinimal()
    .copy(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A",
        driverLikelihood = DriverLikelihood.HIGH,  // TODO remove these if we push this filtering down
        isReportable = true
    )
private val krasVariant = TestVariantFactory.createMinimal()
    .copy(
        gene = "KRAS",
        chromosome = "12",
        position = 25245350,
        ref = "C",
        alt = "T",
        driverLikelihood = DriverLikelihood.HIGH,  // TODO remove these if we push this filtering down
        isReportable = true
    )

// TODO is there a helper for this in test serve factory?
private fun hotspot(variant: VariantAnnotation): ActionableHotspot {
    return ImmutableActionableHotspot.builder()
        .variants(listOf(variant))
        .sourceDate(LocalDate.now())
        .sourceEvent("")
        .build()
}

// TODO tests for hotspot driver reportable/driver likelihood, gene event types
class CombinedEvidenceMatcherTest {
    @Test
    fun `Should combine MatchResults to Failure if any are Failure`() {
        assertThat(
            MatchResult.combine(listOf(MatchResult.Success(), MatchResult.Failure))
        ).isEqualTo(MatchResult.Failure)
    }

    @Test
    fun `Should combine MatchResults to Success if empty`() {
        assertThat(
            MatchResult.combine(emptyList())
        ).isEqualTo(MatchResult.Success())
    }

    @Test
    fun `Should combine MatchResults to Success if all are Success`() {
        assertThat(
            MatchResult.combine(listOf(MatchResult.Success(), MatchResult.Success()))
        ).isEqualTo(MatchResult.Success())
    }

    @Test
    fun `Should aggregate all events when combining successful MatchResults`() {
        val matchResult1 = MatchResult.Success(listOf(brafVariant))
        val matchResult2 = MatchResult.Success(listOf(krasVariant))

        val result = MatchResult.combine(listOf(matchResult1, matchResult2))

        assertThat(result).isEqualTo(MatchResult.Success(listOf(brafVariant, krasVariant)))
    }

    @Test
    fun `Should create Success MatchResult from Actionablet`() {
        assertThat(MatchResult.successWhenNotEmpty(listOf(brafVariant))).isEqualTo(MatchResult.Success(listOf(brafVariant)))
    }

    @Test
    fun `Should create Failure MatchResult from empty list`() {
        assertThat(MatchResult.successWhenNotEmpty(emptyList())).isEqualTo(MatchResult.Failure)
    }

    @Test
    fun `Should not match empty panel`() {
        val matcher = matcherFactory(createClinicalEvidences())
        val test = TestMolecularFactory.createMinimalTestPanelRecord()
        val matches = matcher.match(test)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match panel with combined evidence`() {
        val matcher = matcherFactory(createClinicalEvidences())
        val test = TestMolecularFactory.createProperTestPanelRecord()
    }

    @Test
    fun `Should match combined evidence with multiple hotspots`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafHotspot, krasHotspot))
                .build(),
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafVariant, krasVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isNotEmpty
        assertThat(matches).hasSize(2)
        assertThat(matches.get(brafVariant as Actionable)).isEqualTo(setOf(evidence))
        assertThat(matches.get(krasVariant as Actionable)).isEqualTo(setOf(evidence))
    }

    @Test
    fun `Should not match combined evidence with multiple hotspots when only single hotspot matches`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafHotspot, krasHotspot))
                .build()
        )

        val matcher = matcherFactory(listOf(evidence))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match multiple evidences to same hotspot`() {
        val evidence1 = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafHotspot))
                .build(),
            treatment = "Treatment 1",
        )

        val evidence2 = TestServeEvidenceFactory.create(
            molecularCriterium = ImmutableMolecularCriterium.builder()
                .addAllHotspots(listOf(brafHotspot))
                .build(),
            treatment = "Treatment 2",
        )

        val matcher = CombinedEvidenceMatcher(listOf(evidence1, evidence2))

        val molecularTest = TestMolecularFactory.createMinimalTestPanelRecord()
            .copy(
                // do we need to connect this with a panelSpecification?
//                testedGenes = setOf("BRAF"),
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafVariant))
            )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches.get(brafVariant as Actionable)).isEqualTo(setOf(evidence1, evidence2))
    }

    private fun createTestCombinedCriterium(): MolecularCriterium {
        val hotspot = ImmutableActionableHotspot.builder()
            .variants(
                listOf(
                    ImmutableVariantAnnotation.builder().gene("BRAF").chromosome("7").position(140453136).ref("T").alt("A").build()
                )
            )
            .sourceDate(LocalDate.now())
            .sourceEvent("")
            .build()

        val fusion = ImmutableActionableFusion.builder()
            .geneUp("EGFR")
            .geneDown("RAD51")
            .sourceDate(LocalDate.of(2021, 1, 1))
            .sourceEvent("")
            .build()

        val molecularCriterium = ImmutableMolecularCriterium.builder()
            .hotspots(listOf(hotspot))
            .fusions(listOf(fusion))
            .build()

        return molecularCriterium
    }

    private fun createEvidenceDatabase(): EvidenceDatabase {
        return EvidenceDatabase(createKnownEventResolver(), factory())
    }

    val MATCHING_DOID = "matching doid"

    fun createKnownEventResolver(): KnownEventResolver {
        val knownEvents = ImmutableKnownEvents.builder()
            .addHotspots(TestServeKnownFactory.hotspotBuilder().gene("BRAF").chromosome("7").position(140453136).ref("T").alt("A").build())
            .build()

        return KnownEventResolver(knownEvents, knownEvents.genes())
    }

    fun factory(): ClinicalEvidenceMatcherFactory {
        return object : ClinicalEvidenceMatcherFactory {
            override fun create(molecularTest: MolecularTest): ClinicalEvidenceMatcher {
                return createClinicalEvidenceMatcher(molecularTest)
            }
        }
    }

    fun createClinicalEvidenceMatcher(molecularTest: MolecularTest): ClinicalEvidenceMatcher {
        val personalizedActionabilityFactory = PersonalizedActionabilityFactory(setOf(MATCHING_DOID))

        val evidences = createClinicalEvidences()

        val trials = listOf<ActionableTrial>()

        // create an actionableToEvidences from the molecularTest, or wire up more directly for the tests somewhow?
        val actionableToEvidences: ActionableToEvidences = emptyMap()

        return ClinicalEvidenceMatcher(
            personalizedActionabilityFactory = personalizedActionabilityFactory,
            variantEvidence = VariantEvidence.create(actionableToEvidences, trials),
            copyNumberEvidence = CopyNumberEvidence.create(actionableToEvidences, trials),
            disruptionEvidence = DisruptionEvidence.create(actionableToEvidences, trials),
            homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableToEvidences, trials),
            fusionEvidence = FusionEvidence.create(actionableToEvidences, trials),
            virusEvidence = VirusEvidence.create(actionableToEvidences, trials),
            signatureEvidence = SignatureEvidence.create(evidences, trials)
        )
    }

    fun createClinicalEvidences(): List<EfficacyEvidence> {
        val variant =
            TestServeMolecularFactory.createVariantAnnotation(gene = "BRAF", chromosome = "7", position = 140453136, ref = "T", alt = "A")

        return listOf(
            TestServeEvidenceFactory.createEvidenceForHotspot(variant),
            TestServeEvidenceFactory.createEvidenceForGene(gene = "PTEN", geneEvent = GeneEvent.DELETION),
            TestServeEvidenceFactory.createEvidenceForFusion(geneUp = "EGFR", geneDown = "RAD51")
        )
    }

    fun matcherFactory(evidences: List<EfficacyEvidence>): CombinedEvidenceMatcher {
        return CombinedEvidenceMatcher(evidences)
    }
}

