package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
    private val referenceDate: LocalDate,
    private val intentsToIgnore: Set<Intent>,
    private val settingDescription: String,
    private val maximumLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { it.treatments.any(Treatment::isSystemic) }
        val (_, includedIntentTreatments) = SystemicTreatmentAnalyser.partitionTreatmentsByIntent(priorSystemicTreatments, intentsToIgnore)
        val (palliativeIntentTreatments, nonPalliativeIncludedTreatments) = includedIntentTreatments.partition { it.intents?.contains(Intent.PALLIATIVE) == true }
        val recentAndPotentiallyRecentNonPalliativeTreatments = nonPalliativeIncludedTreatments.filter {
            potentialTreatmentSinceMinDate(
                it,
                referenceDate.minusMonths(MONTHS_TO_SUBTRACT)
            )
        }
        val settingMessage = "$settingDescription setting"
        val probableCount = palliativeIntentTreatments.size + recentAndPotentiallyRecentNonPalliativeTreatments.size

        return when {
            includedIntentTreatments.isEmpty() ->
                EvaluationFactory.pass("Has had no prior systemic treatment in $settingMessage - thus within maximum of $maximumLines line(s)")

            palliativeIntentTreatments.size > maximumLines ->
                EvaluationFactory.fail(
                    "Has had more than $maximumLines systemic treatment line(s) with palliative intent in $settingMessage"
                )

            probableCount > maximumLines + 1 ->
                EvaluationFactory.fail(
                    "Likely exceeded maximum of $maximumLines systemic treatment line(s) in $settingMessage" +
                            " ($probableCount lines likely in $settingMessage)"
                )

            probableCount > maximumLines ->
                EvaluationFactory.undetermined(
                    "Uncertain whether maximum of $maximumLines systemic treatment line(s) in $settingMessage is exceeded" +
                            " ($probableCount lines likely in $settingMessage, setting unclear for some)"
                )

            includedIntentTreatments.size > maximumLines ->
                EvaluationFactory.undetermined(
                    "Uncertain whether maximum of $maximumLines systemic treatment line(s) in $settingMessage is exceeded" +
                            " (${includedIntentTreatments.size} lines with non-excluded intent, setting unclear for older lines)"
                )

            else -> EvaluationFactory.pass("Has had at most $maximumLines systemic treatment line(s) in $settingMessage")
        }
    }
}
