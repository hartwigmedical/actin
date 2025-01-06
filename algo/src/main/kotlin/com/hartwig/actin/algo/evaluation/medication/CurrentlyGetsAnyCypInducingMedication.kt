package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsAnyCypInducingMedication(private val selector: MedicationSelector) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypInducersReceived =
            selector.activeWithInteraction(medications, null, DrugInteraction.Type.INDUCER, DrugInteraction.Group.CYP).map { it.name }

        val cypInducersPlanned =
            selector.plannedWithInteraction(medications, null, DrugInteraction.Type.INDUCER, DrugInteraction.Group.CYP).map { it.name }

        return when {
            cypInducersReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass("CYP inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}")
            }

            cypInducersPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn("Planned CYP inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersPlanned)}")
            }

            else -> {
                EvaluationFactory.recoverableFail("No CYP inducing medication use ")
            }
        }
    }
}