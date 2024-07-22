package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason

class HasExperiencedImmuneRelatedAdverseEvents : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val immunotherapyTreatmentList = record.oncologicalHistory.filter { it.categories().contains(TreatmentCategory.IMMUNOTHERAPY) }
        val immunotherapyTreatmentsByStopReason = immunotherapyTreatmentList.groupBy { it.treatmentHistoryDetails?.stopReason }
        val stopReasonUnknown = immunotherapyTreatmentsByStopReason.keys == setOf(null)
        val hasHadImmunotherapyWithStopReasonToxicity = StopReason.TOXICITY in immunotherapyTreatmentsByStopReason

        val immunotherapyAllergies = record.intolerances.filter {
            it.treatmentCategories?.contains(TreatmentCategory.IMMUNOTHERAPY) ?: false
        }

        return when {
            immunotherapyAllergies.isNotEmpty() -> {
                val allergyString = immunotherapyAllergies.joinToString(", ", prefix = " (", postfix = ")") { it.name }
                EvaluationFactory.warn("Patient may have experienced immunotherapy related adverse events$allergyString")
            }

            hasHadImmunotherapyWithStopReasonToxicity -> {
                EvaluationFactory.warn("Patient may have experienced immunotherapy related adverse events")
            }

            (immunotherapyTreatmentList.isNotEmpty() && stopReasonUnknown) -> {
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