package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.containsTreatment
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadSystemicFirstLineTreatmentWithoutPdAndWithCycles(
    private val treatment: Treatment, private val minCycles: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNameToFind = treatment.name
        val systemic = record.oncologicalHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (treatmentsWithStartDate, treatmentsWithoutStartDate) = systemic.partition { it.startYear != null }
        val firstTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(treatmentsWithStartDate)
        val treatmentToFindWithUnknownStartDate = treatmentsWithoutStartDate.filter { it.containsTreatment(treatmentNameToFind) }
        val hasOnlyHadTargetTreatment = systemic.isNotEmpty() && systemic.all { it.containsTreatment(treatmentNameToFind) }
        val targetTreatment = systemic.firstOrNull { it.containsTreatment(treatmentNameToFind) }

        val evaluation = TreatmentEvaluation.create(
            hadTreatment = systemic.any { it.containsTreatment(treatmentNameToFind) },
            hadUnclearFirstLineTrialTreatment = firstTreatment?.let { TrialFunctions.treatmentMayMatchAsTrial(it, treatment.categories()) }
                ?: false,
            unclearTreatmentLine = treatmentToFindWithUnknownStartDate.isNotEmpty() && !hasOnlyHadTargetTreatment,
            isFirstLine = firstTreatment?.containsTreatment(treatmentNameToFind) == true || hasOnlyHadTargetTreatment,
            pdStatus = targetTreatment?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) },
            hasMinCycles = targetTreatment?.treatmentHistoryDetails?.cycles?.let { it >= minCycles }
        )

        val messageStartNegative = "Has not received ${treatment.display()}"
        val messageStartPositive = "Has received ${treatment.display()}"
        val asFirstLine = " as first-line treatment"
        val messageEnd = " without PD and with at least $minCycles cycles"

        return when (evaluation) {
            TreatmentEvaluation.FIRST_LINE_AND_MEETS_PD_AND_CYCLES -> {
                EvaluationFactory.pass(messageStartPositive + asFirstLine + messageEnd)
            }

            TreatmentEvaluation.UNDETERMINED_IF_FIRST_LINE -> {
                EvaluationFactory.undetermined("$messageStartPositive but undetermined if first line (dates missing)")
            }

            TreatmentEvaluation.UNDETERMINED_PD_STATUS -> {
                EvaluationFactory.undetermined("$messageStartPositive$asFirstLine but undetermined if without PD")
            }

            TreatmentEvaluation.UNDETERMINED_CYCLES -> {
                EvaluationFactory.undetermined("$messageStartPositive$asFirstLine but undetermined if with at least $minCycles cycles")
            }

            TreatmentEvaluation.HAS_HAD_UNCLEAR_TRIAL_TREATMENT -> {
                EvaluationFactory.undetermined(
                    "Undetermined if has received first line ${treatment.display()} treatment (first line is unknown trial treatment)"
                )
            }

            TreatmentEvaluation.DOES_NOT_MEET_CRITERIA -> EvaluationFactory.fail(messageStartNegative + asFirstLine + messageEnd)
        }
    }

    private enum class TreatmentEvaluation {
        HAS_HAD_UNCLEAR_TRIAL_TREATMENT,
        DOES_NOT_MEET_CRITERIA,
        UNDETERMINED_IF_FIRST_LINE,
        UNDETERMINED_PD_STATUS,
        UNDETERMINED_CYCLES,
        FIRST_LINE_AND_MEETS_PD_AND_CYCLES;

        companion object {
            fun create(
                hadTreatment: Boolean,
                hadUnclearFirstLineTrialTreatment: Boolean,
                unclearTreatmentLine: Boolean,
                isFirstLine: Boolean,
                pdStatus: Boolean?,
                hasMinCycles: Boolean?
            ) = when {
                !hadTreatment && hadUnclearFirstLineTrialTreatment -> HAS_HAD_UNCLEAR_TRIAL_TREATMENT
                !hadTreatment || (!isFirstLine && !unclearTreatmentLine) || pdStatus == true || hasMinCycles == false -> {
                    DOES_NOT_MEET_CRITERIA
                }
                unclearTreatmentLine -> UNDETERMINED_IF_FIRST_LINE
                pdStatus == null -> UNDETERMINED_PD_STATUS
                hasMinCycles == null -> UNDETERMINED_CYCLES
                else -> FIRST_LINE_AND_MEETS_PD_AND_CYCLES
            }
        }
    }
}