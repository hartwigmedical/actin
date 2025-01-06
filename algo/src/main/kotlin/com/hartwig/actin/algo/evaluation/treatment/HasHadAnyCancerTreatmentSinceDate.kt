package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions.treatmentSinceMinDate
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import java.time.LocalDate

class HasHadAnyCancerTreatmentSinceDate(
    private val minDate: LocalDate,
    private val monthsAgo: Int,
    private val atcLevelsToFind: Set<AtcLevel>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val antiCancerMedicationsWithoutTrialMedicationsAsTreatments =
            createTreatmentHistoryEntriesFromMedications(record.medications?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
                ?.filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() })

        val effectiveTreatmentHistory = record.oncologicalHistory + antiCancerMedicationsWithoutTrialMedicationsAsTreatments

        return when {
            effectiveTreatmentHistory.any { treatmentSinceMinDate(it, minDate, false) } -> {
                EvaluationFactory.pass("Received anti-cancer therapy within the last $monthsAgo months")
            }

            effectiveTreatmentHistory.any { it.isTrial } || record.medications?.any { it.isTrialMedication } == true -> {
                EvaluationFactory.undetermined("Inconclusive if patient had any prior cancer treatment due to trial participation")
            }

            effectiveTreatmentHistory.any { treatmentSinceMinDate(it, minDate, true) } -> {
                EvaluationFactory.undetermined("Received anti-cancer therapy but undetermined if in the last $monthsAgo months (date unknown)")
            }

            effectiveTreatmentHistory.isEmpty() -> {
                EvaluationFactory.fail("Has not received anti-cancer therapy within $monthsAgo months")
            }

            else -> {
                EvaluationFactory.fail("Has not had any prior cancer treatment")
            }
        }
    }
}
