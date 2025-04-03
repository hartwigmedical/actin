package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSystemicTreatmentInAdvancedOrMetastaticSetting(private val referenceDate: LocalDate) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSystemicTreatments = record.oncologicalHistory.filter { entry -> entry.treatments.any { it.isSystemic } }
            .sortedWith(TreatmentHistoryAscendingDateComparator())
        val (curativeTreatments, nonCurativeTreatments) = priorSystemicTreatments.partition { it.intents?.contains(Intent.CURATIVE) == true }
        val (recentNonCurativeTreatments, nonRecentNonCurativeTreatments) = nonCurativeTreatments
            .partition { TreatmentSinceDateFunctions.treatmentSinceMinDate(it, referenceDate.minusMonths(6), false) }
        val nonCurativeTreatmentsWithUnknownStopDate = nonCurativeTreatments.filter { it.treatmentHistoryDetails?.stopYear == null }
        val palliativeIntentTreatments = priorSystemicTreatments.filter { it.intents?.contains(Intent.PALLIATIVE) == true }

        return when {
            curativeTreatments.isNotEmpty() && nonCurativeTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    createMessage(
                        "Has only had prior systemic treatment with curative intent - thus presumably not in metastatic or advanced setting",
                        priorSystemicTreatments
                    )
                )
            }

            palliativeIntentTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had prior systemic treatment in metastatic or advanced setting",
                        palliativeIntentTreatments
                    )
                )
            }

            recentNonCurativeTreatments.isNotEmpty() -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had recent systemic treatment - presumably in metastatic or advanced setting",
                        recentNonCurativeTreatments
                    )
                )
            }

            nonCurativeTreatments.size > 1 -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had more than one systemic lines with unknown or non-curative intent " +
                                "- presumably at least one in metastatic or advanced setting",
                        nonCurativeTreatments
                    )
                )
            }

           nonCurativeTreatments.size == 1 && !hasRadiotherapyOrSurgeryAfterNonCurativeTreatment(
                priorSystemicTreatments,
                nonCurativeTreatments.first()
            ) -> {
                EvaluationFactory.pass(
                    createMessage(
                        "Has had a systemic line with unknown or non-curative intent not followed by radiotherapy or surgery " +
                                "- thus presumably in metastatic or advanced setting",
                        nonCurativeTreatments
                    )
                )
            }

            nonCurativeTreatmentsWithUnknownStopDate.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment but undetermined if in metastatic or advanced setting",
                        nonCurativeTreatmentsWithUnknownStopDate
                    )
                )
            }

            nonRecentNonCurativeTreatments.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    createMessage(
                        "Has had prior systemic treatment >6 months ago but undetermined if in metastatic or advanced setting",
                        nonRecentNonCurativeTreatments
                    )
                )
            }

            else -> EvaluationFactory.fail("No prior systemic treatment in metastatic or advanced setting")
        }
    }

    private fun hasRadiotherapyOrSurgeryAfterNonCurativeTreatment(
        priorSystemicTreatments: List<TreatmentHistoryEntry>,
        nonCurativeTreatment: TreatmentHistoryEntry
    ): Boolean {
        return priorSystemicTreatments.drop(priorSystemicTreatments.indexOf(nonCurativeTreatment) + 1).any { entry ->
            entry.treatments.any {
                it.categories().contains(TreatmentCategory.RADIOTHERAPY) || it.categories().contains(TreatmentCategory.SURGERY)
            }
        }
    }

    private fun createMessage(string: String, treatments: List<TreatmentHistoryEntry>): String {
        return "$string (${concat(treatments.map { it.treatmentDisplay() })})"
    }
}