package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceWithSpecificIcdTitle(
    private val icdModel: IcdModel, private val targetIcdTitle: String, private val labels: EvaluationLabels.Toxicity
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCode = icdModel.resolveCodeForTitle(targetIcdTitle)!!
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, setOf(targetCode))

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                EvaluationFactory.pass(
                    labels.hasIntoleranceWithSpecificIcdTitlePass(
                        Format.concatItemsWithAnd(icdMatches.fullMatches, true), targetIcdTitle
                    )
                )
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined(labels.hasIntoleranceWithSpecificIcdTitleUndetermined(targetIcdTitle))
            }

            else -> EvaluationFactory.fail(labels.hasIntoleranceWithSpecificIcdTitleFail(targetIcdTitle))
        }
    }
}