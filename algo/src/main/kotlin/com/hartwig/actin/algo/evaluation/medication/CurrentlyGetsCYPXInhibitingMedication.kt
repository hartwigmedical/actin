package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCYPXInhibitingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val cypInhibitorsReceived =
            Format.medicationsToNames(
                selector.activeWithCYPInteraction(
                    record.clinical().medications(),
                    termToFind,
                    CypInteraction.Type.INHIBITOR
                )
            )
        return if (cypInhibitorsReceived.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets CYP$termToFind inhibiting medication: ${Format.concatLowercaseWithAnd(cypInhibitorsReceived)}",
                "CYP$termToFind inhibiting medication use: ${Format.concatLowercaseWithAnd(cypInhibitorsReceived)}"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP$termToFind inhibiting medication ",
                "No CYP$termToFind inhibiting medication use "
            )
        }
    }
}