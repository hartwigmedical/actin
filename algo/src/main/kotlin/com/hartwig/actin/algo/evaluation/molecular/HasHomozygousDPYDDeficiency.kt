package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.DPYD_GENE
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.molecular.datamodel.MolecularRecord

class HasHomozygousDPYDDeficiency internal constructor() : MolecularEvaluationFunction {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        val pharmaco = molecular.pharmaco.firstOrNull { it.gene == DPYD_GENE }
            ?: return EvaluationFactory.undetermined("DPYD haplotype is undetermined", "DPYD haplotype undetermined")

        return when {
            isHomozygousDeficient(pharmaco) -> {
                EvaluationFactory.pass("Patient is homozygous DPYD deficient", inclusionEvents = setOf("DPYD homozygous deficient"))
            }

            else -> {
                EvaluationFactory.fail("Patient is not homozygous DPYD deficient")
            }
        }
    }
}