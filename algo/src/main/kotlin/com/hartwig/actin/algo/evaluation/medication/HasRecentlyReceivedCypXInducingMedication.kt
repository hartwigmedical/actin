package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import java.time.LocalDate

class HasRecentlyReceivedCypXInducingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val minStopDate: LocalDate,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val cypInducersReceived = selector.activeOrRecentlyStoppedWithCypInteraction(
            medications, termToFind, DrugInteraction.Type.INDUCER, minStopDate
        ).map { it.name }.toSet()

        return when {
            cypInducersReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.hasRecentlyReceivedCypXInducingMedicationRecoverablePass(
                        termToFind, Format.concatLowercaseWithCommaAndAnd(cypInducersReceived)
                    )
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(labels.hasRecentlyReceivedCypXInducingMedicationUndetermined(termToFind))
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.hasRecentlyReceivedCypXInducingMedicationRecoverableFail(termToFind))
            }
        }
    }
}