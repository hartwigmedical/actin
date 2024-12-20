package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasIntoleranceWithSpecificIcdTitle(private val icdModel: IcdModel, private val targetIcdTitle: String) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val targetCode = icdModel.resolveCodeForTitle(targetIcdTitle)!!
        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(record.intolerances, setOf(targetCode))

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                EvaluationFactory.pass("Has intolerance ${Format.concatItemsWithAnd(icdMatches.fullMatches)} belonging to $targetIcdTitle")
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined("Intolerance in history - but undetermined if $targetIcdTitle intolerance (drug type unknown)")
            }

            else -> EvaluationFactory.fail("No known intolerance to $targetIcdTitle")
        }
    }
}