package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class HasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosis(private val hasMetastaticCancer: HasMetastaticCancer) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail("No metastatic cancer (hence no extensive metastases) which could be the dominant factor determining prognosis")
            }

            EvaluationResult.UNDETERMINED, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined("Metastatic cancer undetermined and therefore undetermined if metastases could be the dominant factor determining prognosis")
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if the metastases are the dominant factor determining prognosis")
            }
        }
    }
}