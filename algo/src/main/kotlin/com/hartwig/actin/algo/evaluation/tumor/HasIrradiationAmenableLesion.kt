package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasIrradiationAmenableLesion(private val hasMetastaticCancer: HasMetastaticCancer) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail(
                    "Patient has no metastatic cancer and hence no irradiation amenable lesion",
                    "No metastatic cancer and hence no irradiation amenable lesion"
                )
            }

            EvaluationResult.UNDETERMINED, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has metastatic cancer and hence undetermined if patient has irradiation amenable lesion",
                    "Undetermined metastatic cancer and therefore undetermined if irradiation amenable lesion"
                )
            }

            else -> {
                EvaluationFactory.recoverableUndetermined(
                    "Undetermined if patient has irradiation amenable lesion",
                    "Undetermined if irradiation amenable lesion"
                )
            }
        }
    }
}