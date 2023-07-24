package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.algo.evaluation.util.Format

class CurrentlyGetsCYPXSubstrateMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receivedCYPXSubstrate = selector.activeWithCYPInteraction(
            record.clinical().medications(),
            termToFind,
            CypInteraction.Type.SUBSTRATE
        ).map { it.name() }
        return if (receivedCYPXSubstrate.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets CYP$termToFind substrate medication: " + Format.concatLowercaseWithAnd(receivedCYPXSubstrate),
                "CYP$termToFind substrate medication use: " + Format.concatLowercaseWithAnd(receivedCYPXSubstrate)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP$termToFind substrate medication ",
                "No CYP$termToFind substrate medication use "
            )
        }
    }
}