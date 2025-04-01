package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.TnmT

class HasTnmTScore(private val scores: Set<TnmT>) : EvaluationFunction {
    private val t1 = setOf(TnmT.T1, TnmT.T1A, TnmT.T1B, TnmT.T1C)
    private val t2 = setOf(TnmT.T2, TnmT.T2A, TnmT.T2B)
    private val t2A = setOf(TnmT.T2, TnmT.T2A)
    private val t2B = setOf(TnmT.T2, TnmT.T2B)
    private val t123 = t1 + t2 + TnmT.T3
    private val allT = t123 + TnmT.T4

    private val stageMap = mapOf(
        TumorStage.I to t1 + t2A,
        TumorStage.IA to t1,
        TumorStage.IB to t2A,
        TumorStage.II to t123,
        TumorStage.IIA to t1 + t2B,
        TumorStage.IIB to t123,
        TumorStage.III to allT,
        TumorStage.IIIA to allT,
        TumorStage.IIIB to allT,
        TumorStage.IIIC to setOf(TnmT.T3, TnmT.T4),
    )

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
        val possibleTnmTs = stageMap[stage] ?: emptySet()

        return when {
            stage == TumorStage.IV || stage == TumorStage.IVA || stage == TumorStage.IVB || stage == TumorStage.IVC ->
                EvaluationFactory.undetermined("Cancer is metastatic. Undetermined if tumor is TNM T-classification $scores")
            possibleTnmTs.containsAll(scores) -> EvaluationFactory.pass("Tumor could be of TNM T-classification $scores with potential T scores of $possibleTnmTs")
            possibleTnmTs.intersect(scores).isNotEmpty() ->
                EvaluationFactory.undetermined("Tumor could be of $possibleTnmTs but can't be of ${scores.subtract(possibleTnmTs)}")

            else -> EvaluationFactory.fail("Tumor is not of stage $scores")
        }
    }
}