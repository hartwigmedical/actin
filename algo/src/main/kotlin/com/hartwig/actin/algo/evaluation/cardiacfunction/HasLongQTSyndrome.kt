package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasLongQTSyndrome(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLongQTSyndrome = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, setOf(IcdCode(IcdConstants.LONG_QT_SYNDROME_CODE))
        ).fullMatches.isNotEmpty()

        return when {
            hasLongQTSyndrome -> EvaluationFactory.pass("Presence of long QT syndrome")
            else -> EvaluationFactory.fail("No presence of long QT syndrome")
        }
    }
}