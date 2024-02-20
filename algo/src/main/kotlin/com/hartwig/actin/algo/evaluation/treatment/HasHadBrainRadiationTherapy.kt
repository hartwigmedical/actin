package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadBrainRadiationTherapy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBrainMetastases = record.clinical.tumor.hasBrainLesions == true || record.clinical.tumor.hasActiveBrainLesions == true
        val priorRadiotherapy = record.clinical.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
        val brainRadiotherapy =
            priorRadiotherapy.any { it.treatmentHistoryDetails?.bodyLocationCategories?.contains(BodyLocationCategory.BRAIN) ?: false }

        return when {
            brainRadiotherapy -> {
                EvaluationFactory.pass(
                    "Patient has had prior brain radiation therapy",
                    "Has had brain radiation therapy"
                )
            }

            hasBrainMetastases && priorRadiotherapy.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Patient has brain metastases and has received radiotherapy but undetermined if brain radiation therapy",
                    "Undetermined prior brain radiation therapy"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received prior brain radiation therapy",
                    "Has not received prior brain radiation therapy"
                )
            }
        }
    }
}