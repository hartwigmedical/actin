package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadLimitedTreatmentsWithCategoryOfTypes internal constructor(
    private val category: TreatmentCategory, private val types: List<String>,
    private val maxTreatmentLines: Int
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
        return if (numMatchingTreatmentLines + numApproximateTreatmentLines + numOtherTrials <= maxTreatmentLines) {
            unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages(
                    "Patient has received at most $maxTreatmentLines lines of " + concat(
                        types
                    ) + " "
                            + category.display()
                )
                .addPassGeneralMessages(
                    "Has received at most " + maxTreatmentLines + " lines of " + concat(types) + " " + category.display()
                )
                .build()
        } else if (numMatchingTreatmentLines <= maxTreatmentLines) {
            unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "Can't determine whether patient has received at most " + maxTreatmentLines + " lines of "
                            + concat(types) + " " + category.display()
                )
                .addUndeterminedGeneralMessages(
                    "Unclear if has received at most $maxTreatmentLines lines of " + concat(
                        types
                    ) + " "
                            + category.display()
                )
                .build()
        } else {
            unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(
                    "Patient has not received at most $maxTreatmentLines lines of " + concat(
                        types
                    ) + " "
                            + category.display()
                )
                .addFailGeneralMessages(
                    "Has not received at most $maxTreatmentLines lines of " + concat(
                        types
                    ) + " "
                            + category.display()
                )
                .build()
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