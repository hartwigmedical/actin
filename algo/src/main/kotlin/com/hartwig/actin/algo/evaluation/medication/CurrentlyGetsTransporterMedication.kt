package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsTransporterMedication(
    private val selector: MedicationSelector,
    private val name: String,
    private val type: DrugInteraction.Type
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val transporterReceived =
            selector.activeWithInteraction(medications, name, type, "transporter").map { it.name }

        val transporterPlanned =
            selector.plannedWithInteraction(medications, name, type, "transporter").map { it.name }

        val typeText = type.name.lowercase()

        return when {
            transporterReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets $name $typeText medication: ${Format.concatLowercaseWithAnd(transporterReceived)}",
                    "$name inhibiting medication use: ${Format.concatLowercaseWithAnd(transporterReceived)}"
                )
            }

            transporterPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get $name $typeText medication: ${Format.concatLowercaseWithAnd(transporterPlanned)}",
                    "Planned $name $typeText medication use: ${Format.concatLowercaseWithAnd(transporterPlanned)}"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get $name $typeText medication",
                    "No $name $typeText medication use"
                )
            }
        }
    }
}