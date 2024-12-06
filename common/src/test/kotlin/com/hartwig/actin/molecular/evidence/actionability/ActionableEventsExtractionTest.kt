package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
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

private val MOLECULAR_CRITERIUM_1 = TestServeMolecularFactory.createHotspotCriterium()
private val MOLECULAR_CRITERIUM_2 = TestServeMolecularFactory.createGeneCriterium()

class ActionableEventsExtractionTest {

    @Test
    fun `Can extract hotspot`() {
        val actionableHotspot =
            ImmutableActionableHotspot.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("gene")
                .chromosome("chromosome").position(0).ref("ref").alt("alt").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(actionableHotspot).build()
        val efficacyEvidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium))

        assertThat(ActionableEventsExtraction.extractHotspot(efficacyEvidence)).isEqualTo(actionableHotspot)
        assertThat(ActionableEventsExtraction.extractHotspots(actionableTrial)).isEqualTo(setOf(actionableHotspot))
    }

    @Test
    fun `Can extract range`() {
        val actionableRange = ImmutableActionableRange.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("gene")
            .chromosome("chromosome").start(0).end(1).applicableMutationType(MutationType.ANY).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCodons(actionableRange).build()
        val efficacyEvidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium))

        assertThat(ActionableEventsExtraction.extractRange(efficacyEvidence)).isEqualTo(actionableRange)
        assertThat(ActionableEventsExtraction.extractRanges(actionableTrial)).isEqualTo(setOf(actionableRange))
    }

    @Test
    fun `Can extract gene`() {
        val actionableGene =
            ImmutableActionableGene.builder().from(TestServeMolecularFactory.createActionableEvent()).event(GeneEvent.FUSION)
                .gene("gene").sourceEvent("sourceEvent").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addGenes(actionableGene).build()
        val efficacyEvidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium))

        assertThat(ActionableEventsExtraction.extractGene(efficacyEvidence)).isEqualTo(actionableGene)
        assertThat(ActionableEventsExtraction.extractGenes(actionableTrial)).isEqualTo(setOf(actionableGene))
    }

    @Test
    fun `Can extract fusion`() {
        val actionableFusion =
            ImmutableActionableFusion.builder().from(TestServeMolecularFactory.createActionableEvent()).geneUp("geneUp")
                .geneDown("geneDown").minExonUp(0).maxExonUp(0).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addFusions(actionableFusion).build()
        val efficacyEvidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium))

        assertThat(ActionableEventsExtraction.extractFusion(efficacyEvidence)).isEqualTo(actionableFusion)
        assertThat(ActionableEventsExtraction.extractFusions(actionableTrial)).isEqualTo(setOf(actionableFusion))
    }

    @Test
    fun `Can extract characteristic`() {
        val actionableCharacteristic =
            ImmutableActionableCharacteristic.builder().from(TestServeMolecularFactory.createActionableEvent())
                .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCharacteristics(actionableCharacteristic).build()
        val efficacyEvidence = TestServeEvidenceFactory.create(molecularCriterium = molecularCriterium)
        val actionableTrial = TestServeTrialFactory.create(molecularCriteria = setOf(molecularCriterium))

        assertThat(ActionableEventsExtraction.extractCharacteristic(efficacyEvidence)).isEqualTo(actionableCharacteristic)
        assertThat(ActionableEventsExtraction.extractCharacteristics(actionableTrial)).isEqualTo(setOf(actionableCharacteristic))
    }

    @Test
    fun `Can filter efficacy evidences`() {
        val efficacyEvidence1 = TestServeEvidenceFactory.createEvidenceForExon()
        val efficacyEvidence2 = TestServeEvidenceFactory.createEvidenceForFusion()
        val efficacyEvidence3 = TestServeEvidenceFactory.createEvidenceForFusion()

        val filteredEfficacyEvidence = ActionableEventsExtraction.extractEfficacyEvidence(
            listOf(efficacyEvidence1, efficacyEvidence2, efficacyEvidence3),
            ActionableEventsExtraction.fusionFilter()
        )
        assertThat(filteredEfficacyEvidence).containsExactly(efficacyEvidence2, efficacyEvidence3)
    }

    @Test
    fun `Can filter trials`() {
        val actionableTrial1 = TestServeTrialFactory.create(molecularCriteria = setOf(MOLECULAR_CRITERIUM_1))
        val actionableTrial2 = TestServeTrialFactory.create(molecularCriteria = setOf(MOLECULAR_CRITERIUM_2))
        val actionableTrial3 = TestServeTrialFactory.create(molecularCriteria = setOf(MOLECULAR_CRITERIUM_1, MOLECULAR_CRITERIUM_2))
        val filteredTrials =
            ActionableEventsExtraction.extractTrials(
                listOf(actionableTrial1, actionableTrial2, actionableTrial3),
                ActionableEventsExtraction.geneFilter()
            )

        assertThat(filteredTrials).containsExactly(actionableTrial2, actionableTrial3)
    }
}