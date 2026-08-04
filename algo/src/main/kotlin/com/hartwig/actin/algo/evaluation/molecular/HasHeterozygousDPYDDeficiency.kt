package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient
import com.hartwig.actin.algo.evaluation.molecular.DPYDDeficiencyEvaluationFunctions.isProficient
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene

class HasHeterozygousDPYDDeficiency(labels: EvaluationLabels.Molecular) :
    MolecularEvaluationFunction(useInsufficientQualityRecords = true, labels = labels) {

    override fun noMolecularTestEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            labels.hasHeterozygousDpydDeficiencyUndeterminedNoData(),
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun evaluate(test: MolecularTest): Evaluation {
        val pharmaco = test.pharmaco.firstOrNull { it.gene == PharmacoGene.DPYD }
            ?: return EvaluationFactory.undetermined(
                labels.hasHeterozygousDpydDeficiencyUndetermined(),
                isMissingMolecularResultForEvaluation = true
            )

        return when {
            !isHomozygousDeficient(pharmaco) && !isProficient(pharmaco) -> {
                EvaluationFactory.pass(
                    labels.hasHeterozygousDpydDeficiencyPass(),
                    inclusionEvents = setOf("DPYD heterozygous deficient")
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasHeterozygousDpydDeficiencyFail())
            }
        }
    }
}