package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.algo.evaluation.util.Format

class CurrentlyGetsCYPXInhibitingOrInducingMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receivedCYPXInhibitor = selector.activeWithCYPInteraction(
            record.clinical().medications(),
            termToFind,
            CypInteraction.Type.INHIBITOR
        ).map { it.name() }
        val receivedCYPXInducer =
            selector.activeWithCYPInteraction(record.clinical().medications(), termToFind, CypInteraction.Type.INDUCER).map { it.name() }
        return if (receivedCYPXInhibitor.isNotEmpty() || receivedCYPXInducer.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets CYP$termToFind inhibiting/inducing medication: " + Format.concatLowercaseWithAnd(receivedCYPXInducer + receivedCYPXInhibitor),
                "CYP$termToFind inhibiting/inducing medication use: " + Format.concatLowercaseWithAnd(receivedCYPXInducer + receivedCYPXInhibitor)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP$termToFind inhibiting/inducing medication ",
                "No CYP$termToFind inhibiting/inducing medication use "
            )
        }
    }
}