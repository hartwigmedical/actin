package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadSpecificFirstLineSystemicTreatment(private val treatmentToFind: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNameToFind = treatmentToFind.name.lowercase()
        val systemicTreatments = record.oncologicalHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (treatmentsWithStartDate, treatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val firstTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(treatmentsWithStartDate)
        val hasHadTreatmentToFindInFirstLine = containsTreatment(listOf(firstTreatment), treatmentNameToFind)
        val hasHadTreatmentToFindWithUnknownStartDate = containsTreatment(treatmentsWithoutStartDate, treatmentNameToFind)
        val hasOnlyHadTreatmentToFind =
            systemicTreatments.isNotEmpty() && systemicTreatments.all { entry ->
                entry.allTreatments().any { it.name.lowercase() == treatmentNameToFind }
            }
        val firstTreatmentIsPotentialTrialMatch =
            firstTreatment?.let { TrialFunctions.treatmentMayMatchAsTrial(it, treatmentToFind.categories()) } ?: false

        val treatmentToFindDisplay = treatmentToFind.display()
        return when {
            (hasHadTreatmentToFindInFirstLine && treatmentsWithoutStartDate.isEmpty()) || hasOnlyHadTreatmentToFind -> {
                EvaluationFactory.pass("Has received $treatmentToFindDisplay as first-line treatment")
            }

            hasHadTreatmentToFindInFirstLine || hasHadTreatmentToFindWithUnknownStartDate -> {
                EvaluationFactory.undetermined("Undetermined if $treatmentToFindDisplay was given as first-line treatment")
            }

            firstTreatmentIsPotentialTrialMatch -> {
                EvaluationFactory.undetermined("Undetermined if first-line trial treatment contained $treatmentToFindDisplay")
            }

            else -> {
                EvaluationFactory.fail("Has not received $treatmentToFindDisplay as first-line treatment")
            }
        }
    }

    private fun containsTreatment(treatments: List<TreatmentHistoryEntry?>, treatmentNameToFind: String): Boolean {
        return treatments.any { it?.allTreatments()?.any { t -> t.name.lowercase() == treatmentNameToFind } == true }
    }
}