package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.Treatment

class HasHadClinicalBenefitFollowingSomeTreatment(private val treatment: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Currently undetermined if the patient has had objective clinical benefit following therapy with {$treatment.name}",
            "Undetermined clinical benefit following {$treatment.name} therapy"
        )
    }
}