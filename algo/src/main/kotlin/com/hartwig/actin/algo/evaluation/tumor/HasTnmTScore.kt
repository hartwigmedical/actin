package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasTnmTScore(private val score: String): EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tnm = if (record.tumor.hasConfirmedLesions()) "M" else ""
        return if (score.contains(tnm)){
            return EvaluationFactory.pass("Tumor is TNM $score")
        }
        else {
            return EvaluationFactory.undetermined("Undetermined if tumor is TNM $score")
        }
    }
}