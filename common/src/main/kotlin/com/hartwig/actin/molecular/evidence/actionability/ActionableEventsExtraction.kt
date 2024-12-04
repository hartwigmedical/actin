package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

object ActionableEventsExtraction {

    fun filterEfficacyEvidence(
        evidences: List<EfficacyEvidence>,
        molecularCriteriumPredicate: Predicate<MolecularCriterium>
    ): List<EfficacyEvidence> {
        return evidences.filter { evidence -> molecularCriteriumPredicate.test(evidence.molecularCriterium()) }
    }

    fun filterTrials(
        trials: List<ActionableTrial>,
        molecularCriteriumPredicate: Predicate<MolecularCriterium>
    ): List<ActionableTrial> {
        return trials.filter { trial ->
            trial.anyMolecularCriteria().any { criterium -> molecularCriteriumPredicate.test(criterium) }
        }
    }

    fun extractHotspot(efficacyEvidence: EfficacyEvidence): ActionableHotspot {
        return efficacyEvidence.molecularCriterium().hotspots().iterator().next()
    }

    fun extractHotspot(actionableTrial: ActionableTrial): ActionableHotspot {
        return actionableTrial.anyMolecularCriteria().iterator().next().hotspots().iterator().next()
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

    fun extractRange(actionableTrial: ActionableTrial): ActionableRange {
        return if (actionableTrial.anyMolecularCriteria().iterator().next().codons().isNotEmpty()) {
            actionableTrial.anyMolecularCriteria().iterator().next().codons().iterator().next()
        } else if (actionableTrial.anyMolecularCriteria().iterator().next().exons().isNotEmpty()) {
            actionableTrial.anyMolecularCriteria().iterator().next().exons().iterator().next()
        } else {
            throw IllegalStateException(
                "Neither codon nor range present in trial on actionable range: ${
                    actionableTrial.anyMolecularCriteria().iterator().next()
                }"
            )
        }
    }

    fun extractGene(efficacyEvidence: EfficacyEvidence): ActionableGene {
        return efficacyEvidence.molecularCriterium().genes().iterator().next()
    }

    fun extractGenes(actionableTrial: ActionableTrial): Set<ActionableGene> {
        return actionableTrial.anyMolecularCriteria().flatMap { it.genes() }.toSet()
    }

    fun extractFusion(efficacyEvidence: EfficacyEvidence): ActionableFusion {
        return efficacyEvidence.molecularCriterium().fusions().iterator().next()
    }

    fun extractFusions(actionableTrial: ActionableTrial): Set<ActionableFusion> {
        return actionableTrial.anyMolecularCriteria().flatMap { it.fusions() }.toSet()
    }

    fun extractCharacteristic(efficacyEvidence: EfficacyEvidence): ActionableCharacteristic {
        return efficacyEvidence.molecularCriterium().characteristics().iterator().next()
    }

    fun extractCharacteristic(actionableTrial: ActionableTrial): ActionableCharacteristic {
        return actionableTrial.anyMolecularCriteria().iterator().next().characteristics().iterator().next()
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

    fun characteristicsFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium -> molecularCriterium.characteristics().isNotEmpty() }
    }
}