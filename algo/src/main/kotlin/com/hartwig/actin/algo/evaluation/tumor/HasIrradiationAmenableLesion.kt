package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class HasIrradiationAmenableLesion(private val hasMetastaticCancer: HasMetastaticCancer) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail("No metastatic cancer and hence no irradiation amenable lesion")
            }

            EvaluationResult.UNDETERMINED, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined("Metastatic cancer undetermined and therefore undetermined if irradiation amenable lesion")
            }

            else -> {
                EvaluationFactory.recoverableUndetermined("Irradiation amenable lesion undetermined")
            }
        }
    }
}