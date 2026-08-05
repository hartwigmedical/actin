package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

class HasLabValueWithinInstitutionalNormalLimit(private val labels: EvaluationLabels.Laboratory) : SingleLabValueEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val isOutsideRef = labValue.isOutsideRef
            ?: return EvaluationFactory.recoverableUndetermined(
                labels.hasLabValueWithinInstitutionalNormalLimitRecoverableUndetermined(labMeasurement.display())
            )

        return if (isOutsideRef) {
            EvaluationFactory.recoverableFail(
                labels.hasLabValueWithinInstitutionalNormalLimitRecoverableFail(
                    labMeasurement.display().replaceFirstChar { it.uppercase() }
                )
            )
        } else {
            EvaluationFactory.recoverablePass(
                labels.hasLabValueWithinInstitutionalNormalLimitPass(
                    labMeasurement.display().replaceFirstChar { it.uppercase() }
                )
            )
        }
    }
}