package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class IsInDialysis(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            setOf(IcdConstants.DIALYSIS_CARE_CODE, IcdConstants.DEPENDANCE_ON_RENAL_DIALYSIS_CODE).map { IcdCode(it) }.toSet()
        ).fullMatches

        return if (matchingComorbidities.isNotEmpty()) {
            EvaluationFactory.pass("Patient receives renal dialysis")
        } else EvaluationFactory.fail("No renal dialysis")
    }
}