package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
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

    fun extractHotspotTrials(trials: List<ActionableTrial>): List<ActionableTrial> {
        return extractTrials(trials, hotspotFilter())
    }

    fun extractCodonEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, codonFilter())
    }

    fun extractCodonTrials(trials: List<ActionableTrial>): List<ActionableTrial> {
        return extractTrials(trials, codonFilter())
    }

    fun extractExonEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, exonFilter())
    }

    fun extractExonTrials(trials: List<ActionableTrial>): List<ActionableTrial> {
        return extractTrials(trials, exonFilter())
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
    ): List<ActionableTrial> {
        return extractTrials(trials, geneFilter(validGeneEvents))
    }

    fun extractFusionEvidence(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return extractEfficacyEvidence(evidences, fusionFilter())
    }

    fun extractFusionTrials(trials: List<ActionableTrial>): List<ActionableTrial> {
        return extractTrials(trials, fusionFilter())
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
    ): List<ActionableTrial> {
        return extractTrials(trials, characteristicsFilter(validTypes))
    }

    fun extractHotspot(efficacyEvidence: EfficacyEvidence): ActionableHotspot {
        return efficacyEvidence.molecularCriterium().hotspots().iterator().next()
    }

    fun extractHotspots(actionableTrial: ActionableTrial): Set<ActionableHotspot> {
        return extractFromTrial(actionableTrial, MolecularCriterium::hotspots)
    }

    fun extractRange(efficacyEvidence: EfficacyEvidence): ActionableRange {
        return if (efficacyEvidence.molecularCriterium().codons().isNotEmpty()) {
            efficacyEvidence.molecularCriterium().codons().iterator().next()
        } else if (efficacyEvidence.molecularCriterium().exons().isNotEmpty()) {
            efficacyEvidence.molecularCriterium().exons().iterator().next()
        } else {
            throw IllegalStateException(
                "Neither codon nor range present in trial on actionable range: ${efficacyEvidence.molecularCriterium()}"
            )
        }
    }

    fun extractRanges(actionableTrial: ActionableTrial): Set<ActionableRange> {
        val codons = extractFromTrial(actionableTrial, MolecularCriterium::codons)
        val exons = extractFromTrial(actionableTrial, MolecularCriterium::exons)
        return codons + exons
    }

    fun extractGene(efficacyEvidence: EfficacyEvidence): ActionableGene {
        return efficacyEvidence.molecularCriterium().genes().iterator().next()
    }

    fun extractGenes(actionableTrial: ActionableTrial): Set<ActionableGene> {
        return extractFromTrial(actionableTrial, MolecularCriterium::genes)
    }

    fun extractFusion(efficacyEvidence: EfficacyEvidence): ActionableFusion {
        return efficacyEvidence.molecularCriterium().fusions().iterator().next()
    }

    fun extractFusions(actionableTrial: ActionableTrial): Set<ActionableFusion> {
        return extractFromTrial(actionableTrial, MolecularCriterium::fusions)
    }

    fun extractCharacteristic(efficacyEvidence: EfficacyEvidence): ActionableCharacteristic {
        return efficacyEvidence.molecularCriterium().characteristics().iterator().next()
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