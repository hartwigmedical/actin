package com.hartwig.actin.molecular.evidence.actionability

import com.google.common.collect.Sets
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createActionableTrial
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createEfficacyEvidence
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createEfficacyEvidenceWithExon
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createEfficacyEvidenceWithFusion
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val INDICATION_1 = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("name1").doid("doid1").build())
    .excludedSubTypes(emptySet()).build()
private val INDICATION_2 = ImmutableIndication.builder().applicableType(ImmutableCancerType.builder().name("name2").doid("doid2").build())
    .excludedSubTypes(emptySet()).build()
private val MOLECULAR_CRITERIUM_1 = TestServeActionabilityFactory.createHotspot()
private val MOLECULAR_CRITERIUM_2 = TestServeActionabilityFactory.createGene()

class ActionableEventsExtractionTest {

    @Test
    fun `Can extract hotspot`() {
        val actionableHotspot =
            ImmutableActionableHotspot.builder().from(TestServeActionabilityFactory.createActionableEvent()).gene("gene")
                .chromosome("chromosome").position(0).ref("ref").alt("alt").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(actionableHotspot).build()
        val efficacyEvidence = createEfficacyEvidence(molecularCriterium)
        val actionableTrial = createActionableTrial(Sets.newHashSet(molecularCriterium))
        assertThat(extractHotspot(efficacyEvidence)).isEqualTo(actionableHotspot)
        assertThat(extractHotspot(actionableTrial)).isEqualTo(actionableHotspot)
    }

    @Test
    fun `Can extract range`() {
        val actionableRange = ImmutableActionableRange.builder().from(TestServeActionabilityFactory.createActionableEvent()).gene("gene")
            .chromosome("chromosome").start(0).end(1).applicableMutationType(MutationType.ANY).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCodons(actionableRange).build()
        val efficacyEvidence = createEfficacyEvidence(molecularCriterium)
        val actionableTrial = createActionableTrial(Sets.newHashSet(molecularCriterium))
        assertThat(extractRange(efficacyEvidence)).isEqualTo(actionableRange)
        assertThat(extractRange(actionableTrial)).isEqualTo(actionableRange)
    }

    @Test
    fun `Can extract gene`() {
        val actionableGene =
            ImmutableActionableGene.builder().from(TestServeActionabilityFactory.createActionableEvent()).event(GeneEvent.FUSION)
                .gene("gene").sourceEvent("sourceEvent").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addGenes(actionableGene).build()
        val efficacyEvidence = createEfficacyEvidence(molecularCriterium)
        val actionableTrial = createActionableTrial(Sets.newHashSet(molecularCriterium))
        assertThat(ActionableEventsExtraction.extractGene(efficacyEvidence)).isEqualTo(actionableGene)
        assertThat(ActionableEventsExtraction.extractGene(actionableTrial)).isEqualTo(actionableGene)
    }

    @Test
    fun `Can extract fusion`() {
        val actionableFusion =
            ImmutableActionableFusion.builder().from(TestServeActionabilityFactory.createActionableEvent()).geneUp("geneUp")
                .geneDown("geneDown").minExonUp(0).maxExonUp(0).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addFusions(actionableFusion).build()
        val efficacyEvidence = createEfficacyEvidence(molecularCriterium)
        val actionableTrial = createActionableTrial(Sets.newHashSet(molecularCriterium))
        assertThat(ActionableEventsExtraction.extractFusion(efficacyEvidence)).isEqualTo(actionableFusion)
        assertThat(ActionableEventsExtraction.extractFusion(actionableTrial)).isEqualTo(actionableFusion)
    }

    @Test
    fun `Can extract characteristic`() {
        val actionableCharacteristic =
            ImmutableActionableCharacteristic.builder().from(TestServeActionabilityFactory.createActionableEvent())
                .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCharacteristics(actionableCharacteristic).build()
        val efficacyEvidence = createEfficacyEvidence(molecularCriterium)
        val actionableTrial = createActionableTrial(Sets.newHashSet(molecularCriterium))
        assertThat(ActionableEventsExtraction.extractCharacteristic(efficacyEvidence)).isEqualTo(actionableCharacteristic)
        assertThat(ActionableEventsExtraction.extractCharacteristic(actionableTrial)).isEqualTo(actionableCharacteristic)
    }

    @Test
    fun `Can filter efficacy evidences`() {
        val efficacyEvidence1 = createEfficacyEvidenceWithExon()
        val efficacyEvidence2 = createEfficacyEvidenceWithFusion()
        val efficacyEvidence3 = createEfficacyEvidenceWithFusion()
        val filteredEfficacyEvidence = ActionableEventsExtraction.filterEfficacyEvidence(
            listOf(efficacyEvidence1, efficacyEvidence2, efficacyEvidence3),
            fusionFilter()
        )
        assertThat(filteredEfficacyEvidence).containsExactly(efficacyEvidence2, efficacyEvidence3)
    }

    @Test
    fun `Can filter trials`() {
        val actionableTrial1 = createActionableTrial(Sets.newHashSet(MOLECULAR_CRITERIUM_1))
        val actionableTrial2 = createActionableTrial(Sets.newHashSet(MOLECULAR_CRITERIUM_2))
        val actionableTrial3 = createActionableTrial(Sets.newHashSet(MOLECULAR_CRITERIUM_1, MOLECULAR_CRITERIUM_2))
        val filteredTrials =
            ActionableEventsExtraction.filterTrials(listOf(actionableTrial1, actionableTrial2, actionableTrial3), geneFilter())
        assertThat(filteredTrials).containsExactly(actionableTrial2, actionableTrial3)
    }

    @Test
    fun `Can expand trials`() {
        val actionableTrial = createActionableTrial(
            Sets.newHashSet(MOLECULAR_CRITERIUM_1, MOLECULAR_CRITERIUM_2),
            indications = Sets.newHashSet(INDICATION_1, INDICATION_2)
        )
        val expandedTrial = ActionableEventsExtraction.expandTrials(listOf(actionableTrial))
        assertThat(expandedTrial[0].indications()).containsExactly(INDICATION_1)
        assertThat(expandedTrial[0].anyMolecularCriteria()).containsExactly(MOLECULAR_CRITERIUM_1)
        assertThat(expandedTrial[1].indications()).containsExactly(INDICATION_2)
        assertThat(expandedTrial[1].anyMolecularCriteria()).containsExactly(MOLECULAR_CRITERIUM_1)
        assertThat(expandedTrial[2].indications()).containsExactly(INDICATION_1)
        assertThat(expandedTrial[2].anyMolecularCriteria()).containsExactly(MOLECULAR_CRITERIUM_2)
        assertThat(expandedTrial[3].indications()).containsExactly(INDICATION_2)
        assertThat(expandedTrial[3].anyMolecularCriteria()).containsExactly(MOLECULAR_CRITERIUM_2)
    }
}