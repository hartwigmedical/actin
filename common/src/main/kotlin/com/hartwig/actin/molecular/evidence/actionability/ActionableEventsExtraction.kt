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

    fun extractGeneEvidence(
        evidences: List<EfficacyEvidence>,
        validGeneEvents: Set<GeneEvent> = GeneEvent.values().toSet()
    ): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, geneFilter(validGeneEvents))
    }

    fun extractGeneTrials(
        trials: List<ActionableTrial>,
        validGeneEvents: Set<GeneEvent> = GeneEvent.values().toSet()
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

    fun extractCharacteristicEvidence(
        evidences: List<EfficacyEvidence>,
        validTypes: Set<TumorCharacteristicType> = TumorCharacteristicType.values().toSet()
    ): List<EfficacyEvidence> {
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

            geneFilter().test(molecularCriterium) -> {
                molecularCriterium.genes().iterator().next()
            }

            fusionFilter().test(molecularCriterium) -> {
                molecularCriterium.fusions().iterator().next()
            }

            characteristicsFilter().test(molecularCriterium) -> {
                molecularCriterium.characteristics().iterator().next()
            }

            else -> throw IllegalStateException("Could not extract event for molecular criterium: $molecularCriterium")
        }
    }

    fun extractHotspot(molecularCriterium: MolecularCriterium): ActionableHotspot {
        return molecularCriterium.hotspots().iterator().next()
    }

    fun extractHotspots(actionableTrial: ActionableTrial): Set<ActionableHotspot> {
        return extractFromTrial(actionableTrial, MolecularCriterium::hotspots)
    }

    fun extractCodon(molecularCriterium: MolecularCriterium): ActionableRange {
        return molecularCriterium.codons().iterator().next()
    }

    fun extractExon(molecularCriterium: MolecularCriterium): ActionableRange {
        return molecularCriterium.exons().iterator().next()
    }

    fun extractRanges(actionableTrial: ActionableTrial): Set<ActionableRange> {
        val codons = extractFromTrial(actionableTrial, MolecularCriterium::codons)
        val exons = extractFromTrial(actionableTrial, MolecularCriterium::exons)
        return codons + exons
    }

    fun extractGene(molecularCriterium: MolecularCriterium): ActionableGene {
        return molecularCriterium.genes().iterator().next()
    }

    fun extractGene(efficacyEvidence: EfficacyEvidence): ActionableGene {
        return efficacyEvidence.molecularCriterium().genes().iterator().next()
    }

    fun extractGenes(actionableTrial: ActionableTrial): Set<ActionableGene> {
        return extractFromTrial(actionableTrial, MolecularCriterium::genes)
    }

    fun extractFusion(molecularCriterium: MolecularCriterium): ActionableFusion {
        return molecularCriterium.fusions().iterator().next()
    }

    fun extractFusions(actionableTrial: ActionableTrial): Set<ActionableFusion> {
        return extractFromTrial(actionableTrial, MolecularCriterium::fusions)
    }

    fun extractCharacteristic(molecularCriterium: MolecularCriterium): ActionableCharacteristic {
        return molecularCriterium.characteristics().iterator().next()
    }

    fun extractCharacteristics(actionableTrial: ActionableTrial): Set<ActionableCharacteristic> {
        return extractFromTrial(actionableTrial, MolecularCriterium::characteristics)
    }

    fun extractEfficacyEvidence(
        evidences: List<EfficacyEvidence>,
        molecularCriteriumPredicate: Predicate<MolecularCriterium>
    ): List<EfficacyEvidence> {
        return evidences.filter { evidence -> molecularCriteriumPredicate.test(evidence.molecularCriterium()) }
    }

    fun extractTrials(
        trials: List<ActionableTrial>,
        molecularCriteriumPredicate: Predicate<MolecularCriterium>
    ): List<ActionableTrial> {
        return trials.filter { trial ->
            trial.anyMolecularCriteria().any { criterium -> molecularCriteriumPredicate.test(criterium) }
        }
    }

    private fun <T> extractFromTrial(actionableTrial: ActionableTrial, extractActionableEvents: (MolecularCriterium) -> Set<T>): Set<T> {
        return actionableTrial.anyMolecularCriteria().flatMap { extractActionableEvents(it) }.toSet()
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

    fun geneFilter(validGeneEvents: Set<GeneEvent> = GeneEvent.values().toSet()): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.genes().any { validGeneEvents.contains(it.event()) } }
    }

    fun fusionFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.fusions().isNotEmpty() }
    }

    fun characteristicsFilter(
        validTypes: Set<TumorCharacteristicType> = TumorCharacteristicType.values().toSet()
    ): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.characteristics().any { validTypes.contains(it.type()) } }
    }
}