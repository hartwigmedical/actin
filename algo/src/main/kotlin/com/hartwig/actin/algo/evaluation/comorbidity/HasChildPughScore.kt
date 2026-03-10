package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasChildPughScore(private val icdModel: IcdModel, private val requestedScores: List<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverCirrhosis = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, setOf(IcdCode(IcdConstants.LIVER_CIRRHOSIS_CODE))
        ).fullMatches.isNotEmpty()

        val formattedRequestedScores = Format.concatWithCommaAndOr(requestedScores)

        return if (hasLiverCirrhosis) {
            EvaluationFactory.warn("Patient has liver cirrhosis - undetermined if Child-Pugh score $formattedRequestedScores")
        } else {
            EvaluationFactory.undetermined("Undetermined if Child-Pugh score $formattedRequestedScores")
        }
    }
}