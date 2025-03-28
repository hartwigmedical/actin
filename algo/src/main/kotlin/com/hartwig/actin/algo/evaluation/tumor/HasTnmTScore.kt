package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.Displayable
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

class HasTnmTScore(private val score: String): EvaluationFunction {




    override fun evaluate(record: PatientRecord): Evaluation {
        val T1 = setOf(TnmT.T1, TnmT.T1A, TnmT.T1B, TnmT.T1C)
        val T2 = setOf(TnmT.T2, TnmT.T2A, TnmT.T2B)
        val T2A = setOf(TnmT.T2, TnmT.T2A)
        val T2B = setOf(TnmT.T2, TnmT.T2B)
        val T123 = T1 + T2 + TnmT.T3
        val allT = T1 + T2 + TnmT.T3 + TnmT.T4
        val M1 = setOf(TnmT.M1, TnmT.M1A, TnmT.M1B, TnmT.M1C)
        val M1AB = setOf(TnmT.M1, TnmT.M1A, TnmT.M1B)
        val M1C = setOf(TnmT.M1, TnmT.M1C)

        val stageMap = mapOf(
            TumorStage.I to T1 + T2A,
            TumorStage.IA to T1,
            TumorStage.IB to T2A,
            TumorStage.II to T123,
            TumorStage.IIA to T1 + T2B,
            TumorStage.IIB to T123,
            //TumorStage.IIC to T123,
            TumorStage.III to allT,
            TumorStage.IIIA to allT,
            TumorStage.IIIB to allT,
            TumorStage.IIIC to setOf(TnmT.T3, TnmT.T4),
            //TumorStage.IIID to setOf(TnmT.T3, TnmT.T4),
            TumorStage.IV to M1,
            TumorStage.IVA to M1AB,
            TumorStage.IVB to M1C,
            //TumorStage.IVC to M1c
        )

        val scoreTnmT = TnmT.valueOf(score.uppercase())

        val stage = record.tumor.stage
        return when {
            stageMap[stage]?.contains(TnmT.M1) == true -> EvaluationFactory.undetermined("Cancer is metastatic. Primary tumor stage is unknown")
            stageMap[stage]?.contains(scoreTnmT) == true -> EvaluationFactory.pass("Tumor is of stage $score with potential T scores of ${stageMap[stage]}")
            else -> EvaluationFactory.fail("Tumor is of stage $score")
        }
    }
}

enum class TnmT(val category: TnmT?) : Displayable {
    T0(null),
    T1(null),
    T1A(T1),
    T1B(T1),
    T1C(T1),
    T2(null),
    T2A(T2),
    T2B(T2),
    T3(null),
    T4(null),
    M1(null),
    M1A(M1),
    M1B(M1),
    M1C(M1);

    override fun display(): String {
        return this.toString()
    }
}