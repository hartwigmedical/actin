package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
    private val referenceDate: LocalDate,
    private val intentsToIgnore: Set<Intent>,
    private val settingDescription: String,
    private val maximumLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { it.treatments.any(Treatment::isSystemic) }
        val (_, includedIntentTreatments) = SystemicTreatmentAnalyser.partitionByIntent(priorSystemicTreatments, intentsToIgnore)
        val palliativeIntentTreatments = includedIntentTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val nonPalliativeIncludedTreatments = includedIntentTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) != true }
        val (recentUncertainTreatments, _) =
            SystemicTreatmentAnalyser.partitionRecentTreatments(nonPalliativeIncludedTreatments, referenceDate.minusMonths(6), true)
        val settingMessage = "$settingDescription setting"
        val probableCount = palliativeIntentTreatments.size + recentUncertainTreatments.size

        return when {
            includedIntentTreatments.isEmpty() ->
                EvaluationFactory.pass("Has had no prior systemic treatment in $settingMessage - within maximum of $maximumLines line(s)")

            palliativeIntentTreatments.size > maximumLines ->
                EvaluationFactory.fail(
                    createMessage(
                        "Has had more than $maximumLines systemic treatment line(s) with palliative intent in $settingMessage",
                        palliativeIntentTreatments
                    )
                )

            probableCount > maximumLines + 1 ->
                EvaluationFactory.fail(
                    createMessage(
                        "Likely exceeded maximum of $maximumLines systemic treatment line(s) in $settingMessage" +
                                " ($probableCount lines likely in $settingMessage)",
                        includedIntentTreatments
                    )
                )

            probableCount > maximumLines ->
                EvaluationFactory.undetermined(
                    createMessage(
                        "Uncertain whether maximum of $maximumLines systemic treatment line(s) in $settingMessage is exceeded" +
                                " ($probableCount lines likely in $settingMessage, setting unclear for some)",
                        includedIntentTreatments
                    )
                )

            includedIntentTreatments.size > maximumLines ->
                EvaluationFactory.undetermined(
                    createMessage(
                        "Uncertain whether maximum of $maximumLines systemic treatment line(s) in $settingMessage is exceeded" +
                                " (${includedIntentTreatments.size} lines with non-excluded intent, setting unclear for older lines)",
                        includedIntentTreatments
                    )
                )

            else ->
                EvaluationFactory.pass(
                    createMessage(
                        "Has had at most $maximumLines systemic treatment line(s) in $settingMessage",
                        includedIntentTreatments
                    )
                )
        }
    }

    private fun createMessage(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return "$string (${concat(treatments.map { it.treatmentDisplay() })})"
    }
}
