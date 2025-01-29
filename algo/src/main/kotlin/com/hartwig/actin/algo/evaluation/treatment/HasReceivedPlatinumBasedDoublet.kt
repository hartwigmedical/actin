package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.createFullExpandedDoidTree
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.doid.DoidModel

class HasReceivedPlatinumBasedDoublet(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val message = "received platinum based doublet chemotherapy"
        val isNsclc = LUNG_NON_SMALL_CELL_CARCINOMA_DOID in createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val hasReceivedUndefinedChemoradiation = record.oncologicalHistory.any {
            it.treatments.map(Treatment::name).containsAll(listOf("CHEMOTHERAPY", "RADIOTHERAPY"))
        }

        return when {
            TreatmentFunctions.receivedPlatinumDoublet(record) || (isNsclc && hasReceivedUndefinedChemoradiation) -> {
                EvaluationFactory.pass("Has $message ")
            }

            TreatmentFunctions.receivedPlatinumTripletOrAbove(record) -> {
                EvaluationFactory.warn("Has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)")
            }

            else -> {
                EvaluationFactory.fail("Has not $message")
            }
        }
    }
}
