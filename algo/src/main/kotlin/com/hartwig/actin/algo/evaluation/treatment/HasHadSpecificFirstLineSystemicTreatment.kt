package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.containsTreatment
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadSpecificFirstLineSystemicTreatment(
    private val treatmentToFind: Treatment,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNameToFind = treatmentToFind.name
        val systemicTreatments = record.oncologicalHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (treatmentsWithStartDate, treatmentsWithoutStartDate) = systemicTreatments.partition { it.startYear != null }
        val firstTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(treatmentsWithStartDate)
        val hasHadTreatmentToFindInFirstLine = firstTreatment?.containsTreatment(treatmentNameToFind) == true
        val hasHadTreatmentToFindWithUnknownStartDate = treatmentsWithoutStartDate.any { it.containsTreatment(treatmentNameToFind) }
        val hasOnlyHadTreatmentToFind =
            systemicTreatments.isNotEmpty() && systemicTreatments.all { it.containsTreatment(treatmentNameToFind) }
        val firstTreatmentIsPotentialTrialMatch =
            firstTreatment?.let { TrialFunctions.treatmentMayMatchAsTrial(it, treatmentToFind.categories()) } ?: false
        val treatmentToFindDisplay = treatmentToFind.display()

        return when {
            (hasHadTreatmentToFindInFirstLine && treatmentsWithoutStartDate.isEmpty()) || hasOnlyHadTreatmentToFind -> {
                EvaluationFactory.pass(labels.hasHadSpecificFirstLineSystemicTreatmentPass(treatmentToFindDisplay))
            }

            hasHadTreatmentToFindInFirstLine || hasHadTreatmentToFindWithUnknownStartDate -> {
                EvaluationFactory.undetermined(labels.hasHadSpecificFirstLineSystemicTreatmentUndetermined(treatmentToFindDisplay))
            }

            firstTreatmentIsPotentialTrialMatch -> {
                EvaluationFactory.undetermined(labels.hasHadSpecificFirstLineSystemicTreatmentUndeterminedTrial(treatmentToFindDisplay))
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadSpecificFirstLineSystemicTreatmentFail(treatmentToFindDisplay))
            }
        }
    }
}