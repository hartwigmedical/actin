package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.immuno.ActionableHLA
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import java.util.function.Predicate

object ActionableEventExtraction {

    fun extractEvent(molecularCriterium: MolecularCriterium): ActionableEvent {
        // TODO (KD): The below assumes that every molecular criterium contains exactly 1 molecular event.
        return when {
            hotspotFilter().test(molecularCriterium) -> {
                extractHotspot(molecularCriterium)
            }

            codonFilter().test(molecularCriterium) -> {
                extractCodon(molecularCriterium)
            }

            exonFilter().test(molecularCriterium) -> {
                extractExon(molecularCriterium)
            }

            geneFilter(GeneEvent.values().toSet()).test(molecularCriterium) -> {
                extractGene(molecularCriterium)
            }

            fusionFilter().test(molecularCriterium) -> {
                extractFusion(molecularCriterium)
            }

            characteristicsFilter(TumorCharacteristicType.values().toSet()).test(molecularCriterium) -> {
                extractCharacteristic(molecularCriterium)
            }

            hlaFilter().test(molecularCriterium) -> {
                extractHla(molecularCriterium)
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

    private fun extractHla(molecularCriterium: MolecularCriterium): ActionableHLA {
        return molecularCriterium.hla().iterator().next()
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

    private fun hlaFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.hla().isNotEmpty() }
    }
}