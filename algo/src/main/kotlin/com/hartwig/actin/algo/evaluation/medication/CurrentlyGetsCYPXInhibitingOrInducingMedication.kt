package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCYPXInhibitingOrInducingMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedCYPXInhibitor = selector.activeWithCYPInteraction(
            record.clinical().medications(),
            termToFind,
            CypInteraction.Type.INHIBITOR
        ).isNotEmpty()
        val hasReceivedCYPXInducer =
            selector.activeWithCYPInteraction(record.clinical().medications(), termToFind, CypInteraction.Type.INDUCER)
                .isNotEmpty()
        return if (hasReceivedCYPXInhibitor || hasReceivedCYPXInducer) {
            EvaluationFactory.pass(
                "Patient currently gets $termToFind inhibiting/inducing medication",
                "$termToFind inhibiting/inducing medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get $termToFind inhibiting/inducing medication ",
                "No $termToFind inhibiting/inducing medication use "
            )
        }
    }
}