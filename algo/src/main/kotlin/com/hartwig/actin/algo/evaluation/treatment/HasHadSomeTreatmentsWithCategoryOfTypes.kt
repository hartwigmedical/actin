package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadSomeTreatmentsWithCategoryOfTypes(
    private val category: TreatmentCategory, private val types: List<String>, private val minTreatmentLines: Int
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var numMatchingTreatmentLines = 0
        var numApproximateTreatmentLines = 0
        var numOtherTrials = 0
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    if (hasValidType(treatment)) {
                        numMatchingTreatmentLines++
                    }
                } else {
                    numApproximateTreatmentLines++
                }
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                numOtherTrials++
            }
        }
        return if (numMatchingTreatmentLines >= minTreatmentLines) {
            EvaluationFactory.pass(
                "Patient has received at least $minTreatmentLines line(s) of ${concat(types)} ${category.display()}",
                "Has received at least $minTreatmentLines line(s) of ${concat(types)} ${category.display()}"
            )
        } else if (numMatchingTreatmentLines + numApproximateTreatmentLines + numOtherTrials >= minTreatmentLines) {
            EvaluationFactory.undetermined(
                "Can't determine whether patient has received at least $minTreatmentLines line(s) of ${concat(types)} ${category.display()}",
                "Undetermined if received at least $minTreatmentLines line(s) of ${concat(types)} ${category.display()}"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received at least $minTreatmentLines line(s) of ${concat(types)} ${category.display()}",
                "Has not received at least $minTreatmentLines line(s) of ${concat(types)} ${category.display()}"
            )
        }
    }

    private fun hasValidType(treatment: PriorTumorTreatment): Boolean {
        for (type in types) {
            if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                return true
            }
        }
        return false
    }
}