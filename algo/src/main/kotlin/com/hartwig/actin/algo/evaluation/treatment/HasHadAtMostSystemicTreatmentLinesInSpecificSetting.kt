package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.partitionTreatmentsByIntent
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
    private val referenceDate: LocalDate,
    private val intentsToIgnore: Set<Intent>,
    private val settingDescription: String,
    private val maximumLines: Int,
    private val category: TreatmentCategory?,
    private val types: Set<TreatmentType>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { entry ->
            entry.treatments.any {
                it.isSystemic && (category == null || category in it.categories()) && (types.isEmpty() || it.types()
                    .any(types::contains))
            }
        }
        val (_, includedIntentTreatments) = priorSystemicTreatments.partitionTreatmentsByIntent(intentsToIgnore)
        val (palliativeIntentTreatments, nonPalliativeTreatments) = includedIntentTreatments.partitionTreatmentsByIntent(setOf(Intent.PALLIATIVE))
        val potentiallyRecentNonPalliativeTreatments = nonPalliativeTreatments.filter {
            TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(it, referenceDate.minusMonths(MONTHS_TO_SUBTRACT))
        }
        val settingMessage = "$settingDescription setting"
        val typesMessage = if (types.isEmpty()) "" else "${Format.concatItemsWithOr(types)} "
        val treatmentMessage = "$typesMessage${category?.display() ?: "systemic"} treatment"
        val linesMessage = "$treatmentMessage line(s)"
        val probableCount = palliativeIntentTreatments.size + potentiallyRecentNonPalliativeTreatments.size

        return when {
            includedIntentTreatments.isEmpty() ->
                EvaluationFactory.pass(
                    "No prior $treatmentMessage in $settingMessage in provided treatments - thus within maximum of $maximumLines line(s)"
                )

            palliativeIntentTreatments.size > maximumLines ->
                EvaluationFactory.fail(
                    "More than $maximumLines $linesMessage with palliative intent in $settingMessage in provided treatments"
                )

            probableCount > maximumLines + 1 ->
                EvaluationFactory.fail(
                    "Likely exceeded maximum of $maximumLines $linesMessage in $settingMessage" +
                            " ($probableCount lines likely in $settingMessage)"
                )

            probableCount > maximumLines ->
                EvaluationFactory.undetermined(
                    "Uncertain whether maximum of $maximumLines $linesMessage in $settingMessage is exceeded" +
                            " ($probableCount lines likely in $settingMessage, setting unclear for some)"
                )

            includedIntentTreatments.size > maximumLines ->
                EvaluationFactory.undetermined(
                    "Uncertain whether maximum of $maximumLines $linesMessage in $settingMessage is exceeded" +
                            " (${includedIntentTreatments.size} lines with non-excluded intent, setting unclear for older lines)"
                )

            else -> EvaluationFactory.pass("At most $maximumLines $linesMessage in $settingMessage in provided treatments")
        }
    }
}
