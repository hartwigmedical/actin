package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadTreatmentWithCategoryButNotOfTypes internal constructor(
    private val category: TreatmentCategory,
    private val ignoreTypes: List<String>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadValidTreatment = false
        var hasHadOtherTrial = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                var hasCorrectType = true
                for (ignoreType in ignoreTypes) {
                    if (TreatmentTypeResolver.isOfType(treatment, category, ignoreType)) {
                        hasCorrectType = false
                    }
                }
                if (hasCorrectType) {
                    hasHadValidTreatment = true
                }
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadOtherTrial = true
            }
        }
        return if (hasHadValidTreatment) {
            unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages(
                    "Patient received " + category.display() + " ignoring " + concat(
                        ignoreTypes
                    )
                )
                .addPassGeneralMessages(
                    "Has received " + category.display() + " ignoring " + concat(
                        ignoreTypes
                    )
                )
                .build()
        } else if (hasHadOtherTrial) {
            unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "Patient may have received " + category.display() + " ignoring " + concat(
                        ignoreTypes + " due to trial participation"
                    )
                )
                .addUndeterminedGeneralMessages(
                    "Undetermined if received " + category.display() + "," +
                            " ignoring " + concat(
                        ignoreTypes
                    ) + " due to trial participation"
                )
                .build()
        } else {
            unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(
                    "Patient has not received " + category.display() + " ignoring " + concat(
                        ignoreTypes
                    )
                )
                .addFailGeneralMessages(
                    "Has not received " + category.display() + " ignoring " + concat(
                        ignoreTypes
                    )
                )
                .build()
        }
    }
}