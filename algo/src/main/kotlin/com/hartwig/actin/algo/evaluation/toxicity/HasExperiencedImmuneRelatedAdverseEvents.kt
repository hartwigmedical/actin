package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason

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
                EvaluationFactory.warn("Immunotherapy related adverse events in history$allergyString")
            }

            hasHadImmunotherapyWithStopReasonToxicity -> {
                EvaluationFactory.warn("Patient may have experienced immunotherapy related adverse events " +
                        "(prior immunotherapy with stop reason toxicity)")
            }

            (immunotherapyTreatmentList.isNotEmpty() && stopReasonUnknown) -> {
                EvaluationFactory.recoverableUndetermined("Prior immunotherapy related adverse events undetermined")
            }

            else -> {
                EvaluationFactory.fail("No experience of immunotherapy related adverse events")
            }
        }
    }
}