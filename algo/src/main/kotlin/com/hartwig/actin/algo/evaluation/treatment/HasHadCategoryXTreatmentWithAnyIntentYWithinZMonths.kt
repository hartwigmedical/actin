package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadCategoryXTreatmentWithAnyIntentYWithinZMonths (private val category: TreatmentCategory, private val intentsToFind: Set<Intent>, private val monthsCutoff: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory = record.oncologicalHistory
        if (treatmentHistory.isEmpty()) {
            return EvaluationFactory.fail("Patient has no treatment history")
        }

        val matchingTreatments = treatmentHistory.filter { entry ->
            entry.categories().any { it == category }
        }
        if (matchingTreatments.isEmpty()) {
            return EvaluationFactory.fail("Patient has not had ${category.display()} treatment")
        }

        val locoregionalTherapyIntents = matchingTreatments.flatMap { it.intents.orEmpty() }
        if (locoregionalTherapyIntents.intersect(intentsToFind).isEmpty()) {
            return EvaluationFactory.pass("Patient has had ${category.display()} treatment without the listed intents")
        }


        return EvaluationFactory.pass("Patient has received ${category.display()} therapy for the listed intents in the last $monthsCutoff months")
    }
}