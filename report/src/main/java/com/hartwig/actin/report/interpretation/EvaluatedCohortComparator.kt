package com.hartwig.actin.report.interpretation

class EvaluatedCohortComparator : Comparator<EvaluatedCohort> {
    override fun compare(evaluatedCohort1: EvaluatedCohort, evaluatedCohort2: EvaluatedCohort): Int {
        val hasSlotsAvailableCompare =
            java.lang.Boolean.compare(!evaluatedCohort1.hasSlotsAvailable(), !evaluatedCohort2.hasSlotsAvailable())
        if (hasSlotsAvailableCompare != 0) {
            return hasSlotsAvailableCompare
        }
        val hasMolecularCompare =
            java.lang.Boolean.compare(evaluatedCohort1.molecularEvents().isEmpty(), evaluatedCohort2.molecularEvents().isEmpty())
        if (hasMolecularCompare != 0) {
            return hasMolecularCompare
        }
        val trialCompare = evaluatedCohort1.trialId().compareTo(evaluatedCohort2.trialId())
        if (trialCompare != 0) {
            return trialCompare
        }
        val cohortCompare =
            compareCohorts(!evaluatedCohort1.molecularEvents().isEmpty(), evaluatedCohort1.cohort(), evaluatedCohort2.cohort())
        return if (cohortCompare != 0) {
            cohortCompare
        } else compareSets(evaluatedCohort1.molecularEvents(), evaluatedCohort2.molecularEvents())
    }

    companion object {
        private const val COMBINATION_COHORT_IDENTIFIER = "+"
        private fun compareCohorts(hasMolecular: Boolean, cohort1: String?, cohort2: String?): Int {
            if (cohort1 == null) {
                return if (cohort2 != null) -1 else 0
            } else if (cohort2 == null) {
                return 1
            }
            if (hasMolecular) {
                val hasCombinationCohort = java.lang.Boolean.compare(
                    cohort2.contains(COMBINATION_COHORT_IDENTIFIER), cohort1.contains(
                        COMBINATION_COHORT_IDENTIFIER
                    )
                )
                if (hasCombinationCohort != 0) {
                    return hasCombinationCohort
                }
            }
            return cohort1.compareTo(cohort2)
        }

        private fun compareSets(set1: Set<String?>, set2: Set<String?>): Int {
            val countCompare = set2.size - set1.size
            if (countCompare != 0) {
                return if (countCompare > 0) 1 else -1
            }
            val iterator1 = set1.iterator()
            val iterator2 = set2.iterator()
            while (iterator1.hasNext()) {
                val valueCompare = iterator1.next()!!.compareTo(iterator2.next()!!)
                if (valueCompare != 0) {
                    return valueCompare
                }
            }
            return 0
        }
    }
}