package com.hartwig.actin.algo.sort

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.sort.EligibilityComparator

internal object EvaluationMapCompare {
    private val ELIGIBILITY_COMPARATOR: Comparator<Eligibility> = EligibilityComparator()
    fun compare(map1: Map<Eligibility, Evaluation>, map2: Map<Eligibility, Evaluation>): Int {
        val keys1: List<Eligibility> = Lists.newArrayList(map1.keys)
        val keys2: List<Eligibility> = Lists.newArrayList(map2.keys)
        val sizeCompare = keys1.size - keys2.size
        if (sizeCompare != 0) {
            return if (sizeCompare > 0) 1 else -1
        }
        var index = 0
        while (index < keys1.size) {
            val key1 = keys1[index]
            val key2 = keys2[index]
            val eligibilityCompare = ELIGIBILITY_COMPARATOR.compare(key1, key2)
            if (eligibilityCompare != 0) {
                return eligibilityCompare
            }
            val evaluationCompare = map1[key1]!!.result().compareTo(map2[key2]!!.result())
            if (evaluationCompare != 0) {
                return evaluationCompare
            }
            index++
        }
        return 0
    }
}
