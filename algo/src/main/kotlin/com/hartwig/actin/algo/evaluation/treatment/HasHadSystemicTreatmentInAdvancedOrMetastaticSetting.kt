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

class HasHadSystemicTreatmentInAdvancedOrMetastaticSetting(private val referenceDate: LocalDate, private val metastaticOnly: Boolean) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val priorTreatments = record.oncologicalHistory.sortedWith(TreatmentHistoryAscendingDateComparator())
        val priorSystemicTreatments = priorTreatments.filter { it.treatments.any(Treatment::isSystemic) }
        val wrongIntents = if (metastaticOnly) Intent.curativeAdjuvantNeoadjuvantSet() else setOf(Intent.CURATIVE)
        val (wrongIntentTreatments, potentiallyCorrectIntentTreatments) =
            priorSystemicTreatments.partition { it.intents?.any { intent -> wrongIntents.contains(intent) } == true }
        val (recentPotentiallyCorrectIntentTreatments, nonRecentPotentiallyCorrectIntentTreatments) =
            partitionRecentTreatments(potentiallyCorrectIntentTreatments, false)
        val (recentPotentiallyCorrectIntentTreatmentsIncludingUnknown, _) =
            partitionRecentTreatments(potentiallyCorrectIntentTreatments, true)
        val potentiallyCorrectIntentTreatmentsWithUnknownStopDate = potentiallyCorrectIntentTreatments.filter { it.stopYear() == null }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val messageEnding = if (metastaticOnly) "metastatic setting" else "metastatic or advanced setting"
        val wrongIntentMessage = if (metastaticOnly) "non (neo)adjuvant/curative intent" else "non-curative intent"

        return when {
            wrongIntentTreatments.isNotEmpty() && potentiallyCorrectIntentTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    createMessage(
                        "Has only had prior systemic treatment with ${
                            Format.concatItemsWithAnd(wrongIntentTreatments.mapNotNull { it.intents }.toSet().flatten())
                        } intent - thus presumably not in $messageEnding",
                        priorSystemicTreatments
                    )
                )
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage("Has had prior systemic treatment in $messageEnding", palliativeIntentTreatments)
                )
            }

            recentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had recent systemic treatment - presumably in $messageEnding",
                        recentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            potentiallyCorrectIntentTreatments.size > 1 -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had more than one systemic lines with unknown or $wrongIntentMessage" +
                                "- presumably at least one in $messageEnding",
                        potentiallyCorrectIntentTreatments
                    )
                )
            }

            recentPotentiallyCorrectIntentTreatmentsIncludingUnknown.size == 1 && !hasRadiotherapyOrSurgeryAfterNonCurativeTreatment(
                priorTreatments,
                recentPotentiallyCorrectIntentTreatmentsIncludingUnknown.first()
            ) -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had a systemic line with unknown or $wrongIntentMessage not followed by radiotherapy or surgery " +
                                "- thus presumably in $messageEnding",
                        recentPotentiallyCorrectIntentTreatmentsIncludingUnknown
                    )
                )
            }

            potentiallyCorrectIntentTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment but undetermined if in $messageEnding",
                        potentiallyCorrectIntentTreatmentsWithUnknownStopDate
                    )
                )
            }

            nonRecentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment >6 months ago but undetermined if in $messageEnding",
                        nonRecentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            else -> EvaluationFactory.fail("No prior systemic treatment in $messageEnding")
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