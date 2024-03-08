package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCypXInhibitingOrInducingMedication(
    private val selector: MedicationSelector, private val termToFind: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cypMedications = record.medications.filter { medication ->
            medication.cypInteractions
                .any { it.cyp == termToFind && (it.type == CypInteraction.Type.INDUCER || it.type == CypInteraction.Type.INHIBITOR) }
        }

        val activeCypMedications = cypMedications.filter { selector.isActive(it) }.map { it.name }
        val plannedCypMedications = cypMedications.filter { selector.isPlanned(it) }.map { it.name }

        return when {
            activeCypMedications.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets CYP$termToFind inhibiting/inducing medication: ${
                        Format.concatLowercaseWithAnd(
                            activeCypMedications
                        )
                    }",
                    "CYP$termToFind inhibiting/inducing medication use: ${Format.concatLowercaseWithAnd(activeCypMedications)}"
                )
            }

            plannedCypMedications.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get CYP$termToFind inhibiting/inducing medication: ${
                        Format.concatLowercaseWithAnd(
                            plannedCypMedications
                        )
                    }",
                    "Planned CYP$termToFind inhibiting/inducing medication use: ${Format.concatLowercaseWithAnd(plannedCypMedications)}"
                )
            }

            termToFind in MedicationRuleMapper.UNDETERMINED_CYP -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient currently gets CYP$termToFind inhibiting/inducing medication",
                    "Undetermined CYP$termToFind inhibiting/inducing medication use"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get CYP$termToFind inhibiting/inducing medication ",
                    "No CYP$termToFind inhibiting/inducing medication use "
                )
            }
        }
    }
}