package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
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
    private val settingDescription: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorTreatments = record.oncologicalHistory.sortedWith(TreatmentHistoryAscendingDateComparator())
        val priorSystemicTreatments = priorTreatments.filter { it.treatments.any(Treatment::isSystemic) }
        val (excludedIntentTreatments, includedIntentTreatments) =
            SystemicTreatmentAnalyser.partitionByIntent(priorSystemicTreatments, intentsToIgnore)
        val (recentPotentiallyCorrectIntentTreatments, nonRecentPotentiallyCorrectIntentTreatments) =
            partitionRecentTreatments(includedIntentTreatments, false)
        val (recentPotentiallyCorrectIntentTreatmentsIncludingUnknown, _) =
            partitionRecentTreatments(includedIntentTreatments, true)
        val potentiallyCorrectIntentTreatmentsWithUnknownStopDate = includedIntentTreatments.filter { it.stopYear() == null }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val settingMessage = "$settingDescription setting"

        return when {
            excludedIntentTreatments.isNotEmpty() && includedIntentTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    createMessage(
                        "Has only had prior systemic treatment with ${
                            Format.concatItemsWithAnd(excludedIntentTreatments.mapNotNull { it.intents }.toSet().flatten())
                        } intent - thus presumably not in $settingMessage",
                        priorSystemicTreatments
                    )
                )
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage("Has had prior systemic treatment in $settingMessage", palliativeIntentTreatments)
                )
            }

            recentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had recent systemic treatment - presumably in $settingMessage",
                        recentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            includedIntentTreatments.size > 1 -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had more than one systemic treatment line of uncertain setting - presumably at least one in $settingMessage",
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
                        "Has had a systemic treatment line not followed by radiotherapy or surgery - presumably in $settingMessage",
                        recentPotentiallyCorrectIntentTreatmentsIncludingUnknown
                    )
                )
            }

            potentiallyCorrectIntentTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment but undetermined if in $settingMessage",
                        potentiallyCorrectIntentTreatmentsWithUnknownStopDate
                    )
                )
            }

            nonRecentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment >6 months ago but undetermined if in $settingMessage",
                        nonRecentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            else -> EvaluationFactory.fail("No prior systemic treatment in $settingMessage")
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
        return nonCurativeTreatments
            .partition { TreatmentVersusDateFunctions.treatmentSinceMinDate(it, referenceDate.minusMonths(6), includeUnknown) }
    }

    private fun createMessage(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return "$string (${concat(treatments.map { it.treatmentDisplay() })})"
    }
}
