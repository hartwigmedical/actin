package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher.Companion.successWhenNotEmpty
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.actin.molecular.util.GeneConstants
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val brafActionableHotspot = TestServeMolecularFactory.hotspot(
    TestServeMolecularFactory.createVariantAnnotation(
        gene = "BRAF", chromosome = "7", position = 140453136, ref = "T", alt = "A"
    )
)

private val krasActionableHotspot = TestServeMolecularFactory.hotspot(
    TestServeMolecularFactory.createVariantAnnotation(
        gene = "KRAS", chromosome = "12", position = 25245350, ref = "C", alt = "T"
    )
)

private val brafMolecularTestVariant = TestVariantFactory.createMinimal().copy(
    gene = "BRAF",
    chromosome = "7",
    position = 140453136,
    ref = "T",
    alt = "A",
    driverLikelihood = DriverLikelihood.HIGH,
    isReportable = true
)
private val krasMolecularTestVariant = TestVariantFactory.createMinimal().copy(
    gene = "KRAS",
    chromosome = "12",
    position = 25245350,
    ref = "C",
    alt = "T",
    driverLikelihood = DriverLikelihood.HIGH,
    isReportable = true
)

private val actionableFusion: ActionableFusion =
    ImmutableActionableFusion.builder().from(TestServeMolecularFactory.createActionableEvent()).geneUp("EGFR").geneDown("RAD51").build()

private val molecularTestFusion = TestFusionFactory.createMinimal()
    .copy(geneStart = "EGFR", geneEnd = "RAD51", driverLikelihood = DriverLikelihood.HIGH, isReportable = true)

class ActionabilityMatcherTest {

    @Test
    fun `Should match hotspot evidence and trials with variant`() {
        val criterium = ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot)).build()
        val evidence = TestServeEvidenceFactory.create(treatment = "Treatment 1", molecularCriterium = criterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(criterium))

        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant)))
        val matches = matcher.match(molecularTest)

        assertThat(matches.size).isEqualTo(1)
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match hotspot evidence and trials with hotspot independent of driver likelihood`() {
        val criterium = ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot)).build()
        val evidence = TestServeEvidenceFactory.create(treatment = "Treatment 1", molecularCriterium = criterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(criterium))
        val variant = brafMolecularTestVariant.copy(driverLikelihood = null)

        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(variant)))
        val matches = matcher.match(molecularTest)

        assertThat(matches.size).isEqualTo(1)
        assertThat(matches[variant]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match combined evidence having multiple hotspots when also present in molecular test`() {
        val molecularCriterium =
            ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot, krasActionableHotspot)).build()
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers()
                .copy(variants = listOf(brafMolecularTestVariant, krasMolecularTestVariant))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isNotEmpty
        assertThat(matches).hasSize(2)
        val actionabilityMatch = ActionabilityMatch(listOf(evidence), mapOf(trial to setOf(molecularCriterium)))
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(actionabilityMatch)
        assertThat(matches[krasMolecularTestVariant]).isEqualTo(actionabilityMatch)
    }

    @Test
    fun `Should not match combined evidence having multiple hotspots when only partially present in molecular test`() {
        val molecularCriterium =
            ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot, krasActionableHotspot)).build()
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match hotspot to gene events on eligible genes`() {
        val molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val variant = brafMolecularTestVariant.copy(
            canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(
                    variant
                )
            )
        )

        val matches = matcher.match(molecularTest)
        val actionabilityMatch = ActionabilityMatch(listOf(evidence), mapOf(trial to setOf(molecularCriterium)))
        assertThat(matches[variant]).isEqualTo(actionabilityMatch)
    }

    @Test
    fun `Should not match hotspots to gene events on ineligible genes`() {
        val inapplicableGene = ApplicabilityFiltering.NON_APPLICABLE_GENES.first()

        val molecularCriterium = TestServeMolecularFactory.createGeneCriterium(gene = inapplicableGene, geneEvent = GeneEvent.ANY_MUTATION)
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium
        )
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val variantOnInapplicableGene = TestVariantFactory.createMinimal().copy(
            gene = inapplicableGene,
            canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
        )

        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(variantOnInapplicableGene)))

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should include all evidences and trials for same actionable`() {
        val criterium = ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot)).build()

        val evidence1 = TestServeEvidenceFactory.create(
            treatment = "Treatment 1",
            molecularCriterium = criterium,
        )

        val evidence2 = TestServeEvidenceFactory.create(
            treatment = "Treatment 2",
            molecularCriterium = criterium,
        )

        val trial1 = TestServeTrialFactory.create(anyMolecularCriteria = setOf(criterium))
        val trial2 = TestServeTrialFactory.create(anyMolecularCriteria = setOf(criterium))

        val matcher = matcherFactory(listOf(evidence1, evidence2), listOf(trial1, trial2))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches.size).isEqualTo(1)
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(
            ActionabilityMatch(
                listOf(evidence1, evidence2), mapOf(trial1 to setOf(criterium), trial2 to setOf(criterium))
            )
        )
    }

    @Test
    fun `Should match combined evidence and trials having hotspot and fusion`() {
        val molecularCriterium =
            ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot)).addFusions(actionableFusion).build()
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(brafMolecularTestVariant), fusions = listOf(molecularTestFusion)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isNotEmpty
        assertThat(matches).hasSize(2)
        val expectedMatch = ActionabilityMatch(listOf(evidence), mapOf(trial to setOf(molecularCriterium)))
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(expectedMatch)
        assertThat(matches[molecularTestFusion]).isEqualTo(expectedMatch)
    }

    @Test
    fun `Should not match combined evidence having hotspot and fusion when only partially present in molecular test`() {
        val molecularCriterium =
            ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot)).addFusions(actionableFusion).build()
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium
        )
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(krasMolecularTestVariant),  // different variant
                fusions = listOf(molecularTestFusion)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match combined evidence with multiple hotspots when only single hotspot matches`() {
        val molecularCriterium =
            ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot, krasActionableHotspot)).build()
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium
        )
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match virus characteristics`() {
        val molecularCriterium = ImmutableMolecularCriterium.builder().characteristics(
            listOf(
                TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.HPV_POSITIVE)
            )
        ).build()
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val virus = TestMolecularFactory.createMinimalVirus().copy(type = VirusType.HPV, driverLikelihood = DriverLikelihood.HIGH)
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                viruses = listOf(virus)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches[virus]).isEqualTo(ActionabilityMatch(listOf(evidence), mapOf(trial to setOf(molecularCriterium))))
    }

    @Test
    fun `Should match hla`() {
        val molecularCriterium = TestServeMolecularFactory.createHlaCriterium(
            baseActionableEvent = TestServeMolecularFactory.createActionableEvent(), hlaAllele = "A*02:01"
        )
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium
        )
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest()

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match multiple evidences and trials to same hotspot`() {
        val molecularCriterium = ImmutableMolecularCriterium.builder().addAllHotspots(listOf(brafActionableHotspot)).build()
        val evidence1 = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium,
            treatment = "Treatment 1",
        )

        val evidence2 = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium,
            treatment = "Treatment 2",
        )

        val trial1 = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))
        val trial2 = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))

        val matcher = ActionabilityMatcher(listOf(evidence1, evidence2), listOf(trial1, trial2))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(
            ActionabilityMatch(
                listOf(evidence1, evidence2), mapOf(trial1 to setOf(molecularCriterium), trial2 to setOf(molecularCriterium))
            )
        )
    }

    @Test
    fun `Should not match msi stable evidence with msi unstable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val evidenceMatches = matcher.match(panelTestWithMsi(isUnstable = true))
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should not match msi stable evidence with tumor of unknown msi stability`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val emptyTest = TestMolecularFactory.createMinimalPanelTest()
        assertThat(emptyTest.characteristics.microsatelliteStability).isNull()  // test precondition

        val evidenceMatches = matcher.match(emptyTest)
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should match combined msi and driver evidence when both present`() {
        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(brafActionableHotspot)
            .addCharacteristics(TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)).build()
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium,
        )
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = panelTestWithMsi(isUnstable = true).copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(2)
        val actionabilityMatch = actionabilityMatch(evidence, trial)
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(actionabilityMatch)
        assertThat(matches[molecularTest.characteristics.microsatelliteStability!!]).isEqualTo(actionabilityMatch)
    }

    @Test
    fun `Should not match combined msi and driver evidence if only one present`() {
        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(brafActionableHotspot)
            .addCharacteristics(TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)).build()
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = molecularCriterium,
        )
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(molecularCriterium))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant))
        )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should match ms unstable evidence with ms unstable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val panelRecord = panelTestWithMsi(isUnstable = true)
        val matches = matcher.match(panelRecord)
        assertThat(matches).hasSize(1)
        assertThat(matches[panelRecord.characteristics.microsatelliteStability!!]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match ms stable evidence with ms stable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val panelRecord = panelTestWithMsi(isUnstable = false)
        val matches = matcher.match(panelRecord)
        assertThat(matches).hasSize(1)
        assertThat(matches[panelRecord.characteristics.microsatelliteStability!!]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match ms unstable evidence or trials with ms stable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val matches = matcher.match(panelTestWithMsi(isUnstable = false))
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match ms stable evidence or trials with ms unstable tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val matches = matcher.match(panelTestWithMsi(isUnstable = true))
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not ms unstable evidence and trials with unknown msi tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val matches = matcher.match(TestMolecularFactory.createMinimalPanelTest())
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not ms stable evidence and trials with unknown msi tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_STABLE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val matches = matcher.match(TestMolecularFactory.createMinimalPanelTest())
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match high tml evidence or trials with high tml tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalLoad = TumorMutationalLoad(score = 185, isHigh = true, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalLoad = tumorMutationalLoad
                )
            )
        )
        assertThat(matches).hasSize(1)
        assertThat(matches[tumorMutationalLoad]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match low tml evidence or trials with low tml tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalLoad = TumorMutationalLoad(score = 5, isHigh = false, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalLoad = tumorMutationalLoad
                )
            )
        )
        assertThat(matches).hasSize(1)
        assertThat(matches[tumorMutationalLoad]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match high tml evidence or trials with low tml tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalLoad = TumorMutationalLoad(score = 5, isHigh = false, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalLoad = tumorMutationalLoad
                )
            )
        )
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match low tml evidence or trials with high tml tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalLoad = TumorMutationalLoad(score = 185, isHigh = true, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalLoad = tumorMutationalLoad
                )
            )
        )
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match high tmb evidence and trials with high tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalBurden = TumorMutationalBurden(
            score = 100.0, isHigh = true, evidence = TestClinicalEvidenceFactory.createEmpty()
        )
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalBurden = tumorMutationalBurden
                )
            )
        )
        assertThat(matches).hasSize(1)
        assertThat(matches[tumorMutationalBurden]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match low tmb evidence and trials with low tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalBurden = TumorMutationalBurden(score = 1.0, isHigh = false, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalBurden = tumorMutationalBurden
                )
            )
        )
        assertThat(matches).hasSize(1)
        assertThat(matches[tumorMutationalBurden]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match high tmb evidence and trials with low tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalBurden = TumorMutationalBurden(score = 1.0, isHigh = false, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalBurden = tumorMutationalBurden
                )
            )
        )
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match low tmb evidence and trials with high tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val tumorMutationalBurden =
            TumorMutationalBurden(score = 100.0, isHigh = true, evidence = TestClinicalEvidenceFactory.createEmpty())
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    tumorMutationalBurden = tumorMutationalBurden
                )
            )
        )
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match high tmb evidence and trials with unknown tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_BURDEN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val matches = matcher.match(TestMolecularFactory.createMinimalPanelTest())
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should not match low tmb evidence and trials with unknown tmb tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))
        val matches = matcher.match(TestMolecularFactory.createMinimalPanelTest())
        assertThat(matches).isEmpty()
    }

    private fun actionabilityMatch(
        evidence: EfficacyEvidence,
        trial: ActionableTrial
    ) = ActionabilityMatch(
        listOf(evidence),
        mapOf(trial to setOf(evidence.molecularCriterium()))
    )

    @Test
    fun `Should match hrd evidence with hrd tumor`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val homologousRecombination = minimalHrdCharacteristic().copy(isDeficient = true)
        val matches = matcher.match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                    homologousRecombination = homologousRecombination
                )
            )
        )
        assertThat(matches).hasSize(1)
        assertThat(matches[homologousRecombination]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match gene evidence with variant on that gene`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val variant = brafMolecularTestVariant.copy(
            canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(variant)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[variant]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match gene evidence for ineligible event with variant on that gene`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.FUSION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(
                    brafMolecularTestVariant.copy(
                        canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
                    )
                )
            )
        )

        val evidenceMatches = matcher.match(molecularTest)
        assertThat(evidenceMatches).isEmpty()
    }

    @Test
    fun `Should match absence of protein evidence with variant on that gene if MMR gene`() {
        val gene = GeneConstants.MMR_GENES.first()
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = gene, geneEvent = GeneEvent.ABSENCE_OF_PROTEIN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val variant = brafMolecularTestVariant.copy(
            gene = gene, canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(variant)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[variant]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match absence of protein evidence with variant on that gene if non-MMR gene`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ABSENCE_OF_PROTEIN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val variant = brafMolecularTestVariant.copy(
            canonicalImpact = TestMolecularFactory.createMinimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE)
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(variant)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match promiscuous fusion`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "EGFR", geneEvent = GeneEvent.FUSION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                fusions = listOf(molecularTestFusion)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[molecularTestFusion]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match gene disruption`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val disruption =
            TestMolecularFactory.createMinimalDisruption().copy(gene = "BRAF", isReportable = true, geneRole = GeneRole.UNKNOWN)
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(disruptions = listOf(disruption))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match homozygous disruption`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "BRAF", geneEvent = GeneEvent.ANY_MUTATION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val homDisruption =
            TestMolecularFactory.createMinimalHomozygousDisruption().copy(gene = "BRAF", isReportable = true, geneRole = GeneRole.UNKNOWN)
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(homozygousDisruptions = listOf(homDisruption))
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[homDisruption]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match absence of protein evidence with hom disruption if MMR gene`() {
        val gene = GeneConstants.MMR_GENES.first()
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = gene, geneEvent = GeneEvent.ABSENCE_OF_PROTEIN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val homDisruption = TestMolecularFactory.createMinimalHomozygousDisruption().copy(gene = gene, isReportable = true)
        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(homozygousDisruptions = listOf(homDisruption)))

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[homDisruption]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match absence of protein with hom disruption if non-MMR gene`() {
        val gene = "some gene"
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = gene, geneEvent = GeneEvent.ABSENCE_OF_PROTEIN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val homDisruption = TestMolecularFactory.createMinimalHomozygousDisruption().copy(gene = gene, isReportable = true)
        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(homozygousDisruptions = listOf(homDisruption)))

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should match copy number amplification`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "EGFR", geneEvent = GeneEvent.AMPLIFICATION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "EGFR",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_GAIN),
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                copyNumbers = listOf(copyNumber)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[copyNumber]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match copy number deletion`() {
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = "EGFR", geneEvent = GeneEvent.DELETION)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = "EGFR",
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL),
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                copyNumbers = listOf(copyNumber)
            )
        )

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[copyNumber]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should match absence of protein evidence with deletion if MMR gene`() {
        val gene = GeneConstants.MMR_GENES.first()
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = gene, geneEvent = GeneEvent.ABSENCE_OF_PROTEIN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = gene,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.PARTIAL_DEL),
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = listOf(copyNumber)))

        val matches = matcher.match(molecularTest)
        assertThat(matches).hasSize(1)
        assertThat(matches[copyNumber]).isEqualTo(actionabilityMatch(evidence, trial))
    }

    @Test
    fun `Should not match absence of protein with deletion if non-MMR gene`() {
        val gene = "some gene"
        val evidence = TestServeEvidenceFactory.createEvidenceForGene(gene = gene, geneEvent = GeneEvent.ABSENCE_OF_PROTEIN)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(evidence.molecularCriterium()))
        val matcher = matcherFactory(listOf(evidence), listOf(trial))

        val copyNumber = TestMolecularFactory.createMinimalCopyNumber().copy(
            gene = gene,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(CopyNumberType.FULL_DEL),
        )
        val molecularTest = TestMolecularFactory.createMinimalPanelTest()
            .copy(drivers = TestMolecularFactory.createMinimalTestDrivers().copy(copyNumbers = listOf(copyNumber)))

        val matches = matcher.match(molecularTest)
        assertThat(matches).isEmpty()
    }

    @Test
    fun `Should create Success MatchResult from Actionable`() {
        assertThat(successWhenNotEmpty(listOf(brafMolecularTestVariant))).isEqualTo(
            ActionabilityMatchResult.Success(
                listOf(brafMolecularTestVariant)
            )
        )
    }

    @Test
    fun `Should create Failure MatchResult from empty list`() {
        assertThat(successWhenNotEmpty(emptyList())).isEqualTo(ActionabilityMatchResult.Failure)
    }

    @Test
    fun `Should match trials in a realistic scenario`() {
        val nonApplicableGene = "TP53"
        val nonApplicableGeneCriterium = TestServeMolecularFactory.createGeneCriterium(gene = nonApplicableGene)
        val tmbHighCharacteristic = TestServeMolecularFactory.createCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
        val nonApplicableGeneAndMatchingCharacteristicCriterium =
            (TestServeMolecularFactory.createGeneCriterium(gene = nonApplicableGene) as ImmutableMolecularCriterium).withCharacteristics(
                tmbHighCharacteristic
            )
        val matchingHotspotAndCharacteristicCriterium =
            ImmutableMolecularCriterium.builder().addHotspots(brafActionableHotspot).addCharacteristics(tmbHighCharacteristic).build()
        val matchingHotspotCriterium = ImmutableMolecularCriterium.builder().addHotspots(brafActionableHotspot).build()

        val trial = TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(
                nonApplicableGeneCriterium,
                nonApplicableGeneAndMatchingCharacteristicCriterium,
                matchingHotspotAndCharacteristicCriterium,
                matchingHotspotCriterium
            )
        )
        val highTmb = TumorMutationalBurden(11.0, true, ClinicalEvidence(emptySet(), emptySet()))
        val matches = matcherFactory(emptyList(), listOf(trial)).match(
            TestMolecularFactory.createMinimalPanelTest().copy(
                drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(brafMolecularTestVariant)),
                characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(tumorMutationalBurden = highTmb)
            )
        )

        val filteredTrial = TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(
                matchingHotspotAndCharacteristicCriterium,
                matchingHotspotCriterium
            )
        )

        assertThat(matches).hasSize(2)
        assertThat(matches[brafMolecularTestVariant]).isEqualTo(
            ActionabilityMatch(
                emptyList(),
                mapOf(filteredTrial to setOf(matchingHotspotAndCharacteristicCriterium, matchingHotspotCriterium))
            )
        )
        assertThat(matches[highTmb]).isEqualTo(
            ActionabilityMatch(
                emptyList(),
                mapOf(filteredTrial to setOf(matchingHotspotAndCharacteristicCriterium))
            )
        )
    }

    private fun matcherFactory(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial> = emptyList()): ActionabilityMatcher {
        return ActionabilityMatcherFactory.create(serveRecord(evidences, trials))
    }

    private fun serveRecord(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial> = emptyList()): ServeRecord {
        return ImmutableServeRecord.builder().knownEvents(ImmutableKnownEvents.builder().build()).addAllEvidences(evidences)
            .addAllTrials(trials).build()
    }

    private fun panelTestWithMsi(isUnstable: Boolean): MolecularTest {
        return TestMolecularFactory.createMinimalPanelTest().copy(
            characteristics = TestMolecularFactory.createMinimalTestCharacteristics().copy(
                microsatelliteStability = MicrosatelliteStability(
                    microsatelliteIndelsPerMb = 0.0,
                    isUnstable = isUnstable,
                    evidence = TestClinicalEvidenceFactory.createEmpty()
                )
            )
        )
    }

    private fun minimalHrdCharacteristic(): HomologousRecombination {
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
