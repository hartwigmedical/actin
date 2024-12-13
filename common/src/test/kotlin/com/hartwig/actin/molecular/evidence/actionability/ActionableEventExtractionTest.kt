package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
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

class ActionableEventExtractionTest {

    @Test
    fun `Can extract hotspot`() {
        val actionableHotspot =
            ImmutableActionableHotspot.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("gene")
                .chromosome("chromosome").position(0).ref("ref").alt("alt").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(actionableHotspot).build()

        assertThat(ActionableEventExtraction.extractHotspot(molecularCriterium)).isEqualTo(actionableHotspot)
    }

    @Test
    fun `Can extract codon`() {
        val actionableCodon = ImmutableActionableRange.builder().from(TestServeMolecularFactory.createActionableEvent()).gene("gene")
            .chromosome("chromosome").start(0).end(1).applicableMutationType(MutationType.ANY).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCodons(actionableCodon).build()

        assertThat(ActionableEventExtraction.extractCodon(molecularCriterium)).isEqualTo(actionableCodon)
    }

    @Test
    fun `Can extract gene`() {
        val actionableGene =
            ImmutableActionableGene.builder().from(TestServeMolecularFactory.createActionableEvent()).event(GeneEvent.FUSION)
                .gene("gene").sourceEvent("sourceEvent").build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addGenes(actionableGene).build()

        assertThat(ActionableEventExtraction.extractGene(molecularCriterium)).isEqualTo(actionableGene)
    }

    @Test
    fun `Can extract fusion`() {
        val actionableFusion =
            ImmutableActionableFusion.builder().from(TestServeMolecularFactory.createActionableEvent()).geneUp("geneUp")
                .geneDown("geneDown").minExonUp(0).maxExonUp(0).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addFusions(actionableFusion).build()

        assertThat(ActionableEventExtraction.extractFusion(molecularCriterium)).isEqualTo(actionableFusion)
    }

    @Test
    fun `Can extract characteristic`() {
        val actionableCharacteristic =
            ImmutableActionableCharacteristic.builder().from(TestServeMolecularFactory.createActionableEvent())
                .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD).build()
        val molecularCriterium = ImmutableMolecularCriterium.builder().addCharacteristics(actionableCharacteristic).build()

        assertThat(ActionableEventExtraction.extractCharacteristic(molecularCriterium)).isEqualTo(actionableCharacteristic)
    }
}