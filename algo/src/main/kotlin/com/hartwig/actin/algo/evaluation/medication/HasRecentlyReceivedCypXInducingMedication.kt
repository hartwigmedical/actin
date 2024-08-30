package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.CypInteraction
import java.time.LocalDate

class HasRecentlyReceivedCypXInducingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val minStopDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypInducersReceived = selector.activeOrRecentlyStoppedWithCypInteraction(
            medications, termToFind, CypInteraction.Type.INDUCER, minStopDate
        ).map { it.name }.toSet()

        return when {
            cypInducersReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient has recently received CYP$termToFind inducing medication: ${
                        Format.concatLowercaseWithAnd(
                            cypInducersReceived
                        )
                    }",
                    "Recent CYP$termToFind inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}"
                )
            }

            termToFind in MedicationRuleMapper.UNDETERMINED_CYP -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has recently received CYP$termToFind inducing medication",
                    "Undetermined CYP$termToFind inducing medication use"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has not recently received CYP$termToFind inducing medication ",
                    "No recent CYP$termToFind inducing medication use "
                )
            }
        }
    }
}