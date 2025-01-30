package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasReceivedPlatinumBasedDoublet(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val message = "received platinum based doublet chemotherapy"
        val treatmentHistoryAnalyzer = TreatmentHistoryAnalyzer(record, doidModel)

        return when {
            treatmentHistoryAnalyzer.receivedPlatinumDoublet() -> {
                EvaluationFactory.pass("Has $message ")
            }

            treatmentHistoryAnalyzer.isNsclc && treatmentHistoryAnalyzer.receivedUndefinedChemoradiation() -> {
                EvaluationFactory.pass("Has received chemoradiation - assumed platinum-based")
            }

            treatmentHistoryAnalyzer.receivedPlatinumTripletOrAbove() -> {
                EvaluationFactory.warn("Has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)")
            }

            else -> {
                EvaluationFactory.fail("Has not $message")
            }
        }
    }
}
