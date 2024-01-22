package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosis(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (HasMetastaticCancer(doidModel).evaluate(record).result()) {
             EvaluationResult.FAIL -> {
                EvaluationFactory.fail(
                    "Patient has no metastatic cancer (hence no extensive metastases) which could be the dominant factor determining prognosis in terms of life expectancy and performance status",
                    "No metastatic cancer (hence no extensive metastases) which could be the dominant factor determining prognosis"
                )
            }

            EvaluationResult.UNDETERMINED, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined(
                    "Undetermined metastatic cancer and therefore undetermined if metastases could be the dominant factor determining prognosis in terms of life expectancy and performance status",
                    "Undetermined metastatic cancer and therefore undetermined if metastases could be the dominant factor determining prognosis"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if the metastases are the dominant factor determining prognosis in terms of life expectancy and performance status",
                    "Undetermined if the metastases are the dominant factor determining prognosis"
                )
            }
        }
    }
}