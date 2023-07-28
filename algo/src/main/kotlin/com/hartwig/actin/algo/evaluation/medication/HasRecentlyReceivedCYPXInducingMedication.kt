package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction
import java.time.LocalDate

class HasRecentlyReceivedCYPXInducingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val minStopDate: LocalDate
) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val cypInducersReceived = Format.medicationsToNames(
            selector.activeOrRecentlyStoppedWithCYPInteraction(
                record.clinical().medications(),
                termToFind,
                CypInteraction.Type.INDUCER,
                minStopDate
            )
        )
        return if (cypInducersReceived.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has recently received CYP$termToFind inducing medication: ${Format.concatLowercaseWithAnd(cypInducersReceived)}",
                "Recent CYP$termToFind inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has not recently received CYP$termToFind inducing medication ",
                "No recent CYP$termToFind inducing medication use "
            )
        }
    }
}