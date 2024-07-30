package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

class HasHadSomeTreatmentsWithCategoryWithIntents(private val category: TreatmentCategory, private val intentsToFind: Set<Intent>) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            category,
            { historyEntry -> matchesIntentFromSet(intentsToFind, historyEntry.intents) })
        val intentsList = Format.concatItemsWithOr(intentsToFind)

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.pass(
                    "Patient has received $intentsList ${category.display()} ($treatmentDisplay)",
                    "Has received $intentsList ${category.display()} ($treatmentDisplay)"
                )
            }

            treatmentSummary.hasApproximateMatch() -> {
                EvaluationFactory.undetermined(
                    "Can't determine if intent of received ${category.display()} is $intentsList",
                    "Undetermined if received ${category.display()} is $intentsList"
                )
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined(
                    "Patient may have received $intentsList ${category.display()} due to trial participation",
                    "Undetermined if received $intentsList ${category.display()} due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received $intentsList ${category.display()}",
                    "Has not received $intentsList ${category.display()}"
                )
            }
        }
    }

    private fun matchesIntentFromSet(intentsToFind: Set<Intent>, intents: Set<Intent>?): Boolean? {
        return if (!intents.isNullOrEmpty()) intents.any { intent -> intentsToFind.contains(intent) } else null
    }
}
