package com.hartwig.actin.algo.sort

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.trial.sort.EligibilityComparator

class EvaluationMapComparator : Comparator<Map<Eligibility, Evaluation>> {

    private val eligibilityComparator: Comparator<Eligibility> = EligibilityComparator()

    override fun compare(map1: Map<Eligibility, Evaluation>, map2: Map<Eligibility, Evaluation>): Int {
        val keys1 = map1.keys.toList()
        val keys2 = map2.keys.toList()

        val sizeCompare = keys1.size - keys2.size
        if (sizeCompare != 0) {
            return sizeCompare
        }

        keys1.indices.forEach { index ->
            val key1 = keys1[index]
            val key2 = keys2[index]
            val eligibilityCompare = eligibilityComparator.compare(key1, key2)
            if (eligibilityCompare != 0) {
                return eligibilityCompare
            }
            val evaluationCompare = map1[key1]!!.result.compareTo(map2[key2]!!.result)
            if (evaluationCompare != 0) {
                return evaluationCompare
            }
        }
        return 0
    }
}