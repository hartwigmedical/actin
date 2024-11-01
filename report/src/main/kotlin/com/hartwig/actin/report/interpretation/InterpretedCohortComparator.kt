package com.hartwig.actin.report.interpretation

class InterpretedCohortComparator : Comparator<InterpretedCohort> {
    override fun compare(cohort1: InterpretedCohort, cohort2: InterpretedCohort): Int {
        return compareByDescending(InterpretedCohort::hasSlotsAvailable)
            .thenBy { it.molecularEvents.isEmpty() }
            .thenBy(nullsLast(), InterpretedCohort::phase)
            .thenByDescending { it.warnings.isEmpty() }
            .thenBy(InterpretedCohort::trialId)
            .thenComparing(::compareCohortNames)
            .thenByDescending { it.molecularEvents.size }
            .thenComparing(::compareMolecularEvents)
            .compare(cohort1, cohort2)
    }

    companion object {
        private const val COMBINATION_COHORT_IDENTIFIER = "+"

        private fun compareCohortNames(cohort1: InterpretedCohort, cohort2: InterpretedCohort): Int {
            val cohort1Name = cohort1.name
            val cohort2Name = cohort2.name
            if (cohort1Name == null) {
                return if (cohort2Name != null) -1 else 0
            } else if (cohort2Name == null) {
                return 1
            }

            val hasMolecular = cohort1.molecularEvents.isNotEmpty()
            return compareBy<String> { hasMolecular && it.contains(COMBINATION_COHORT_IDENTIFIER) }
                .thenByDescending { it }
                .compare(cohort2Name, cohort1Name)
        }

        private fun compareMolecularEvents(cohort1: InterpretedCohort, cohort2: InterpretedCohort): Int {
            return cohort1.molecularEvents.sorted().zip(cohort2.molecularEvents.sorted()).asSequence()
                .map { compareValues(it.second, it.first) }
                .find { it != 0 } ?: 0
        }
    }
}