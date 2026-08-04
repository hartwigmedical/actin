package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene

class HasHomozygousDPYDDeficiency(labels: EvaluationLabels.Molecular) :
    MolecularEvaluationFunction(useInsufficientQualityRecords = true, labels = labels) {

    override fun noMolecularTestEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            labels.hasHomozygousDpydDeficiencyUndeterminedNoData(),
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val pharmaco = test.pharmaco.firstOrNull { it.gene == PharmacoGene.DPYD }
            ?: return EvaluationFactory.undetermined(labels.hasHomozygousDpydDeficiencyUndetermined(), isMissingMolecularResultForEvaluation = true)

        return when {
            isHomozygousDeficient(pharmaco) -> {
                EvaluationFactory.pass(
                    labels.hasHomozygousDpydDeficiencyPass(),
                    inclusionEvents = setOf("DPYD homozygous deficient")
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasHomozygousDpydDeficiencyFail())
            }
        }
    }
}