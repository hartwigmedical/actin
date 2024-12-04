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
        val actionableHotspot = ImmutableActionableHotspot.builder().from(createActionableEvent())
            .gene(gene)
            .chromosome(chromosome)
            .position(position)
            .ref(ref)
            .alt(alt)
            .build()

        return ImmutableMolecularCriterium.builder().addHotspots(actionableHotspot).build()
    }

    fun createCodon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        val actionableCodon = ImmutableActionableRange.builder().from(createActionableEvent())
            .gene(gene)
            .chromosome(chromosome)
            .start(start)
            .end(end)
            .applicableMutationType(applicableMutationType)
            .build()

        return ImmutableMolecularCriterium.builder().addCodons(actionableCodon).build()
    }

    fun createExon(
        gene: String = "",
        chromosome: String = "",
        start: Int = 0,
        end: Int = 0,
        applicableMutationType: MutationType = MutationType.ANY
    ): MolecularCriterium {
        val actionableExon = ImmutableActionableRange.builder().from(createActionableEvent())
            .gene(gene)
            .chromosome(chromosome)
            .start(start)
            .end(end)
            .applicableMutationType(applicableMutationType)
            .build()

        return ImmutableMolecularCriterium.builder().addExons(actionableExon).build()
    }

    fun createGene(gene: String = "", geneEvent: GeneEvent = GeneEvent.ANY_MUTATION, sourceEvent: String = ""): MolecularCriterium {
        val actionableGene = ImmutableActionableGene.builder().from(createActionableEvent())
            .sourceEvent(sourceEvent)
            .event(geneEvent)
            .gene(gene)
            .build()

        return ImmutableMolecularCriterium.builder().addGenes(actionableGene).build()
    }

    fun createFusion(geneUp: String = "", geneDown: String = "", minExonUp: Int? = null, maxExonUp: Int? = null): MolecularCriterium {
        val actionableFusion = ImmutableActionableFusion.builder().from(createActionableEvent())
            .geneUp(geneUp)
            .geneDown(geneDown)
            .minExonUp(minExonUp)
            .maxExonUp(maxExonUp)
            .build()

        return ImmutableMolecularCriterium.builder().addFusions(actionableFusion).build()
    }

    fun createCharacteristic(type: TumorCharacteristicType = TumorCharacteristicType.MICROSATELLITE_STABLE): MolecularCriterium {
        val actionableCharacteristic = ImmutableActionableCharacteristic.builder().from(createActionableEvent()).type(type).build()

        return ImmutableMolecularCriterium.builder().addCharacteristics(actionableCharacteristic).build()
    }

    fun createHLA(): MolecularCriterium {
        val actionableHLA = ImmutableActionableHLA.builder().from(createActionableEvent()).hlaAllele("").build()

        return ImmutableMolecularCriterium.builder().addHla(actionableHLA).build()
    }

    fun createCombined(): MolecularCriterium {
        return ImmutableMolecularCriterium.builder()
            .from(createHotspot())
            .from(createCodon())
            .from(createExon())
            .from(createGene())
            .from(createFusion())
            .from(createCharacteristic())
            .from(createHLA())
            .build()
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