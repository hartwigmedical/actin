package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.algo.evaluation.util.Format

class CurrentlyGetsAnyCypInhibitingOrInducingMedication(private val selector: MedicationSelector) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val activeMedications = selector.active(record.clinical().medications())
        val cypMedications = activeMedications.filter { medication ->
            medication.cypInteractions()
                .any { it.type() == CypInteraction.Type.INDUCER || it.type() == CypInteraction.Type.INHIBITOR }
        }.map { it.name() }

        return if (cypMedications.isNotEmpty()) {
            EvaluationFactory.recoverablePass(
                "Patient currently gets CYP inhibiting/inducing medication: ${Format.concatLowercaseWithAnd(cypMedications)}",
                "CYP inhibiting/inducing medication use: ${Format.concatLowercaseWithAnd(cypMedications)}"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP inhibiting/inducing medication ",
                "No CYP inhibiting/inducing medication use "
            )
        }
    }
}