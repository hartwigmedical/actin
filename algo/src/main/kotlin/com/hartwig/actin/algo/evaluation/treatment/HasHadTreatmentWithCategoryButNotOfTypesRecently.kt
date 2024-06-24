package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import java.time.LocalDate

class HasHadTreatmentWithCategoryButNotOfTypesRecently(
    private val category: TreatmentCategory, private val ignoreTypes: Set<TreatmentType>,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentAssessment = record.oncologicalHistory.map { treatmentHistoryEntry ->
            val startedPastMinDate = isAfterDate(minDate, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
            val categoryAndTypeMatch = treatmentHistoryEntry.categories().contains(category)
                    && ignoreTypes.none { treatmentHistoryEntry.isOfType(it) == true }
            TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = TrialFunctions.treatmentMayMatchAsTrial(treatmentHistoryEntry, category)
                        && startedPastMinDate == true
            )
        }.fold(TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val ignoringTypesList = concatItems(ignoreTypes)
        return when {
            treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass("Has received ${category.display()} treatment ignoring $ignoringTypesList")
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received ${category.display()} treatment ignoring $ignoringTypesList but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive ${category.display()} treatment",
                    "Inconclusive ${category.display()} treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Has not had recent ${category.display()} treatment ignoring $ignoringTypesList"
                )
            }
        }
    }

    private data class TreatmentAssessment(
        val hasHadValidTreatment: Boolean = false,
        val hasInconclusiveDate: Boolean = false,
        val hasHadTrialAfterMinDate: Boolean = false
    ) {

        fun combineWith(other: TreatmentAssessment): TreatmentAssessment {
            return TreatmentAssessment(
                hasHadValidTreatment || other.hasHadValidTreatment,
                hasInconclusiveDate || other.hasInconclusiveDate,
                hasHadTrialAfterMinDate || other.hasHadTrialAfterMinDate
            )
        }
    }
}