package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.sort.PriorTumorTreatmentDescendingDateComparator

internal object SystemicTreatmentAnalyser {
    fun maxSystemicTreatments(treatments: List<PriorTumorTreatment>): Int {
        var systemicCount = 0
        for (treatment in treatments) {
            if (treatment.isSystemic) {
                systemicCount++
            }
        }
        return systemicCount
    }

    fun minSystemicTreatments(treatments: List<PriorTumorTreatment>): Int {
        val systemicByName = treatments.filter { it.isSystemic }.groupBy { it.name() }

        var systemicCount = 0
        for (systemicWithName in systemicByName.values) {
            systemicCount++
            if (systemicWithName.size > 1) {
                val sortedWithName = systemicWithName.sortedWith(PriorTumorTreatmentDescendingDateComparator())
                for (i in 1 until sortedWithName.size) {
                    if (isInterrupted(sortedWithName[i], sortedWithName[i - 1], treatments)) {
                        systemicCount++
                    }
                }
            }
        }
        return systemicCount
    }

    fun lastSystemicTreatment(priorTumorTreatments: List<PriorTumorTreatment>): PriorTumorTreatment? {
        return priorTumorTreatments.filter { it.isSystemic }
            .maxWithOrNull { treatment1, treatment2 -> compareTreatmentsByStartDate(treatment1, treatment2) }
    }

    private fun compareTreatmentsByStartDate(treatment1: PriorTumorTreatment, treatment2: PriorTumorTreatment): Int {
        val yearComparison = compareNullableIntegers(treatment1.startYear(), treatment2.startYear())
        return if (yearComparison != 0) yearComparison else compareNullableIntegers(treatment1.startMonth(), treatment2.startMonth())
    }

    private fun compareNullableIntegers(first: Int?, second: Int?): Int {
        // Nulls are considered less than non-nulls
        return if (first != null) {
            if (second != null) {
                Integer.compare(first, second)
            } else {
                1
            }
        } else if (second != null) {
            -1
        } else {
            0
        }
    }

    private fun isInterrupted(
        mostRecent: PriorTumorTreatment, leastRecent: PriorTumorTreatment,
        treatments: List<PriorTumorTreatment>
    ): Boolean {
        // Treatments with ambiguous timeline are never considered interrupted.
        if (!isAfter(mostRecent, leastRecent)) {
            return false
        }
        for (treatment in treatments) {
            if (treatment.name() != mostRecent.name() && isAfter(treatment, leastRecent) && isBefore(treatment, mostRecent)) {
                return true
            }
        }
        return false
    }

    private fun isBefore(first: PriorTumorTreatment, second: PriorTumorTreatment): Boolean {
        return if (isLower(first.startYear(), second.startYear())) {
            true
        } else {
            isEqual(first.startYear(), second.startYear()) && isLower(
                first.startMonth(),
                second.startMonth()
            )
        }
    }

    private fun isAfter(first: PriorTumorTreatment, second: PriorTumorTreatment): Boolean {
        return if (isHigher(first.startYear(), second.startYear())) {
            true
        } else {
            isEqual(first.startYear(), second.startYear()) && isHigher(
                first.startMonth(),
                second.startMonth()
            )
        }
    }

    private fun isHigher(int1: Int?, int2: Int?): Boolean {
        return if (int1 == null || int2 == null) {
            false
        } else int1 > int2
    }

    private fun isLower(int1: Int?, int2: Int?): Boolean {
        return if (int1 == null || int2 == null) {
            false
        } else int1 < int2
    }

    private fun isEqual(int1: Int?, int2: Int?): Boolean {
        return if (int1 == null || int2 == null) {
            false
        } else int1 == int2
    }
}