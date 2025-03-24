package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasTnmTScore(private val score: String): EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tumor = record.tumor
        val tnm = if (tumor.hasConfirmedLesions()) "M" else ""
        return if (score.contains(tnm)){
            EvaluationFactory.pass("Tumor is TNM $score")
        }
        else {
            EvaluationFactory.fail("Undetermined if tumor is TNM $score")
        }
    }
}