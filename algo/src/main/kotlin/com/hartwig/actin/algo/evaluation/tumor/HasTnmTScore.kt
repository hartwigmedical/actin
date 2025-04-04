package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.TnmT

class HasTnmTScore(private val targetTnmTs: Set<TnmT>) : EvaluationFunction {
    private val t1 = setOf(TnmT.T1, TnmT.T1A, TnmT.T1B, TnmT.T1C)
    private val t2A = setOf(TnmT.T2, TnmT.T2A)
    private val t2B = setOf(TnmT.T2, TnmT.T2B)
    private val t123 = t1 + t2A + t2B + TnmT.T3
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
        val stages = record.tumor.stage?.let { setOf(it) }?: record.tumor.derivedStages
        val possibleTnmTs = stages?.mapNotNull { stageMap[it] }?.flatten()?.toSet() ?: emptySet()

        return when {
            stages.isNullOrEmpty() -> EvaluationFactory.undetermined("No tumor stage  or derived tumor stage found. Tnm T scores not determined.")
            setOf(TumorStage.IV, TumorStage.IVA, TumorStage.IVB, TumorStage.IVC).containsAll(stages) ->
                EvaluationFactory.undetermined("Cancer is metastatic. Undetermined if tumor is TNM T-classification ${show(targetTnmTs)}")
            targetTnmTs.containsAll(possibleTnmTs) -> EvaluationFactory.pass("Tumor could be T scores of ${show(possibleTnmTs)}")
            targetTnmTs.intersect(possibleTnmTs).isNotEmpty() ->
                EvaluationFactory.undetermined("Undetermined if TNM T-classification is of ${show(targetTnmTs)}- derived T's based on tumor stage are ${show(possibleTnmTs)}")
            else -> EvaluationFactory.fail("Tumor is not of stage ${show(targetTnmTs)}")
        }
    }

    private fun show(tnmTs: Set<TnmT>): String{
        return Format.concatWithCommaAndOr(tnmTs.map { it.display() })
    }
}