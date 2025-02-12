package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadCategoryXTreatmentWithAnyIntentYWithinZMonths {
    fun evaluate(record: PatientRecord, treatmentCategory: TreatmentCategory, intents: Set<Intent>, months: Int): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        if (treatmentHistory.isEmpty()) {
            return EvaluationFactory.fail("Patient has no treatment history")
        }

        val matchingTreatments = treatmentHistory.filter { entry ->
            entry.categories().any { it == treatmentCategory }
        }
        if (matchingTreatments.isEmpty()) {
            return EvaluationFactory.fail("Patient has not had ${treatmentCategory.display()} treatment")
        }

        val locoregionalTherapyIntents = matchingTreatments.flatMap { it.intents.orEmpty() }
        if (locoregionalTherapyIntents.intersect(intents).isEmpty()) {
            return EvaluationFactory.pass("Patient has had ${treatmentCategory.display()} treatment without the listed intents")
        }


        return EvaluationFactory.pass("Patient has received ${treatmentCategory.display()} therapy for the listed intents in the last $months months")
    }
}