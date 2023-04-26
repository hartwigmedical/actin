package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TreatmentCategory

class HasHadLimitedTreatmentsWithCategory internal constructor(
    private val category: TreatmentCategory,
    private val maxTreatmentLines: Int
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var numTreatmentLines = 0
        var numOtherTrials = 0
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                numTreatmentLines++
            } else if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                numOtherTrials++
            }
        }
        return when {
            numTreatmentLines + numOtherTrials <= maxTreatmentLines -> {
                EvaluationFactory.pass(
                    "Patient has received at most " + maxTreatmentLines + " lines of " + category.display() + " treatment",
                    "Has received at most " + maxTreatmentLines + " lines of " + category.display()
                )
            }

            numTreatmentLines <= maxTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Patient may have received more than " + maxTreatmentLines + " lines of " + category.display() + " treatment",
                    "Undetermined if received at most " + maxTreatmentLines + " lines of " + category.display()
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has received more than " + maxTreatmentLines + " lines of " + category.display(),
                    "Has not received at most " + maxTreatmentLines + " lines of " + category.display()
                )
            }
        }
    }
}