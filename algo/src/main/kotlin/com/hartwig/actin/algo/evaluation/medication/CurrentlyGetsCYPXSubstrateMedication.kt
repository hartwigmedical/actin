package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCYPXSubstrateMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedCYPXSubstrate = selector.activeWithCYPInteraction(
            record.clinical().medications(),
            termToFind,
            CypInteraction.Type.SUBSTRATE
        ).isNotEmpty()
        return if (hasReceivedCYPXSubstrate) {
            EvaluationFactory.pass(
                "Patient currently gets $termToFind substrate medication",
                "$termToFind substrate medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get $termToFind substrate medication ",
                "No $termToFind substrate medication use "
            )
        }
    }
}