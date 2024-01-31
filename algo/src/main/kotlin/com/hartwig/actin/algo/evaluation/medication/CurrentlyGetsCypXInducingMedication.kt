package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCypXInducingMedication(private val selector: MedicationSelector, private val termToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cypInducersReceived = selector.activeWithCypInteraction(record.clinical.medications, termToFind, CypInteraction.Type.INDUCER)
            .map { it.name }

        val cypInducersPlanned = selector.plannedWithCypInteraction(record.clinical.medications, termToFind, CypInteraction.Type.INDUCER)
            .map { it.name }

        return when {
            cypInducersReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets CYP$termToFind inducing medication: ${Format.concatLowercaseWithAnd(cypInducersReceived)}",
                    "CYP$termToFind inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}"
                )
            }

            termToFind in MedicationRuleMapper.UNDETERMINED_CYP -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient currently gets CYP$termToFind inducing medication",
                    "Undetermined CYP$termToFind inducing medication use"
                )
            }

            cypInducersPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get CYP$termToFind inducing medication: ${Format.concatLowercaseWithAnd(cypInducersPlanned)}",
                    "Planned CYP$termToFind inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersPlanned)}"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get CYP$termToFind inducing medication ",
                    "No CYP$termToFind inducing medication use "
                )
            }
        }
    }
}