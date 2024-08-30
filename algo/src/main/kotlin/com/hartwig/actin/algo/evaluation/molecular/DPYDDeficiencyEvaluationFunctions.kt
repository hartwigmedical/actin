package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.orange.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry

object DPYDDeficiencyEvaluationFunctions {

    fun isHomozygousDeficient(pharmaco: PharmacoEntry): Boolean {
        return pharmaco.haplotypes.none { it.function == HaplotypeFunction.NORMAL_FUNCTION }
    }

    fun isProficient(pharmaco: PharmacoEntry): Boolean {
        return pharmaco.haplotypes.all { it.function == HaplotypeFunction.NORMAL_FUNCTION }
    }
}