package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadBrainRadiationTherapy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val hasPotentialBrainMetastases = tumorDetails.hasBrainLesions == true || tumorDetails.hasActiveBrainLesions == true
                || (tumorDetails.hasCnsLesions == true && tumorDetails.hasBrainLesions == null)
        val priorRadiotherapies = record.oncologicalHistory
            .filter { it.categories().contains(TreatmentCategory.RADIOTHERAPY) }
        val hasHadBrainRadiotherapy =
            priorRadiotherapies.any { it.treatmentHistoryDetails?.bodyLocationCategories?.contains(BodyLocationCategory.BRAIN) ?: false }

        return when {
            hasHadBrainRadiotherapy -> {
                EvaluationFactory.pass(
                    "Patient has had prior brain radiation therapy",
                    "Has had brain radiation therapy"
                )
            }

            hasPotentialBrainMetastases && priorRadiotherapies.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "Patient has brain and/or CNS metastases and has received radiotherapy but undetermined if brain radiation therapy",
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