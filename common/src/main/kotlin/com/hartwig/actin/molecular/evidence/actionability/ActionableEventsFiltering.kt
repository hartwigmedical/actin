package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.ActionableCharacteristic
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.molecular.range.ActionableRange
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial
import java.util.function.Predicate
import java.util.stream.Collectors

object ActionableEventsFiltering {

    fun getHotspot(efficacyEvidence: EfficacyEvidence): ActionableHotspot {
        return efficacyEvidence.molecularCriterium().hotspots().iterator().next()
    }

    fun getHotspot(actionableTrial: ActionableTrial): ActionableHotspot {
        return actionableTrial.anyMolecularCriteria().iterator().next().hotspots().iterator().next()
    }

    fun getRange(efficacyEvidence: EfficacyEvidence): ActionableRange {
        return if (efficacyEvidence.molecularCriterium().codons().isNotEmpty()) {
            efficacyEvidence.molecularCriterium().codons().iterator().next()
        } else if (efficacyEvidence.molecularCriterium().exons().isNotEmpty()) {
            efficacyEvidence.molecularCriterium().exons().iterator().next()
        } else {
            throw IllegalStateException("Neither codon nor range present in trial on actionable range: ${efficacyEvidence.molecularCriterium()}")
        }
    }

    fun getRange(actionableTrial: ActionableTrial): ActionableRange {
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

    fun getGene(efficacyEvidence: EfficacyEvidence): ActionableGene {
        return efficacyEvidence.molecularCriterium().genes().iterator().next()
    }

    fun getGene(actionableTrial: ActionableTrial): ActionableGene {
        return actionableTrial.anyMolecularCriteria().iterator().next().genes().iterator().next()
    }

    fun getFusion(efficacyEvidence: EfficacyEvidence): ActionableFusion {
        return efficacyEvidence.molecularCriterium().fusions().iterator().next()
    }

    fun getFusion(actionableTrial: ActionableTrial): ActionableFusion {
        return actionableTrial.anyMolecularCriteria().iterator().next().fusions().iterator().next()
    }

    fun getCharacteristic(efficacyEvidence: EfficacyEvidence): ActionableCharacteristic {
        return efficacyEvidence.molecularCriterium().characteristics().iterator().next()
    }

    fun getCharacteristic(actionableTrial: ActionableTrial): ActionableCharacteristic {
        return actionableTrial.anyMolecularCriteria().iterator().next().characteristics().iterator().next()
    }

    fun hotspotFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium: MolecularCriterium ->
            molecularCriterium.hotspots().isNotEmpty()
        }
    }

    fun codonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium: MolecularCriterium ->
            molecularCriterium.codons().isNotEmpty()
        }
    }

    fun exonFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium: MolecularCriterium ->
            molecularCriterium.exons().isNotEmpty()
        }
    }

    fun fusionFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium: MolecularCriterium ->
            molecularCriterium.fusions().isNotEmpty()
        }
    }

    fun geneFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium: MolecularCriterium ->
            molecularCriterium.genes().isNotEmpty()
        }
    }

    fun characteristicsFilter(): Predicate<MolecularCriterium> {
        return Predicate { molecularCriterium: MolecularCriterium ->
            molecularCriterium.characteristics().isNotEmpty()
        }
    }

    fun filterEfficacyEvidence(
        evidences: List<EfficacyEvidence>,
        molecularCriteriumPredicate: Predicate<MolecularCriterium>
    ): List<EfficacyEvidence> {
        return evidences.stream()
            .filter { evidence: EfficacyEvidence -> molecularCriteriumPredicate.test(evidence.molecularCriterium()) }
            .collect(Collectors.toList())
    }

    fun filterAndExpandTrials(
        trials: List<ActionableTrial>,
        molecularCriteriumPredicate: Predicate<MolecularCriterium>
    ): List<ActionableTrial> {
        val expandedFilteredTrials: MutableList<ActionableTrial> = mutableListOf()
        for (trial in trials) {
            for (criterium in trial.anyMolecularCriteria()) {
                if (molecularCriteriumPredicate.test(criterium)) {
                    expandedFilteredTrials.addAll(expandWithIndicationAndCriterium(trial, criterium))
                }
            }
        }
        return expandedFilteredTrials
    }

    private fun expandWithIndicationAndCriterium(
        baseTrial: ActionableTrial,
        criterium: MolecularCriterium
    ): List<ActionableTrial> {
        val expandedTrials: MutableList<ActionableTrial> = mutableListOf()
        val trialBuilder = ImmutableActionableTrial.builder().from(baseTrial).anyMolecularCriteria(listOf(criterium))
        for (indication in baseTrial.indications()) {
            expandedTrials.add(trialBuilder.indications(listOf(indication)).build())
        }
        return expandedTrials
    }
}