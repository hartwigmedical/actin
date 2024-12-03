package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory.create
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory.createEvidenceForExon
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory.createEvidenceForFusion
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractHotspot
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.extractRange
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.fusionFilter
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction.geneFilter
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.common.ImmutableIndication
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ImmutableActionableRange
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val INDICATION_1 = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("name1").doid("doid1").build())
    .excludedSubTypes(emptySet()).build()
private val INDICATION_2 = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("name2").doid("doid2").build())
    .excludedSubTypes(emptySet()).build()
private val MOLECULAR_CRITERIUM_1 = TestServeMolecularFactory.createHotspot()
private val MOLECULAR_CRITERIUM_2 = TestServeMolecularFactory.createGene()

class ActionableEventsExtractionTest {

    @Test
    fun `Can extract hotspot`() {
        val actionableHotspot =
            ImmutableActionableHotspot.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("gene")
                .chromosome("chromosome").position(0).ref("ref").alt("alt").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(actionableHotspot).build()
        val efficacyEvidence = create(molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(setOf(molecularCriterium))
        assertThat(extractHotspot(efficacyEvidence)).isEqualTo(actionableHotspot)
        assertThat(extractHotspot(actionableTrial)).isEqualTo(actionableHotspot)
    }

    @Test
    fun `Can extract range`() {
        val actionableRange = ImmutableActionableRange.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("gene")
            .chromosome("chromosome").start(0).end(1).applicableMutationType(MutationType.ANY).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCodons(actionableRange).build()
        val efficacyEvidence = create(molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(setOf(molecularCriterium))
        assertThat(extractRange(efficacyEvidence)).isEqualTo(actionableRange)
        assertThat(extractRange(actionableTrial)).isEqualTo(actionableRange)
    }

    @Test
    fun `Can extract gene`() {
        val actionableGene =
            ImmutableActionableGene.builder().from(TestServeMolecularFactory.createActionableEvent()).event(GeneEvent.FUSION)
                .gene("gene").sourceEvent("sourceEvent").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addGenes(actionableGene).build()
        val efficacyEvidence = create(molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(setOf(molecularCriterium))
        assertThat(ActionableEventsExtraction.extractGene(efficacyEvidence)).isEqualTo(actionableGene)
        assertThat(ActionableEventsExtraction.extractGene(actionableTrial)).isEqualTo(actionableGene)
    }

    @Test
    fun `Can extract fusion`() {
        val actionableFusion =
            ImmutableActionableFusion.builder().from(TestServeMolecularFactory.createActionableEvent()).geneUp("geneUp")
                .geneDown("geneDown").minExonUp(0).maxExonUp(0).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addFusions(actionableFusion).build()
        val efficacyEvidence = create(molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(setOf(molecularCriterium))
        assertThat(ActionableEventsExtraction.extractFusion(efficacyEvidence)).isEqualTo(actionableFusion)
        assertThat(ActionableEventsExtraction.extractFusion(actionableTrial)).isEqualTo(actionableFusion)
    }

    @Test
    fun `Can extract characteristic`() {
        val actionableCharacteristic =
            ImmutableActionableCharacteristic.builder().from(TestServeMolecularFactory.createActionableEvent())
                .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCharacteristics(actionableCharacteristic).build()
        val efficacyEvidence = create(molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(setOf(molecularCriterium))

        assertThat(ActionableEventsExtraction.extractCharacteristic(efficacyEvidence)).isEqualTo(actionableCharacteristic)
        assertThat(ActionableEventsExtraction.extractCharacteristic(actionableTrial)).isEqualTo(actionableCharacteristic)
    }

    @Test
    fun `Can filter efficacy evidences`() {
        val efficacyEvidence1 = createEvidenceForExon()
        val efficacyEvidence2 = createEvidenceForFusion()
        val efficacyEvidence3 = createEvidenceForFusion()
        val filteredEfficacyEvidence = ActionableEventsExtraction.filterEfficacyEvidence(
            listOf(efficacyEvidence1, efficacyEvidence2, efficacyEvidence3),
            fusionFilter()
        )
        assertThat(filteredEfficacyEvidence).containsExactly(efficacyEvidence2, efficacyEvidence3)
    }

    @Test
    fun `Can filter trials`() {
        val actionableTrial1 = TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_1))
        val actionableTrial2 = TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_2))
        val actionableTrial3 = TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_1, MOLECULAR_CRITERIUM_2))
        val filteredTrials =
            ActionableEventsExtraction.filterTrials(listOf(actionableTrial1, actionableTrial2, actionableTrial3), geneFilter())
        assertThat(filteredTrials).containsExactly(actionableTrial2, actionableTrial3)
    }

    @Test
    fun `Can expand trials`() {
        val actionableTrial =
            TestServeTrialFactory.create(
                setOf(MOLECULAR_CRITERIUM_1, MOLECULAR_CRITERIUM_2),
                indications = setOf(INDICATION_1, INDICATION_2)
            )
        val expandedTrial = ActionableEventsExtraction.expandTrials(listOf(actionableTrial))
        val expandedTrial1 =
            ImmutableActionableTrial.builder()
                .from(TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_1), indications = setOf(INDICATION_1)))
                .build()
        val expandedTrial2 =
            ImmutableActionableTrial.builder()
                .from(TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_1), indications = setOf(INDICATION_2)))
                .build()
        val expandedTrial3 =
            ImmutableActionableTrial.builder()
                .from(TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_2), indications = setOf(INDICATION_1)))
                .build()
        val expandedTrial4 =
            ImmutableActionableTrial.builder()
                .from(TestServeTrialFactory.create(setOf(MOLECULAR_CRITERIUM_2), indications = setOf(INDICATION_2)))
                .build()

        assertThat(expandedTrial).containsAll(listOf(expandedTrial1, expandedTrial2, expandedTrial3, expandedTrial4))
    }
}