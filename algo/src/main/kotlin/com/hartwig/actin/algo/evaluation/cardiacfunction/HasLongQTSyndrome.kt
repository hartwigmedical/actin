package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.icd.IcdModel

class HasLongQTSyndrome(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLongQTSyndrome = OtherConditionSelector.selectClinicallyRelevant(record.priorOtherConditions).flatMap {
            OtherConditionSelector.selectConditionsMatchingIcdCode(
                record.priorOtherConditions,
                listOf(IcdConstants.LONG_QT_SYNDROME_CODE),
                icdModel
            )
        }

        return when {
            hasLongQTSyndrome.isNotEmpty() -> EvaluationFactory.pass("Patient has long QT syndrome", "Presence of long QT syndrome")
            else -> EvaluationFactory.fail("Patient does not have long QT syndrome", "No presence of long QT syndrome")
        }
    }
}