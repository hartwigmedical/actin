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

class HasHadTreatmentWithCategoryOfTypesRecently internal constructor(
    private val category: TreatmentCategory, private val types: List<String>,
    private val minDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadValidTreatment = false
        var hasHadTrialAfterMinDate = false
        var hasInconclusiveDate = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            val startedPastMinDate = isAfterDate(minDate, treatment.startYear(), treatment.startMonth())
            if (hasValidCategoryAndType(treatment)) {
                if (startedPastMinDate == null) {
                    hasInconclusiveDate = true
                } else if (startedPastMinDate) {
                    hasHadValidTreatment = true
                }
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL) && startedPastMinDate != null && startedPastMinDate) {
                hasHadTrialAfterMinDate = true
            }
        }
        return if (hasHadValidTreatment) {
            EvaluationFactory.pass(
                "Patient has received " + concat(types) + " " + category.display() + " treatment",
                "Received " + concat(types) + " " + category.display() + " treatment"
            )
        } else if (hasInconclusiveDate) {
            EvaluationFactory.undetermined(
                "Patient has received " + concat(types) + " " + category.display() + " treatment with inconclusive date",
                "Received " + concat(types) + " " + category.display() + " treatment but inconclusive date"
            )
        } else if (hasHadTrialAfterMinDate) {
            EvaluationFactory.undetermined(
                "Patient has participated in a trial recently, inconclusive " + category.display() + " treatment",
                "Inconclusive " + category.display() + " treatment due to trial participation"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received " + concat(types) + " " + category.display() + " treatment",
                "Not received " + concat(types) + " " + category.display() + " treatment"
            )
        }
    }

    private fun hasValidCategoryAndType(treatment: PriorTumorTreatment): Boolean {
        if (treatment.categories().contains(category)) {
            for (type in types) {
                if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                    return true
                }
            }
        }
        return false
    }
}