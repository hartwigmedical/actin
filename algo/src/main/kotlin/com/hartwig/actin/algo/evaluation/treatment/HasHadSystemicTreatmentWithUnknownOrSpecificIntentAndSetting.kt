package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSetting(
    private val referenceDate: LocalDate,
    private val intentsToIgnore: Set<Intent>,
    private val categoryToIgnore: TreatmentCategory?,
    private val settingDescription: String,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorTreatments = record.oncologicalHistory.sortedWith(TreatmentHistoryAscendingDateComparator())
        val priorSystemicTreatments =
            priorTreatments.filter { it.treatments.any(Treatment::isSystemic) && !it.categories().contains(categoryToIgnore) }
        val (excludedIntentTreatments, includedIntentTreatments) =
            SystemicTreatmentAnalyser.partitionByIntent(priorSystemicTreatments, intentsToIgnore)
        val (recentPotentiallyCorrectIntentTreatments, nonRecentPotentiallyCorrectIntentTreatments) =
            partitionRecentTreatments(includedIntentTreatments, false)
        val (recentPotentiallyCorrectIntentTreatmentsIncludingUnknown, _) =
            partitionRecentTreatments(includedIntentTreatments, true)
        val potentiallyCorrectIntentTreatmentsWithUnknownStopDate = includedIntentTreatments.filter { it.stopYear() == null }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val settingMessage = labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingSettingMessage(settingDescription)
        val categoryToIgnoreMessage = categoryToIgnore?.let {
            labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingCategoryToIgnoreMessage(it.display())
        } ?: ""

        return when {
            excludedIntentTreatments.isNotEmpty() && includedIntentTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingFailExcludedIntent(
                            Format.concatItemsWithAnd(excludedIntentTreatments.mapNotNull { it.intents }.toSet().flatten()),
                            settingMessage
                        ),
                        priorSystemicTreatments
                    )
                )
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassInSetting(settingMessage, categoryToIgnoreMessage),
                        palliativeIntentTreatments
                    )
                )
            }

            recentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassRecent(categoryToIgnoreMessage, settingMessage),
                        recentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            includedIntentTreatments.size > 1 -> {
                EvaluationFactory.pass(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassMultipleLines(
                            categoryToIgnoreMessage, settingMessage
                        ),
                        includedIntentTreatments
                    )
                )
            }

            recentPotentiallyCorrectIntentTreatmentsIncludingUnknown.size == 1 && !hasRadiotherapyOrSurgeryAfterNonCurativeTreatment(
                priorTreatments,
                recentPotentiallyCorrectIntentTreatmentsIncludingUnknown.first()
            ) -> {
                EvaluationFactory.pass(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassNotFollowedByRadiotherapyOrSurgery(
                            categoryToIgnoreMessage, settingMessage
                        ),
                        recentPotentiallyCorrectIntentTreatmentsIncludingUnknown
                    )
                )
            }

            potentiallyCorrectIntentTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingUndeterminedUnknownStopDate(
                            categoryToIgnoreMessage, settingMessage
                        ),
                        potentiallyCorrectIntentTreatmentsWithUnknownStopDate
                    )
                )
            }

            nonRecentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingUndeterminedNonRecent(
                            categoryToIgnoreMessage, settingMessage
                        ),
                        nonRecentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            else -> EvaluationFactory.fail(
                labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingFailNoPriorSystemicTreatment(settingMessage)
            )
        }
    }

    private fun hasRadiotherapyOrSurgeryAfterNonCurativeTreatment(
        priorTreatments: List<TreatmentHistoryEntry>,
        nonCurativeTreatment: TreatmentHistoryEntry
    ): Boolean {
        return priorTreatments.drop(priorTreatments.indexOf(nonCurativeTreatment) + 1).any { entry ->
            entry.treatments.any {
                it.categories().contains(TreatmentCategory.RADIOTHERAPY) || it.categories().contains(TreatmentCategory.SURGERY)
            }
        }
    }

    private fun partitionRecentTreatments(
        nonCurativeTreatments: List<TreatmentHistoryEntry>,
        includeUnknown: Boolean
    ): Pair<List<TreatmentHistoryEntry>, List<TreatmentHistoryEntry>> {
        return SystemicTreatmentAnalyser.partitionRecentTreatments(nonCurativeTreatments, referenceDate.minusMonths(6), includeUnknown)
    }

    private fun createMessage(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return labels.hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingWithTreatments(
            string, concat(treatments.map { it.treatmentDisplay() })
        )
    }
}
