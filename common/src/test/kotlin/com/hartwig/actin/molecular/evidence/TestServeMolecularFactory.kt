package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.characteristic.ImmutableActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableVariantAnnotation
import com.hartwig.serve.datamodel.molecular.hotspot.VariantAnnotation
import com.hartwig.serve.datamodel.molecular.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ImmutableActionableRange
import java.time.LocalDate

object TestServeMolecularFactory {

    fun createHotspotCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        variants: Set<VariantAnnotation> = setOf(createVariantAnnotation()),
    ): MolecularCriterium {
        val actionableHotspot = ImmutableActionableHotspot.builder().from(baseActionableEvent)
            .addAllVariants(variants)
            .build()
        return ImmutableMolecularCriterium.builder().addHotspots(actionableHotspot).build()
    }

    fun createCodonCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        val actionableCodon = ImmutableActionableRange.builder().from(baseActionableEvent)
            .gene(gene)
            .chromosome(chromosome)
            .start(start)
            .end(end)
            .applicableMutationType(applicableMutationType)
            .build()

        return ImmutableMolecularCriterium.builder().addCodons(actionableCodon).build()
    }

    fun createExonCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        val actionableExon = ImmutableActionableRange.builder().from(baseActionableEvent)
            .gene(gene)
            .chromosome(chromosome)
            .start(start)
            .end(end)
            .applicableMutationType(applicableMutationType)
            .build()

        return ImmutableMolecularCriterium.builder().addExons(actionableExon).build()
    }

    fun createGeneCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        gene: String = "",
        geneEvent: GeneEvent = GeneEvent.ANY_MUTATION,
        sourceEvent: String = ""
    ): MolecularCriterium {
        val actionableGene = ImmutableActionableGene.builder().from(baseActionableEvent)
            .sourceEvent(sourceEvent)
            .event(geneEvent)
            .gene(gene)
            .build()

        return ImmutableMolecularCriterium.builder().addGenes(actionableGene).build()
    }

    fun createFusionCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        geneUp: String = "",
        geneDown: String = "",
        minExonUp: Int? = null,
        maxExonUp: Int? = null
    ): MolecularCriterium {
        val actionableFusion = ImmutableActionableFusion.builder().from(baseActionableEvent)
            .geneUp(geneUp)
            .geneDown(geneDown)
            .minExonUp(minExonUp)
            .maxExonUp(maxExonUp)
            .build()

        return ImmutableMolecularCriterium.builder().addFusions(actionableFusion).build()
    }

    fun createCharacteristicCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE
    ): MolecularCriterium {
        val actionableCharacteristic = ImmutableActionableCharacteristic.builder().from(baseActionableEvent).type(type).build()

        return ImmutableMolecularCriterium.builder().addCharacteristics(actionableCharacteristic).build()
    }

    fun createHlaCriterium(
        baseActionableEvent: ActionableEvent = createActionableEvent(),
        hlaAllele: String = ""
    ): MolecularCriterium {
        val actionableHla = ImmutableActionableHLA.builder().from(baseActionableEvent).hlaAllele(hlaAllele).build()

        return ImmutableMolecularCriterium.builder().addHla(actionableHla).build()
    }

    fun createCombinedCriterium(): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .from(createHotspotCriterium())
            .from(createCodonCriterium())
            .from(createExonCriterium())
            .from(createGeneCriterium())
            .from(createFusionCriterium())
            .from(createCharacteristicCriterium())
            .from(createHlaCriterium())
            .build()
    }

    fun createActionableEvent(
        sourceDate: LocalDate = LocalDate.of(2021, 2, 3),
        sourceEvent: String = "",
        sourceUrls: Set<String> = emptySet()
    ): ActionableEvent {
        return object : ActionableEvent {
            override fun sourceDate(): LocalDate {
                return sourceDate
            }

            override fun sourceEvent(): String {
                return sourceEvent
            }

            override fun sourceUrls(): Set<String> {
                return sourceUrls
            }
        }
    }

    fun createVariantAnnotation(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): VariantAnnotation {
        return ImmutableVariantAnnotation.builder().gene(gene).chromosome(chromosome).position(position).ref(ref).alt(alt).build()
    }
}