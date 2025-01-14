package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasChildPughScore(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverCirrhosis = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, setOf(IcdCode(IcdConstants.LIVER_CIRRHOSIS_CODE))
        ).fullMatches.isNotEmpty()

        return if (hasLiverCirrhosis) {
            EvaluationFactory.undetermined("Child-Pugh score undetermined")
        } else {
            EvaluationFactory.notEvaluated("Assumed that Child-Pugh score is not relevant since no history of liver cirrhosis")
        }
    }
}