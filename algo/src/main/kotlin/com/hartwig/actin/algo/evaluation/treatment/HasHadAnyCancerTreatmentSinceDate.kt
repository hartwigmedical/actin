package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions.treatmentSinceMinDate
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication
import java.time.LocalDate

class HasHadAnyCancerTreatmentSinceDate(
    private val minDate: LocalDate,
    private val monthsAgo: Int,
    private val atcLevelsToFind: Set<AtcLevel>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorCancerTreatment = record.oncologicalHistory

        val activePriorCancerMedication = record.medications
            ?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            ?.filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() } ?: emptyList()

        val foundTrialMedication = record.medications?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            ?.any(Medication::isTrialMedication) ?: false

        return when {
            priorCancerTreatment.any { treatmentSinceMinDate(it, minDate, false) } || activePriorCancerMedication.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has had anti-cancer therapy within the last $monthsAgo months",
                    "Received anti-cancer therapy within the last $monthsAgo months"
                )
            }

            foundTrialMedication -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive if patient has had any cancer treatment",
                    "Inconclusive if patient had any prior cancer treatment due to trial participation"
                )
            }

            priorCancerTreatment.any { treatmentSinceMinDate(it, minDate, true) } -> {
                EvaluationFactory.undetermined(
                    "Patient has had anti-cancer therapy but undetermined if in the last $monthsAgo months (date unknown)",
                    "Received anti-cancer therapy but undetermined if in the last $monthsAgo months (date unknown)"
                )
            }

            priorCancerTreatment.isEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has not received anti-cancer therapy within $monthsAgo months",
                    "Has not received anti-cancer therapy within $monthsAgo months"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not had any prior cancer treatments",
                    "Has not had any cancer treatment"
                )
            }
        }
    }
}
