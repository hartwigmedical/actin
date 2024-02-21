package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

class HasAcquiredResistanceToSomeTreatment(private val treatment: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val targetDrug = record.clinical.oncologicalHistory.filter { it.treatments.contains(treatment) }
        val progressiveDiseaseUnderTargetDrug = targetDrug.any {
            it.treatmentHistoryDetails?.stopReason == StopReason.PROGRESSIVE_DISEASE
                    || it.treatmentHistoryDetails?.bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE
        }
        val toxicityMessage =
            if (targetDrug.all { it.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY }) "(stop reason toxicity) " else null

        return when {
            progressiveDiseaseUnderTargetDrug -> {
                EvaluationFactory.pass(
                    "Patient has a tumor with potential acquired resistance to ${treatment.name}",
                    "Has potential acquired resistance to ${treatment.name}"
                )
            }

            targetDrug.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Undetermined acquired resistance to ${treatment.name} $toxicityMessage- assuming none",
                    "Undetermined resistance to ${treatment.name} $toxicityMessage- assuming none"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient was not treated with ${treatment.name} hence does not have acquired resistance to this drug",
                    "No acquired resistance to ${treatment.name} since not in treatment history"
                )
            }
        }
    }

}