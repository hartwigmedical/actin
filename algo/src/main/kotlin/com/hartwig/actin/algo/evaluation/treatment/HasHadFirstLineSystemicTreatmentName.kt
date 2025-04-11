package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadFirstLineSystemicTreatmentName(private val treatmentToFind: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (treatmentsWithStartDate, treatmentsWithoutStartDate) = record.oncologicalHistory.partition { it.startYear != null }
        val firstTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(treatmentsWithStartDate)
        val hasHadTreatmentToFindInFirstLine = firstTreatment?.allTreatments()?.contains(treatmentToFind) == true
        val hadTreatmentToFindWithUnknownStartDate = treatmentsWithoutStartDate.any { it.allTreatments().any { t -> t == treatmentToFind } }
        val hasOnlyHadTreatmentToFindButWithUnknownStartDate =
            hadTreatmentToFindWithUnknownStartDate && treatmentsWithoutStartDate.size == 1
        val firstTreatmentIsPotentialTrialMatch = firstTreatment?.isTrial == true && (firstTreatment.categories()
            .containsAll(treatmentToFind.categories()) || firstTreatment.categories().isEmpty())

        val treatmentToFindDisplay = treatmentToFind.display()
        return when {
            (hasHadTreatmentToFindInFirstLine && treatmentsWithoutStartDate.isEmpty()) || hasOnlyHadTreatmentToFindButWithUnknownStartDate -> {
                EvaluationFactory.pass("Has received $treatmentToFindDisplay as first-line treatment")
            }

            hasHadTreatmentToFindInFirstLine || hadTreatmentToFindWithUnknownStartDate -> {
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
}