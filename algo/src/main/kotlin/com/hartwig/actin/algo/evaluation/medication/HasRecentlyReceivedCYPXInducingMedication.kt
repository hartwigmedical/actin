package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction
import java.time.LocalDate

class HasRecentlyReceivedCYPXInducingMedication internal constructor(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val minStopDate: LocalDate
) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedCYPXInducer = selector.activeOrRecentlyStoppedWithCYPInteraction(
            record.clinical().medications(),
            "Any",
            CypInteraction.Type.INDUCER,
            minStopDate
        ).isNotEmpty()
        return if (hasReceivedCYPXInducer) {
            EvaluationFactory.pass(
                "Patient has recently received $termToFind inducing medication",
                "Recent $termToFind inducing medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has not recently received $termToFind inducing medication ",
                "No recent $termToFind inducing medication use "
            )
        }
    }
}