package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.containsUnexpectedHaplotypeFunction
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isProficient
import com.hartwig.actin.molecular.datamodel.MolecularRecord

class HasHeterozygousDPYDDeficiency internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.filter { it.gene == DPYDDeficiencyEvaluationFunctions.DPYD_GENE }

        if (pharmaco.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")
        }

        return when {
            containsUnexpectedHaplotypeFunction(pharmaco) -> {
                EvaluationFactory.recoverableUndetermined(
                    "DPYD haplotype function cannot be determined due to unexpected haplotype function",
                    "DPYD haplotype function undetermined"
                )
            }

            !isHomozygousDeficient(pharmaco) && !isProficient(pharmaco) -> {
                EvaluationFactory.pass("Patient is heterozygous DPYD deficient", inclusionEvents = setOf("DPYD deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not heterozygous DPYD deficient")
            }
        }
    }
}