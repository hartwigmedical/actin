package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory.fail
import com.hartwig.actin.algo.evaluation.EvaluationFactory.pass
import com.hartwig.actin.algo.evaluation.EvaluationFactory.undetermined
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPDFollowingTreatmentWithCategory internal constructor(private val category: TreatmentCategory) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadPDFollowingTreatmentWithCategory = false
        var hasHadTreatmentWithUnclearPDStatus = false
        var hasHadTrial = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                when (ProgressiveDiseaseFunctions.treatmentResultedInPDOption(treatment)) {
                    true -> {
                        hasHadPDFollowingTreatmentWithCategory = true
                    }

                    null -> {
                        hasHadTreatmentWithUnclearPDStatus = true
                    }

                    else -> {}
                }
            }
            if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadTrial = true
            }
        }
        return if (hasHadPDFollowingTreatmentWithCategory) {
            pass(
                "Patient has had progressive disease following treatment with category " + category.display(),
                "Has had " + category.display() + " treatment with PD"
            )
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            undetermined(
                "Patient has had treatment with category " + category.display() + " but unclear PD status",
                "Has had " + category.display() + " treatment but undetermined PD status"
            )
        } else if (hasHadTrial) {
            undetermined(
                "Patient has had trial with unclear treatment category",
                "Trial treatment of unclear treatment category"
            )
        } else {
            fail(
                "Patient has no progressive disease following treatment with category " + category.display(),
                "Has not had " + category.display() + " treatment with PD"
            )
        }
    }
}