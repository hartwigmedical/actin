package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.containsTreatment
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadSystemicFirstLineTreatmentWithoutPdAndWithCycles(
    private val treatment: Treatment, private val minCycles: Int, private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNameToFind = treatment.name
        val systemic = record.oncologicalHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (treatmentsWithStartDate, treatmentsWithoutStartDate) = systemic.partition { it.startYear != null }
        val firstTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(treatmentsWithStartDate)
        val treatmentToFindWithUnknownStartDate = treatmentsWithoutStartDate.filter { it.containsTreatment(treatmentNameToFind) }
        val hasOnlyHadTargetTreatment = systemic.isNotEmpty() && systemic.all { it.containsTreatment(treatmentNameToFind) }
        val targetTreatment = systemic.firstOrNull { it.containsTreatment(treatmentNameToFind) }
        val isFirstLine = if (treatmentToFindWithUnknownStartDate.isNotEmpty() && !hasOnlyHadTargetTreatment) {
            null
        } else {
            hasOnlyHadTargetTreatment || firstTreatment?.containsTreatment(treatmentNameToFind) == true
        }

        val evaluation = TreatmentEvaluation.create(
            hadUnclearFirstLineTrialTreatment = firstTreatment?.let { TrialFunctions.treatmentMayMatchAsTrial(it, treatment.categories()) }
                ?: false,
            isFirstLine = isFirstLine,
            hasPd = targetTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) },
            hasMinCycles = targetTreatment?.treatmentHistoryDetails?.cycles?.let { it >= minCycles }
        )

        val treatmentDisplay = treatment.display()

        return when (evaluation) {
            TreatmentEvaluation.FIRST_LINE_WITHOUT_PD_AND_SUFFICIENT_CYCLES -> {
                EvaluationFactory.pass(labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesPass(treatmentDisplay, minCycles))
            }

            TreatmentEvaluation.UNDETERMINED_IF_FIRST_LINE -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedDatesMissing(treatmentDisplay)
                )
            }

            TreatmentEvaluation.UNDETERMINED_PD_STATUS -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedPdStatus(treatmentDisplay)
                )
            }

            TreatmentEvaluation.UNDETERMINED_CYCLES -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedCycles(treatmentDisplay, minCycles)
                )
            }

            TreatmentEvaluation.HAS_HAD_UNCLEAR_TRIAL_TREATMENT -> {
                EvaluationFactory.undetermined(
                    labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedTrial(treatmentDisplay)
                )
            }

            TreatmentEvaluation.FIRST_LINE_WITHOUT_PD_AND_INSUFFICIENT_CYCLES -> {
                EvaluationFactory.warn(
                    labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesWarnInsufficientCycles(treatmentDisplay, minCycles)
                )
            }

            TreatmentEvaluation.DOES_NOT_MEET_CRITERIA ->
                EvaluationFactory.fail(labels.hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesFail(treatmentDisplay, minCycles))
        }
    }

    private enum class TreatmentEvaluation {
        HAS_HAD_UNCLEAR_TRIAL_TREATMENT,
        DOES_NOT_MEET_CRITERIA,
        UNDETERMINED_IF_FIRST_LINE,
        UNDETERMINED_PD_STATUS,
        UNDETERMINED_CYCLES,
        FIRST_LINE_WITHOUT_PD_AND_SUFFICIENT_CYCLES,
        FIRST_LINE_WITHOUT_PD_AND_INSUFFICIENT_CYCLES;

        companion object {
            fun create(
                hadUnclearFirstLineTrialTreatment: Boolean,
                isFirstLine: Boolean?,
                hasPd: Boolean?,
                hasMinCycles: Boolean?
            ) = when {
                hadUnclearFirstLineTrialTreatment -> HAS_HAD_UNCLEAR_TRIAL_TREATMENT
                isFirstLine == false || hasPd == true -> DOES_NOT_MEET_CRITERIA
                isFirstLine == null -> UNDETERMINED_IF_FIRST_LINE
                hasPd == null -> UNDETERMINED_PD_STATUS
                hasMinCycles == null -> UNDETERMINED_CYCLES
                !hasMinCycles -> FIRST_LINE_WITHOUT_PD_AND_INSUFFICIENT_CYCLES
                else -> FIRST_LINE_WITHOUT_PD_AND_SUFFICIENT_CYCLES
            }
        }
    }
}