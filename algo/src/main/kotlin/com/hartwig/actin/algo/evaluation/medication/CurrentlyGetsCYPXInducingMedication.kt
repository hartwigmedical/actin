package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCYPXInducingMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedCYPXInducer =
            selector.activeWithCYPInteraction(record.clinical().medications(), termToFind, CypInteraction.Type.INDUCER)
                .isNotEmpty()
        return if (hasReceivedCYPXInducer) {
            EvaluationFactory.pass(
                "Patient currently gets $termToFind inducing medication",
                "$termToFind inducing medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get $termToFind inducing medication ",
                "No $termToFind inducing medication use "
            )
        }
    }
}