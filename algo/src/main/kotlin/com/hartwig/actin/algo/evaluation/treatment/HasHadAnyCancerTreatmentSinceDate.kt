package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.certainTreatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.medication.MedicationToTreatmentConverter
import java.time.LocalDate

class HasHadAnyCancerTreatmentSinceDate(
    private val minDate: LocalDate,
    private val monthsAgo: Int,
    private val atcLevelsToFind: Set<AtcLevel>,
    private val interpreter: MedicationStatusInterpreter,
    private val categoryToIgnore: TreatmentCategory?,
    private val typesToIgnore: Set<TreatmentType>,
    private val onlySystemicTreatments: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val antiCancerMedicationsWithoutTrialMedicationsAsTreatments =
            MedicationToTreatmentConverter.convertAndCombine(
                record.medications?.filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
                    ?.filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() },
                record.oncologicalHistory
            )

        val effectiveTreatmentHistory = antiCancerMedicationsWithoutTrialMedicationsAsTreatments
            .filter { entry ->
                val treatments = entry.allTreatments().filterNot { treatment ->
                    treatment.categories().contains(categoryToIgnore) && treatment.types().any { it in typesToIgnore }
                }
                (!onlySystemicTreatments && treatments.isNotEmpty()) || treatments.any { it.isSystemic }
            }

        val systemicMessage = if (onlySystemicTreatments) " systemic" else ""
        val systemicPrefix = if (onlySystemicTreatments) "systemic " else ""

        val ignoringString = if (typesToIgnore.isNotEmpty()) " ignoring ${Format.concatItemsWithAnd(typesToIgnore)}" else ""

        return when {
            effectiveTreatmentHistory.any { certainTreatmentSinceMinDate(it, minDate) } -> {
                EvaluationFactory.pass("${systemicPrefix}anti-cancer therapy within the last $monthsAgo months in provided treatments")
            }

            effectiveTreatmentHistory.any { it.isTrial } || record.medications?.any { it.isTrialMedication } == true -> {
                EvaluationFactory.undetermined("Inconclusive if there was any prior ${systemicPrefix}cancer treatment because of trial participation")
            }

            effectiveTreatmentHistory.any { potentialTreatmentSinceMinDate(it, minDate) } -> {
                EvaluationFactory.undetermined("${systemicPrefix}anti-cancer therapy in provided treatments but undetermined if in the last $monthsAgo months (date unknown)")
            }

            effectiveTreatmentHistory.isEmpty() -> {
                EvaluationFactory.fail("No ${systemicPrefix}anti-cancer therapy within $monthsAgo months$ignoringString in provided treatments")
            }

            else -> EvaluationFactory.fail("No prior$systemicMessage cancer treatment$ignoringString in provided treatments")
        }
    }
}
