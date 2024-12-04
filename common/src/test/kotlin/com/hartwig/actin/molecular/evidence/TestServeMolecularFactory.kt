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
import com.hartwig.serve.datamodel.molecular.immuno.ImmutableActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ImmutableActionableRange
import java.time.LocalDate

object TestServeMolecularFactory {

    fun createHotspot(
        gene: String = "",
        chromosome: String = "",
        position: Int = 0,
        ref: String = "",
        alt: String = ""
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addHotspots(
            ImmutableActionableHotspot.builder().from(createActionableEvent()).gene(gene).chromosome(chromosome).position(position).ref(ref)
                .alt(alt).build()
        ).build()
    }

    fun createCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addCodons(
            ImmutableActionableRange.builder().from(createActionableEvent()).gene(gene).chromosome(chromosome).start(start).end(end)
                .applicableMutationType(applicableMutationType).build()
        ).build()
    }

    fun createExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addExons(
            ImmutableActionableRange.builder().from(createActionableEvent()).gene(gene).chromosome(chromosome).start(start).end(end)
                .applicableMutationType(applicableMutationType).build()
        ).build()
    }

    fun createGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION, sourceEvent: String = ""): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addGenes(
                ImmutableActionableGene.builder().from(createActionableEvent()).event(geneEvent).gene(gene).sourceEvent(sourceEvent).build()
            ).build()
    }

    fun createFusion(geneUp: String = "", geneDown: String = "", minExonUp: Int? = null, maxExonUp: Int? = null): MolecularCriterium {
        return ImmutableMolecularCriterium.builder().addFusions(
            ImmutableActionableFusion.builder().from(createActionableEvent()).geneUp(geneUp).geneDown(geneDown).minExonUp(minExonUp)
                .maxExonUp(maxExonUp).build()
        ).build()
    }

    fun createCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addCharacteristics(ImmutableActionableCharacteristic.builder().from(createActionableEvent()).type(type).build()).build()
    }

    fun createHLA(): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .addHla(ImmutableActionableHLA.builder().from(createActionableEvent()).hlaAllele("").build()).build()
    }

    fun createActionableEvent(): ActionableEvent {
        return object : ActionableEvent {
            override fun sourceDate(): LocalDate {
                return LocalDate.of(2021, 2, 3)
            }

            override fun sourceEvent(): String {
                return ""
            }

            override fun sourceUrls(): Set<String> {
                return setOf("https://url")
            }
        }
    }
}