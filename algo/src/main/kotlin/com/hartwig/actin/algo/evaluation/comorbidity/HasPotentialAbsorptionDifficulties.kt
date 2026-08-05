package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasPotentialAbsorptionDifficulties(private val icdModel: IcdModel, private val labels: EvaluationLabels.Comorbidity) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetIcdCodes = IcdConstants.POSSIBLE_ABSORPTION_DIFFICULTIES_SET.map { IcdCode(it) }.toSet()
        val matchingComorbidities =
            icdModel.findInstancesMatchingAnyIcdCode(record.comorbidities, targetIcdCodes).fullMatches

        return if (matchingComorbidities.isNotEmpty()) {
            EvaluationFactory.pass(labels.hasPotentialAbsorptionDifficultiesPass(Format.concatItemsWithAnd(matchingComorbidities)))
        } else {
            EvaluationFactory.fail(labels.hasPotentialAbsorptionDifficultiesFail())
        }
    }
}