package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.ActionableEvent
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

object ActionableEventsExtraction {

    fun extractHotspotEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, hotspotFilter())
    }

    fun extractHotspotTrials(trials: List<ActionableTrial>): Pair<List<ActionableTrial>, Predicate<MolecularCriterium>> {
        val predicate = hotspotFilter()
        return Pair(extractTrials(trials, predicate), predicate)
    }

    fun extractCodonEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, codonFilter())
    }

    fun extractCodonTrials(trials: List<ActionableTrial>): Pair<List<ActionableTrial>, Predicate<MolecularCriterium>> {
        val predicate = codonFilter()
        return Pair(extractTrials(trials, predicate), predicate)
    }

    fun extractExonEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, exonFilter())
    }

    fun extractExonTrials(trials: List<ActionableTrial>): Pair<List<ActionableTrial>, Predicate<MolecularCriterium>> {
        val predicate = exonFilter()
        return Pair(extractTrials(trials, predicate), predicate)
    }

    fun extractGeneEvidence(evidences: List<EfficacyEvidence>, validGeneEvents: Set<GeneEvent>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, geneFilter(validGeneEvents))
    }

    fun extractGeneTrials(
        trials: List<ActionableTrial>,
        validGeneEvents: Set<GeneEvent>
    ): Pair<List<ActionableTrial>, Predicate<MolecularCriterium>> {
        val predicate = geneFilter(validGeneEvents)
        return Pair(extractTrials(trials, predicate), predicate)
    }

    fun extractFusionEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, fusionFilter())
    }

    fun extractFusionTrials(trials: List<ActionableTrial>): Pair<List<ActionableTrial>, Predicate<MolecularCriterium>> {
        val predicate = fusionFilter()
        return Pair(extractTrials(trials, predicate), predicate)
    }

    fun extractCharacteristicEvidence(evidences: List<EfficacyEvidence>, validTypes: Set<TumorCharacteristicType>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, characteristicsFilter(validTypes))
    }

    fun extractCharacteristicsTrials(
        trials: List<ActionableTrial>,
        validTypes: Set<TumorCharacteristicType> = TumorCharacteristicType.values().toSet()
    ): Pair<List<ActionableTrial>, Predicate<MolecularCriterium>> {
        val predicate = characteristicsFilter(validTypes)
        return Pair(extractTrials(trials, predicate), predicate)
    }

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

    private fun extractEfficacyEvidence(
        evidences: List<EfficacyEvidence>,
        predicate: Predicate<MolecularCriterium>
    ): List<EfficacyEvidence> {
        return evidences.filter { evidence -> predicate.test(evidence.molecularCriterium()) }
    }

    private fun extractTrials(trials: List<ActionableTrial>, predicate: Predicate<MolecularCriterium>): List<ActionableTrial> {
        return trials.filter { trial ->
            trial.anyMolecularCriteria().any { criterium -> predicate.test(criterium) }
        }
    }

    private fun hotspotFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.hotspots().isNotEmpty() }
    }

    private fun codonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.codons().isNotEmpty() }
    }

    private fun exonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.exons().isNotEmpty() }
    }

    private fun geneFilter(validGeneEvents: Set<GeneEvent>): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.genes().any { validGeneEvents.contains(it.event()) } }
    }

    private fun fusionFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.fusions().isNotEmpty() }
    }

    private fun characteristicsFilter(validTypes: Set<TumorCharacteristicType>): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.characteristics().any { validTypes.contains(it.type()) } }
    }
}