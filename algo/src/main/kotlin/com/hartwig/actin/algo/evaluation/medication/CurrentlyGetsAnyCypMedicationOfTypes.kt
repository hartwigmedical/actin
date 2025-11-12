package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsAnyCypMedicationOfTypes(private val selector: MedicationSelector, private val types: Set<DrugInteraction.Type>) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypMedications = medications.filter { medication ->
            medication.cypInteractions.any { it.type in types }
        }

        val activeCypMedications = cypMedications.filter { selector.isActive(it) }.map { it.name }.toSet()
        val plannedCypMedications = cypMedications.filter { selector.isPlanned(it) }.map { it.name }.toSet()

        val type = Format.concatLowercaseWithCommaAndOr(types.map { it.name })
        return when {
            activeCypMedications.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "CYP $type medication use (${concatLowercaseWithCommaAndAnd(activeCypMedications)})"
                )
            }

            plannedCypMedications.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Planned CYP $type medication (${concatLowercaseWithCommaAndAnd(plannedCypMedications)})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail("No CYP $type medication use")
            }
        }
    }
}