package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasEcgAberration(private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cardiacArrhythmiaComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            listOf(IcdCode(IcdConstants.CARDIAC_ARRHYTHMIA_BLOCK))
        ).fullMatches

        return when {
            record.ecgs.isNotEmpty() -> {
                val aberrations = Format.concat(record.ecgs.map { it.name ?: "details unknown" })
                EvaluationFactory.recoverablePass("ECG abnormalities present ($aberrations)")
            }

            cardiacArrhythmiaComorbidities.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Cardiac arrhythmia in history (${Format.concatItemsWithAnd(cardiacArrhythmiaComorbidities)})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail("No known ECG abnormalities")
            }
        }
    }
}