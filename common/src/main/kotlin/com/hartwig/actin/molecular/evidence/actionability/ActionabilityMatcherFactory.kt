package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial

object ActionabilityMatcherFactory {

    fun create(serveRecord: ServeRecord): ActionabilityMatcher {
        return ActionabilityMatcher(filterEvidences(serveRecord.evidences()), filterTrials(serveRecord.trials()))
    }

    private fun filterEvidences(evidences: List<EfficacyEvidence>): List<EfficacyEvidence> {
        return evidences
            .filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }
            .filter { isMolecularCriteriumApplicable(it.molecularCriterium()) }
    }
    
    private fun filterTrials(trials: List<ActionableTrial>): List<ActionableTrial> {
        return trials
            .filter { it.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE }
            .mapNotNull { trial ->
                val applicableCriteria = trial.anyMolecularCriteria().filter { isMolecularCriteriumApplicable(it) }
                if (applicableCriteria.isNotEmpty()) {
                    ImmutableActionableTrial.builder().from(trial).anyMolecularCriteria(applicableCriteria).build()
                } else {
                    null
                }
            }
    }

    private fun isMolecularCriteriumApplicable(molecularCriterium: MolecularCriterium): Boolean {
        with(molecularCriterium) {
            return hotspots().all { ApplicabilityFiltering.isApplicable(it) } &&
                    codons().all { ApplicabilityFiltering.isApplicable(it) } &&
                    exons().all { ApplicabilityFiltering.isApplicable(it) } &&
                    genes().all { ApplicabilityFiltering.isApplicable(it) }
        }
    }
}