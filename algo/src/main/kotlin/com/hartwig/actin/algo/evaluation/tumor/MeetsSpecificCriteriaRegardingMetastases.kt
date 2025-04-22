package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class MeetsSpecificCriteriaRegardingMetastases(private val hasMetastaticCancer: HasMetastaticCancer) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (hasMetastaticCancer.evaluate(record).result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail("No metastatic cancer present hence won't meet study specific criteria regarding metastases")
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.undetermined("Undetermined if study specific criteria regarding metastases are met")
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined if metastatic cancer and therefore undetermined if study specific criteria regarding metastases are met")
            }
        }
    }
}