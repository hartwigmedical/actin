package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.time.LocalDate

class HasHadTreatmentWithCategoryOfTypesRecently(
    private val category: TreatmentCategory, private val types: List<String>,
    private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentAssessment = record.clinical().priorTumorTreatments().map { treatment ->
            val startedPastMinDate = isAfterDate(minDate, treatment.startYear(), treatment.startMonth())
            val categoryAndTypeMatch = hasValidCategoryAndType(treatment)
            TreatmentAssessment(
                hasHadValidTreatment = categoryAndTypeMatch && startedPastMinDate == true,
                hasInconclusiveDate = categoryAndTypeMatch && startedPastMinDate == null,
                hasHadTrialAfterMinDate = TreatmentSummaryForCategory.treatmentMayMatchCategoryAsTrial(treatment, category)
                        && startedPastMinDate == true
            )
        }.fold(TreatmentAssessment()) { acc, element -> acc.combineWith(element) }

        return when {
            treatmentAssessment.hasHadValidTreatment -> {
                EvaluationFactory.pass("Has received ${concat(types)} ${category.display()} treatment")
            }

            treatmentAssessment.hasInconclusiveDate -> {
                EvaluationFactory.undetermined("Has received ${concat(types)} ${category.display()} treatment but inconclusive date")
            }

            treatmentAssessment.hasHadTrialAfterMinDate -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive ${category.display()} treatment",
                    "Inconclusive ${category.display()} treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Has not received ${concat(types)} ${category.display()} treatment"
                )
            }
        }
    }

    private fun hasValidCategoryAndType(treatment: PriorTumorTreatment): Boolean {
        return treatment.categories().contains(category) && types.any { TreatmentTypeResolver.isOfType(treatment, category, it) }
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