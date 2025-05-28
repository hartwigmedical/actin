package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial

data class ActionabilityMatch(
    val evidenceMatches: List<EfficacyEvidence>,
    val matchingCriteriaPerTrialMatch: Map<ActionableTrial, Set<MolecularCriterium>>
)
