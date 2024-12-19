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
        val (fullMatches, mainMatchesWithUnknownExtension) =
            icdModel.findInstancesMatchingAnyIcdCode(record.intolerances, setOf(targetCode))

        return when {
            fullMatches.isNotEmpty() -> {
                EvaluationFactory.pass("Has allergy ${Format.concatItemsWithAnd(fullMatches)} belonging to $targetIcdTitle")
            }

            mainMatchesWithUnknownExtension.isNotEmpty() -> {
                EvaluationFactory.undetermined("Allergy in history - but undetermined if $targetIcdTitle allergy (drug type unknown)")
            }

            else -> EvaluationFactory.fail("No known allergy to $targetIcdTitle")
        }
    }
}