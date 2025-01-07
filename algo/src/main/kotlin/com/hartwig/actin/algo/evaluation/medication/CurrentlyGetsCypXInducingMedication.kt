package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsCypXInducingMedication(private val selector: MedicationSelector, private val termToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypInducersReceived =
            selector.activeWithInteraction(medications, termToFind, DrugInteraction.Type.INDUCER, DrugInteraction.Group.CYP)
                .map { it.name }.toSet()

        val cypInducersPlanned =
            selector.plannedWithInteraction(medications, termToFind, DrugInteraction.Type.INDUCER, DrugInteraction.Group.CYP)
                .map { it.name }.toSet()

        return when {
            cypInducersReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "CYP$termToFind inducing medication use (${concatLowercaseWithCommaAndAnd(cypInducersReceived)})"
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(
                    "CYP$termToFind inducing medication use undetermined"
                )
            }

            cypInducersPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Planned CYP$termToFind inducing medication use (${concatLowercaseWithCommaAndAnd(cypInducersPlanned)})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "No CYP$termToFind inducing medication use "
                )
            }
        }
    }
}