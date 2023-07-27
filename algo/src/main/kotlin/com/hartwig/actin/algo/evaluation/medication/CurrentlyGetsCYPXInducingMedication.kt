package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.algo.evaluation.util.Format

class CurrentlyGetsCYPXInducingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receivedCYPXInducer =
            selector.activeWithCYPInteraction(record.clinical().medications(), termToFind, CypInteraction.Type.INDUCER).map { it.name() }
        return if (receivedCYPXInducer.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets CYP$termToFind inducing medication: " + Format.concatLowercaseWithAnd(receivedCYPXInducer),
                "CYP$termToFind inducing medication use: " + Format.concatLowercaseWithAnd(receivedCYPXInducer)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP$termToFind inducing medication ",
                "No CYP$termToFind inducing medication use "
            )
        }
    }
}