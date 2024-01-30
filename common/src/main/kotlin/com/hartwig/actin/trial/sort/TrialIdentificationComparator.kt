package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.TrialIdentification

class TrialIdentificationComparator : Comparator<TrialIdentification> {
    private val comparator = Comparator.comparing(TrialIdentification::trialId)
        .thenComparing(TrialIdentification::acronym)
        .thenComparing(TrialIdentification::open)
        .thenComparing(TrialIdentification::title)

    override fun compare(identification1: TrialIdentification, identification2: TrialIdentification): Int {
        return comparator.compare(identification1, identification2)
    }
}
