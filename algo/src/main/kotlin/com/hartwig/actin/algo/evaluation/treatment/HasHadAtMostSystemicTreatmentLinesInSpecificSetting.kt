package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
    private val referenceDate: LocalDate,
    private val intentsToIgnore: Set<Intent>,
    private val settingDescription: String,
    private val maximumLines: Int,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { it.treatments.any(Treatment::isSystemic) }
        val (_, includedIntentTreatments) = SystemicTreatmentAnalyser.partitionByIntent(priorSystemicTreatments, intentsToIgnore)
        val palliativeIntentTreatments = includedIntentTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val nonPalliativeIncludedTreatments = includedIntentTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) != true }
        val (recentUncertainTreatments, _) =
            SystemicTreatmentAnalyser.partitionRecentTreatments(nonPalliativeIncludedTreatments, referenceDate.minusMonths(6), true)
        val settingMessage = labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingSettingMessage(settingDescription)
        val probableCount = palliativeIntentTreatments.size + recentUncertainTreatments.size

        return when {
            includedIntentTreatments.isEmpty() ->
                EvaluationFactory.pass(
                    labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingPassNoPrior(settingMessage, maximumLines)
                )

            palliativeIntentTreatments.size > maximumLines ->
                EvaluationFactory.fail(
                    labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingFailPalliative(maximumLines, settingMessage)
                )

            probableCount > maximumLines + 1 ->
                EvaluationFactory.fail(
                    labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingFailLikelyExceeded(
                        maximumLines, settingMessage, probableCount
                    )
                )

            probableCount > maximumLines ->
                EvaluationFactory.undetermined(
                    labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingUndeterminedUncertain(
                        maximumLines, settingMessage, probableCount
                    )
                )

            includedIntentTreatments.size > maximumLines ->
                EvaluationFactory.undetermined(
                    labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingUndeterminedNonExcluded(
                        maximumLines, settingMessage, includedIntentTreatments.size
                    )
                )

            else ->
                EvaluationFactory.pass(
                    labels.hasHadAtMostSystemicTreatmentLinesInSpecificSettingPassAtMost(maximumLines, settingMessage)
                )
        }
    }
}
