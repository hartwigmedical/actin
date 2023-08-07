package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction
import java.time.LocalDate

class HasRecentlyReceivedCypXInducingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val minStopDate: LocalDate
) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val cypInducersReceived = selector.activeOrRecentlyStoppedWithCypInteraction(
            record.clinical().medications(),
            termToFind,
            CypInteraction.Type.INDUCER,
            minStopDate
        ).map { it.name() }

        return if (cypInducersReceived.isNotEmpty()) {
            EvaluationFactory.recoverablePass(
                "Patient has recently received CYP$termToFind inducing medication: ${Format.concatLowercaseWithAnd(cypInducersReceived)}",
                "Recent CYP$termToFind inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}"
            )
        } else if (termToFind in MedicationRuleMapper.UNDETERMINED_CYP) {
            EvaluationFactory.undetermined(
                "Undetermined if patient has recently received CYP$termToFind inducing medication",
                "Undetermined CYP$termToFind inducing medication use"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has not recently received CYP$termToFind inducing medication ",
                "No recent CYP$termToFind inducing medication use "
            )
        }
    }
}