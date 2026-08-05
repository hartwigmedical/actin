package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.treatmentSinceMinDate
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
    private val onlySystemicTreatments: Boolean,
    private val labels: EvaluationLabels.Treatment
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

        val systemicSuffix = if (onlySystemicTreatments) labels.hasHadAnyCancerTreatmentSinceDateSystemicSuffix() else ""

        val ignoringSuffix = if (typesToIgnore.isNotEmpty()) {
            labels.hasHadAnyCancerTreatmentSinceDateIgnoringSuffix(Format.concatItemsWithAnd(typesToIgnore))
        } else ""

        return when {
            effectiveTreatmentHistory.any { treatmentSinceMinDate(it, minDate, false) } -> {
                EvaluationFactory.pass(labels.hasHadAnyCancerTreatmentSinceDatePass(systemicSuffix, monthsAgo))
            }

            effectiveTreatmentHistory.any { it.isTrial } || record.medications?.any { it.isTrialMedication } == true -> {
                EvaluationFactory.undetermined(labels.hasHadAnyCancerTreatmentSinceDateUndeterminedTrial(systemicSuffix))
            }

            effectiveTreatmentHistory.any { treatmentSinceMinDate(it, minDate, true) } -> {
                EvaluationFactory.undetermined(
                    labels.hasHadAnyCancerTreatmentSinceDateUndeterminedDateUnknown(systemicSuffix, monthsAgo)
                )
            }

            effectiveTreatmentHistory.isEmpty() -> {
                EvaluationFactory.fail(
                    labels.hasHadAnyCancerTreatmentSinceDateFailNotReceived(systemicSuffix, monthsAgo, ignoringSuffix)
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadAnyCancerTreatmentSinceDateFailNoTreatment(systemicSuffix, ignoringSuffix))
            }
        }
    }
}
