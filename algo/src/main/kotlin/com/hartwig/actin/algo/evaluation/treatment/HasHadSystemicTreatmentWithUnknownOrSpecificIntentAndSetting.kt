package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.partitionTreatmentsByIntent
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.partitionTreatmentsByCertainOccurrenceSinceMinDate
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate
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

const val MONTHS_TO_SUBTRACT = 6L

class HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSetting(
    private val referenceDate: LocalDate,
    private val intentsToIgnore: Set<Intent>,
    private val categoryToIgnore: TreatmentCategory?,
    private val settingDescription: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorTreatments = record.oncologicalHistory.sortedWith(TreatmentHistoryAscendingDateComparator())
        val priorSystemicTreatments =
            priorTreatments.filter { it.treatments.any(Treatment::isSystemic) && !it.categories().contains(categoryToIgnore) }
        val (excludedIntentTreatments, includedIntentTreatments) = priorSystemicTreatments.partitionTreatmentsByIntent(intentsToIgnore)
        val (certainRecentPotentiallyCorrectIntentTreatments, nonRecentPotentiallyCorrectIntentTreatments) = includedIntentTreatments.partitionTreatmentsByCertainOccurrenceSinceMinDate(
            referenceDate.minusMonths(MONTHS_TO_SUBTRACT)
        )
        val potentiallyRecentPotentiallyCorrectIntentTreatments =
            includedIntentTreatments.filter { potentialTreatmentSinceMinDate(it, referenceDate.minusMonths(MONTHS_TO_SUBTRACT)) }
        val potentiallyCorrectIntentTreatmentsWithUnknownStopDate = includedIntentTreatments.filter { it.stopYear() == null }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val settingMessage = "$settingDescription setting"
        val categoryToIgnoreMessage = categoryToIgnore?.let { " ignoring ${it.display()}" } ?: ""

        return when {
            excludedIntentTreatments.isNotEmpty() && includedIntentTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    createMessage(
                        "Only prior systemic treatment with ${
                            Format.concatItemsWithAnd(excludedIntentTreatments.mapNotNull { it.intents }.toSet().flatten())
                        } intent in provided treatments - thus presumably not in $settingMessage",
                        priorSystemicTreatments
                    )
                )
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Prior systemic treatment in $settingMessage$categoryToIgnoreMessage in provided treatments",
                        palliativeIntentTreatments
                    )
                )
            }

            certainRecentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Recent systemic treatment$categoryToIgnoreMessage in provided treatments - presumably in $settingMessage",
                        certainRecentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            includedIntentTreatments.size > 1 -> {
                EvaluationFactory.pass(
                    createMessage(
                        "More than one systemic treatment line of uncertain setting$categoryToIgnoreMessage in provided treatments - presumably at least one in $settingMessage",
                        includedIntentTreatments
                    )
                )
            }

            potentiallyRecentPotentiallyCorrectIntentTreatments.size == 1 && !hasRadiotherapyOrSurgeryAfterNonCurativeTreatment(
                priorTreatments,
                potentiallyRecentPotentiallyCorrectIntentTreatments.first()
            ) -> {
                EvaluationFactory.pass(
                    createMessage(
                        "A systemic treatment line$categoryToIgnoreMessage was not followed by radiotherapy or surgery - presumably in $settingMessage",
                        potentiallyRecentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            potentiallyCorrectIntentTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Prior systemic treatment$categoryToIgnoreMessage in provided treatments but undetermined if in $settingMessage",
                        potentiallyCorrectIntentTreatmentsWithUnknownStopDate
                    )
                )
            }

            nonRecentPotentiallyCorrectIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Prior systemic treatment$categoryToIgnoreMessage >6 months ago in provided treatments but undetermined if in $settingMessage",
                        nonRecentPotentiallyCorrectIntentTreatments
                    )
                )
            }

            else -> EvaluationFactory.fail("No prior systemic treatment in $settingMessage in provided treatments")
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

    private fun createMessage(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return "$string (${concat(treatments.map { it.treatmentDisplay() })})"
    }
}
