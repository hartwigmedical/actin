package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsAnyCypInhibitingOrInducingMedication(private val selector: MedicationSelector) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypMedications = medications.filter { medication ->
            medication.cypInteractions.any { it.type == DrugInteraction.Type.INDUCER || it.type == DrugInteraction.Type.INHIBITOR }
        }

        val activeCypMedications = cypMedications.filter { selector.isActive(it) }.map { it.name }.toSet()
        val plannedCypMedications = cypMedications.filter { selector.isPlanned(it) }.map { it.name }.toSet()

        return when {
            activeCypMedications.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "CYP inhibiting or inducing medication use (${concatLowercaseWithCommaAndAnd(activeCypMedications)})"
                )
            }

            plannedCypMedications.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Planned CYP inhibiting or inducing medication (${concatLowercaseWithCommaAndAnd(plannedCypMedications)})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail("No CYP inhibiting or inducing medication use")
            }
        }
    }
}