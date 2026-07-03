package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasPotentialDisruptionOfLymphaticDrainage(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = IcdConstants.LYMPHATIC_DRAINAGE_SET.map { IcdCode(it) }.toSet()
        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, targetIcdCodes).fullMatches

        return if (matchingComorbidities.isNotEmpty()) {
            EvaluationFactory.warn("Potential disruption of lymphatic drainage (${Format.concatItemsWithAnd(matchingComorbidities)})")
        } else {
            EvaluationFactory.fail("No potential disruption of lymphatic drainage")
        }
    }
}