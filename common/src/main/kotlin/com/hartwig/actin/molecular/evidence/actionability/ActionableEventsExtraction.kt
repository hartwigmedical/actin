package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import java.util.function.Predicate

object ActionableEventsExtraction {

    fun extractEvent(molecularCriterium: MolecularCriterium): ActionableEvent {
        // TODO (KD): The below assumes that every molecular criterium contains exactly 1 molecular event.
        return when {
            hotspotFilter().test(molecularCriterium) -> {
                molecularCriterium.hotspots().iterator().next()
            }

            codonFilter().test(molecularCriterium) -> {
                molecularCriterium.codons().iterator().next()
            }

            exonFilter().test(molecularCriterium) -> {
                molecularCriterium.exons().iterator().next()
            }

            geneFilter(GeneEvent.values().toSet()).test(molecularCriterium) -> {
                molecularCriterium.genes().iterator().next()
            }

            fusionFilter().test(molecularCriterium) -> {
                molecularCriterium.fusions().iterator().next()
            }

            characteristicsFilter(TumorCharacteristicType.values().toSet()).test(molecularCriterium) -> {
                molecularCriterium.characteristics().iterator().next()
            }

            else -> throw IllegalStateException("Could not extract event for molecular criterium: $molecularCriterium")
        }
    }

    fun extractHotspot(molecularCriterium: MolecularCriterium): ActionableHotspot {
        return molecularCriterium.hotspots().iterator().next()
    }

    fun extractCodon(molecularCriterium: MolecularCriterium): ActionableRange {
        return molecularCriterium.codons().iterator().next()
    }

    fun extractExon(molecularCriterium: MolecularCriterium): ActionableRange {
        return molecularCriterium.exons().iterator().next()
    }

    fun extractGene(molecularCriterium: MolecularCriterium): ActionableGene {
        return molecularCriterium.genes().iterator().next()
    }

    fun extractFusion(molecularCriterium: MolecularCriterium): ActionableFusion {
        return molecularCriterium.fusions().iterator().next()
    }

    fun extractCharacteristic(molecularCriterium: MolecularCriterium): ActionableCharacteristic {
        return molecularCriterium.characteristics().iterator().next()
    }

    fun hotspotFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.hotspots().isNotEmpty() }
    }

    fun codonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.codons().isNotEmpty() }
    }

    fun exonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.exons().isNotEmpty() }
    }

    fun geneFilter(validGeneEvents: Set<GeneEvent>): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.genes().any { validGeneEvents.contains(it.event()) } }
    }

    fun fusionFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.fusions().isNotEmpty() }
    }

    fun characteristicsFilter(validTypes: Set<TumorCharacteristicType>): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.characteristics().any { validTypes.contains(it.type()) } }
    }
}