package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTnmTScore(private val score: String): EvaluationFunction {
    private val stageMap = 1

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
        val tnmT = when (stage) {
            TumorStage.I -> '1'
            TumorStage.IA -> '1'
            TumorStage.IB -> '2'
            TumorStage.II -> '2'
            TumorStage.III -> '3'
            TumorStage.IV -> 'M'
            else -> 'X'
        }
        return when {
            tnmT == 'M' -> EvaluationFactory.undetermined("Cancer is metastatic. Primary tumor stage is unknown")
            score.contains(tnmT) -> EvaluationFactory.pass("Tumor is of stage $score")
            else -> EvaluationFactory.fail("Tumor is of stage $score")
        }
    }
}

enum class TnmT {
    T0,
    T1,
    T2,
    T3,
    T4,
    M1,
    M1C
}