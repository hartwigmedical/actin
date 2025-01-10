package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadSomeTreatmentsWithCategory(private val category: TreatmentCategory, private val minTreatmentLines: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(effectiveTreatmentHistory, category)

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("Has received at least $minTreatmentLines line(s) of ${category.display()}")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Inconclusive if received at least $minTreatmentLines line(s) of ${category.display()} due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received at least $minTreatmentLines line(s) of ${category.display()}")
            }
        }
    }
}