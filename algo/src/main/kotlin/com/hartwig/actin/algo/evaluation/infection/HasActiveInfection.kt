package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.InfectionStatus

//TODO (ACTIN-38): Update according to README
class HasActiveInfection internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val infection = record.clinicalStatus.infectionStatus
            ?: return EvaluationFactory.undetermined("Infection status data is missing", "Unknown infection status")
        return if (infection.hasActiveInfection) {
            EvaluationFactory.pass(
                "Patient has active infection: " + description(infection),
                "Infection presence: " + description(infection)
            )
        } else {
            EvaluationFactory.fail("Patient has no active infection", "No infection present")
        }
    }

    companion object {
        private fun description(infection: InfectionStatus): String {
            return infection.description ?: "Unknown"
        }
    }
}