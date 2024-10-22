package com.hartwig.actin.report.interpretation

class CohortComparator : Comparator<Cohort> {
    override fun compare(evaluatedCohort1: Cohort, evaluatedCohort2: Cohort): Int {
        return compareByDescending(Cohort::hasSlotsAvailable)
            .thenBy { it.molecularEvents.isEmpty() }
            .thenBy(nullsLast(), Cohort::phase)
            .thenByDescending { it.warnings.isEmpty() }
            .thenBy(Cohort::trialId)
            .thenComparing(::compareCohortNames)
            .thenByDescending { it.molecularEvents.size }
            .thenComparing(::compareMolecularEvents)
            .compare(evaluatedCohort1, evaluatedCohort2)
    }

    companion object {
        private const val COMBINATION_COHORT_IDENTIFIER = "+"

        private fun compareCohortNames(evaluatedCohort1: Cohort, evaluatedCohort2: Cohort): Int {
            val cohort1 = evaluatedCohort1.cohort
            val cohort2 = evaluatedCohort2.cohort
            if (cohort1 == null) {
                return if (cohort2 != null) -1 else 0
            } else if (cohort2 == null) {
                return 1
            }

            val hasMolecular = evaluatedCohort1.molecularEvents.isNotEmpty()
            return compareBy<String> { hasMolecular && it.contains(COMBINATION_COHORT_IDENTIFIER) }
                .thenByDescending { it }
                .compare(cohort2, cohort1)
        }

        private fun compareMolecularEvents(evaluatedCohort1: Cohort, evaluatedCohort2: Cohort): Int {
            return evaluatedCohort1.molecularEvents.sorted().zip(evaluatedCohort2.molecularEvents.sorted()).asSequence()
                .map { compareValues(it.second, it.first) }
                .find { it != 0 } ?: 0
        }
    }
}