package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadSomeTreatmentsWithCategoryWithIntentsRecently(
    private val category: TreatmentCategory,
    private val intentsToFind: Set<Intent>,
    private val minDate: LocalDate,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            category, {
                historyEntry -> historyEntry.intents?.intersect(intentsToFind)?.isNotEmpty() == true
                    && isAfterDate(minDate, historyEntry.treatmentHistoryDetails?.stopYear, historyEntry.treatmentHistoryDetails?.stopMonth) == true
            })
        val intentsList = Format.concatItemsWithOr(intentsToFind)

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.pass("Has received $intentsList ${category.display()} ($treatmentDisplay)")
            }

            treatmentSummary.hasApproximateMatch() -> {
                EvaluationFactory.undetermined("Undetermined if received ${category.display()} is $intentsList")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial included $intentsList ${category.display()}")
            }

            else -> {
                EvaluationFactory.fail("Has not received $intentsList ${category.display()}")
            }
        }
    }
}
/*
val evaluation = HasHadSomeTreatmentsWithCategoryWithIntents(category, intentsToFind).evaluate(record)
        return when (evaluation.equals(EvaluationResult.PASS) && true){
            true -> EvaluationFactory.pass(
                "Patient has had treatment with category ${category.display()} and intents " +
                    "${Format.concatItemsWithOr(intentsToFind)}} after $minDate")
            else -> EvaluationFactory.undetermined(
                "Undetermined if has had treatment with category ${category.display()} and intents " +
                        "${Format.concatItemsWithOr(intentsToFind)}} after $minDate")
        }




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
 */