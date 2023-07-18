package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import java.time.LocalDate

class HasHadTreatmentWithCategoryOfTypesRecently(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentAssessment = record.clinical().treatmentHistory().map { treatmentHistoryEntry ->
            val startedPastMinDate = isAfterDate(minDate, treatmentHistoryEntry.startYear(), treatmentHistoryEntry.startMonth())
            val categoryAndTypeMatch = treatmentHistoryEntry.categories().contains(category)
                    && treatmentHistoryEntry.matchesTypeFromSet(types) == true
            TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(treatmentHistoryEntry, category)
                        && startedPastMinDate == true
            )
        }.fold(TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        val typesList = concat(types.map(TreatmentType::display))
        return when {
            treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass("Has received $typesList ${category.display()} treatment")
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received $typesList ${category.display()} treatment but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive ${category.display()} treatment",
                    "Inconclusive ${category.display()} treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Has not received $typesList ${category.display()} treatment"
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