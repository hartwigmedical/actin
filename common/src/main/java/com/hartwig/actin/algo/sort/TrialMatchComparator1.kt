package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.sort.TrialIdentificationComparator
import java.lang.Boolean
import kotlin.Comparator
import kotlin.Int

class TrialMatchComparator : Comparator<TrialMatch> {
    override fun compare(eligibility1: TrialMatch, eligibility2: TrialMatch): Int {
        val identificationCompare = IDENTIFICATION_COMPARATOR.compare(eligibility1.identification(), eligibility2.identification())
        if (identificationCompare != 0) {
            return identificationCompare
        }
        val isPotentiallyEligibleCompare = Boolean.compare(eligibility1.isPotentiallyEligible(), eligibility2.isPotentiallyEligible())
        if (isPotentiallyEligibleCompare != 0) {
            return isPotentiallyEligibleCompare
        }
        val sizeCompare = eligibility1.cohorts().size - eligibility2.cohorts().size
        if (sizeCompare != 0) {
            return if (sizeCompare > 0) 1 else -1
        }
        var index = 0
        while (index < eligibility1.cohorts().size) {
            val cohortCompare = COHORT_ELIGIBILITY_COMPARATOR.compare(eligibility1.cohorts()[index], eligibility2.cohorts()[index])
            if (cohortCompare != 0) {
                return cohortCompare
            }
            index++
        }
        return EvaluationMapCompare.compare(eligibility1.evaluations(), eligibility2.evaluations())
    }

    companion object {
        private val IDENTIFICATION_COMPARATOR: Comparator<TrialIdentification> = TrialIdentificationComparator()
        private val COHORT_ELIGIBILITY_COMPARATOR: Comparator<CohortMatch> = CohortMatchComparator()
    }
}
