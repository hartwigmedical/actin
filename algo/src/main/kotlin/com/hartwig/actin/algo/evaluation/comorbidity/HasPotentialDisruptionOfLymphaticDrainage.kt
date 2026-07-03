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
        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, setOf(IcdCode(IcdConstants.DISORDERS_OF_LYMPHATIC_VESSELS_OR_LYMPH_NODES))
        ).fullMatches

        return if (matchingComorbidities.isNotEmpty()) {
            EvaluationFactory.warn("Potential disruption of lymphatic drainage (${Format.concatItemsWithAnd(matchingComorbidities)})")
        } else {
            EvaluationFactory.fail("No potential disruption of lymphatic drainage")
        }
    }
}