package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasReceivedPlatinumBasedDoublet(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val message = "received platinum based doublet chemotherapy"
        val treatmentHistoryAnalysis = TreatmentHistoryAnalysis.create(record)

        return when {
            treatmentHistoryAnalysis.receivedPlatinumDoublet() -> {
                EvaluationFactory.pass("Has $message ")
            }

            DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID) &&
                    treatmentHistoryAnalysis.receivedUndefinedChemoradiation() -> {
                EvaluationFactory.pass("Has received chemoradiation for NSCLC - assumed platinum-based")
            }

            treatmentHistoryAnalysis.receivedPlatinumTripletOrAbove() -> {
                EvaluationFactory.warn("Has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)")
            }

            else -> {
                EvaluationFactory.fail("Has not $message")
            }
        }
    }
}
