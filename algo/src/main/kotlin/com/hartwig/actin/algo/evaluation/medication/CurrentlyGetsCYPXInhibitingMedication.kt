package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCYPXInhibitingMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedCYPXInhibitor = selector.activeWithCYPInteraction(
            record.clinical().medications(),
            termToFind,
            CypInteraction.Type.INHIBITOR
        ).isNotEmpty()
        return if (hasReceivedCYPXInhibitor) {
            EvaluationFactory.pass(
                "Patient currently gets $termToFind inhibiting medication",
                "$termToFind inhibiting medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get $termToFind inhibiting medication ",
                "No $termToFind inhibiting medication use "
            )
        }
    }
}