package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCypXInhibitingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val cypInhibitorsReceived =
            selector.activeWithCypInteraction(
                record.clinical().medications(),
                termToFind,
                CypInteraction.Type.INHIBITOR
            ).map { it.name() }
        return if (cypInhibitorsReceived.isNotEmpty()) {
            EvaluationFactory.recoverablePass(
                "Patient currently gets CYP$termToFind inhibiting medication: ${Format.concatLowercaseWithAnd(cypInhibitorsReceived)}",
                "CYP$termToFind inhibiting medication use: ${Format.concatLowercaseWithAnd(cypInhibitorsReceived)}"
            )
        } else if (termToFind in MedicationRuleMapper.UNDETERMINED_CYP) {
            EvaluationFactory.undetermined(
                "Undetermined if patient currently gets CYP$termToFind inhibiting medication",
                "Undetermined CYP$termToFind inhibiting medication use"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP$termToFind inhibiting medication ",
                "No CYP$termToFind inhibiting medication use "
            )
        }
    }
}