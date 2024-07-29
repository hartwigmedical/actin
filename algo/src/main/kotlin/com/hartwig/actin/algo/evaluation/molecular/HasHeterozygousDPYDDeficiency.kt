package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isProficient
import com.hartwig.actin.molecular.datamodel.MolecularRecord

class HasHeterozygousDPYDDeficiency internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.firstOrNull { it.gene == DPYDDeficiencyEvaluationFunctions.DPYD_GENE }
            ?: return EvaluationFactory.undetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")

        return when {
            !isHomozygousDeficient(pharmaco) && !isProficient(pharmaco) -> {
                EvaluationFactory.pass("Patient is heterozygous DPYD deficient", inclusionEvents = setOf("DPYD deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not heterozygous DPYD deficient")
            }
        }
    }
}