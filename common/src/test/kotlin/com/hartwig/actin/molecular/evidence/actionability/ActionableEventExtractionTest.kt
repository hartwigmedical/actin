package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.molecular.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ImmutableActionableRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionableEventExtractionTest {

    @Test
    fun `Can extract actionable event for hotspot`() {
        val variantAnnotation = TestServeMolecularFactory.createVariantAnnotation()
        val actionableHotspot = ImmutableActionableHotspot.builder()
            .from(TestServeMolecularFactory.createActionableEvent())
            .addVariants(variantAnnotation)
            .build()

        val molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
            baseActionableEvent = actionableHotspot,
            variants = setOf(variantAnnotation)
        )

        assertThat(ActionableEventExtraction.extractEvent(molecularCriterium)).isEqualTo(actionableHotspot)
    }

    @Test
    fun `Can extract actionable event for codon`() {
        val actionableCodon = ImmutableActionableRange.builder().from(TestServeMolecularFactory.createActionableEvent())
            .gene("gene").chromosome("chromosome").start(0).end(1).applicableMutationType(MutationType.ANY).build()

        val molecularCriterium = TestServeMolecularFactory.createCodonCriterium(
            baseActionableEvent = actionableCodon,
            gene = actionableCodon.gene(),
            chromosome = actionableCodon.chromosome(),
            start = actionableCodon.start(),
            end = actionableCodon.end(),
            applicableMutationType = actionableCodon.applicableMutationType()
        )

        assertThat(ActionableEventExtraction.extractCodon(molecularCriterium)).isEqualTo(actionableCodon)
    }

    @Test
    fun `Can extract actionable event for exon`() {
        val actionableExon = ImmutableActionableRange.builder().from(TestServeMolecularFactory.createActionableEvent())
            .gene("gene").chromosome("chromosome").start(0).end(1).applicableMutationType(MutationType.ANY).build()

        val molecularCriterium = TestServeMolecularFactory.createExonCriterium(
            baseActionableEvent = actionableExon,
            gene = actionableExon.gene(),
            chromosome = actionableExon.chromosome(),
            start = actionableExon.start(),
            end = actionableExon.end(),
            applicableMutationType = actionableExon.applicableMutationType()
        )

        assertThat(ActionableEventExtraction.extractExon(molecularCriterium)).isEqualTo(actionableExon)
    }

    @Test
    fun `Can extract actionable event for gene`() {
        val actionableGene = ImmutableActionableGene.builder().from(TestServeMolecularFactory.createActionableEvent())
            .gene("gene").event(GeneEvent.FUSION).build()

        val molecularCriterium = TestServeMolecularFactory.createGeneCriterium(
            baseActionableEvent = actionableGene,
            gene = actionableGene.gene(),
            geneEvent = actionableGene.event(),
            sourceEvent = actionableGene.sourceEvent()
        )

        assertThat(ActionableEventExtraction.extractEvent(molecularCriterium)).isEqualTo(actionableGene)
    }

    @Test
    fun `Can extract actionable event for fusion`() {
        val actionableFusion = ImmutableActionableFusion.builder().from(TestServeMolecularFactory.createActionableEvent())
            .geneUp("gene-up").geneDown("gene-down").minExonUp(1).maxExonUp(2).build()

        val molecularCriterium = TestServeMolecularFactory.createFusionCriterium(
            baseActionableEvent = actionableFusion,
            geneUp = actionableFusion.geneUp(),
            geneDown = actionableFusion.geneDown(),
            minExonUp = actionableFusion.minExonUp(),
            maxExonUp = actionableFusion.maxExonUp()
        )

        assertThat(ActionableEventExtraction.extractEvent(molecularCriterium)).isEqualTo(actionableFusion)
    }

    @Test
    fun `Can extract actionable event for characteristic`() {
        val actionableCharacteristic = ImmutableActionableCharacteristic.builder().from(TestServeMolecularFactory.createActionableEvent())
            .type(TumorCharacteristicType.LOW_TUMOR_MUTATIONAL_LOAD).build()

        val molecularCriterium = TestServeMolecularFactory.createCharacteristicCriterium(
            baseActionableEvent = actionableCharacteristic,
            type = actionableCharacteristic.type()
        )

        assertThat(ActionableEventExtraction.extractEvent(molecularCriterium)).isEqualTo(actionableCharacteristic)
    }

    @Test
    fun `Can extract actionable event for hla`() {
        val actionableHla = ImmutableActionableHLA.builder().from(TestServeMolecularFactory.createActionableEvent())
            .hlaAllele("hla allele").build()

        val molecularCriterium = TestServeMolecularFactory.createHlaCriterium(
            baseActionableEvent = actionableHla,
            hlaAllele = actionableHla.hlaAllele()
        )

        assertThat(ActionableEventExtraction.extractEvent(molecularCriterium)).isEqualTo(actionableHla)
    }
}