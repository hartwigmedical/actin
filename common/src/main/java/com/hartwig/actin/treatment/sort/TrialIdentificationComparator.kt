package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.TrialIdentification
import java.lang.Boolean
import kotlin.Comparator
import kotlin.Int

class TrialIdentificationComparator() : Comparator<TrialIdentification> {
    public override fun compare(identification1: TrialIdentification, identification2: TrialIdentification): Int {
        val idCompare: Int = identification1.trialId().compareTo(identification2.trialId())
        if (idCompare != 0) {
            return idCompare
        }
        val acronymCompare: Int = identification1.acronym().compareTo(identification2.acronym())
        if (acronymCompare != 0) {
            return acronymCompare
        }
        val openCompare: Int = Boolean.compare(identification2.open(), identification1.open())
        if (openCompare != 0) {
            return openCompare
        }
        return identification1.title().compareTo(identification2.title())
    }
}
