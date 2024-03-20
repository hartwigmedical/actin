package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason

class HasExperiencedImmuneRelatedAdverseEvents internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val immunotherapyTreatmentList = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.IMMUNOTHERAPY) }
        val hasHadImmuneTherapy = immunotherapyTreatmentList.isNotEmpty()
        val stopReasonUnknown = immunotherapyTreatmentList.all { it.treatmentHistoryDetails?.stopReason == null }
        val hasHadImmuneTherapyWithStopReasonToxicity = immunotherapyTreatmentList.any {
            it.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
        }
        val hasImmunotherapyAllergies = record.intolerances.any { it.drugAllergyType == "Immunotherapy drug allergy" }

        return when {
            (hasHadImmuneTherapy && (hasHadImmuneTherapyWithStopReasonToxicity || hasImmunotherapyAllergies)) -> {
                EvaluationFactory.warn(
                    "Patient may have experienced immunotherapy related adverse events",
                    "Probable prior immunotherapy related adverse events"
                )
            }

            (hasHadImmuneTherapy && stopReasonUnknown) -> {
                EvaluationFactory.recoverableUndetermined(
                    "Undetermined prior immunotherapy related adverse events",
                    "Undetermined prior immunotherapy related adverse events"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not experienced immunotherapy related adverse events",
                    "No experience of immunotherapy related adverse events"
                )
            }
        }
    }
}