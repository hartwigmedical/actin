package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPDOption
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Treatment

class HasHadPDFollowingSpecificTreatment(private val treatments: List<Treatment>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNamesToMatch = treatments.map { it.name().lowercase() }.toSet()
        val treatmentEvaluation = evaluateTreatmentHistory(record, treatmentNamesToMatch)

        return if (treatmentEvaluation.matchingTreatmentsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "Has received ${Format.concat(treatmentEvaluation.matchingTreatmentsWithPD.map(Treatment::name))} treatment with PD"
            )
        } else if (treatmentEvaluation.includesTrial) {
            EvaluationFactory.undetermined("Undetermined if received specific ${Format.concat(treatmentNamesToMatch)} treatment")
        } else if (treatmentEvaluation.matchesWithUnclearPD) {
            EvaluationFactory.undetermined(
                "Has received ${Format.concat(treatmentEvaluation.matchingTreatments.map(Treatment::name))} treatment but undetermined if PD"
            )
        } else if (treatmentEvaluation.matchingTreatments.isNotEmpty()) {
            EvaluationFactory.fail("Has received ${Format.concat(treatmentEvaluation.matchingTreatments.map(Treatment::name))} treatment, but no PD")
        } else {
            EvaluationFactory.fail("Has not received specific ${Format.concat(treatmentNamesToMatch)} treatment")
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord, treatmentNamesToMatch: Set<String>): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical().treatmentHistory()

        return treatmentHistory.map { entry ->
            val isPD = treatmentResultedInPDOption(entry)
            if (treatmentsMatchNameListExactly(entry.treatments(), treatmentNamesToMatch)) {
                TreatmentHistoryEvaluation(
                    matchingTreatmentsWithPD = if (isPD == true) entry.treatments() else emptySet(),
                    matchingTreatments = entry.treatments(),
                    matchesWithUnclearPD = isPD == null,
                    includesTrial = entry.isTrial
                )
            } else {
                TreatmentHistoryEvaluation(includesTrial = entry.isTrial)
            }
        }.fold(TreatmentHistoryEvaluation()) { acc, result ->
            TreatmentHistoryEvaluation(
                matchingTreatmentsWithPD = acc.matchingTreatmentsWithPD + result.matchingTreatmentsWithPD,
                matchingTreatments = acc.matchingTreatments + result.matchingTreatments,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                includesTrial = acc.includesTrial || result.includesTrial
            )
        }
    }

    companion object {
        private fun treatmentsMatchNameListExactly(treatments: Set<Treatment>, treatmentNamesToMatch: Set<String>): Boolean {
            return treatments.map { it.name().lowercase() }.intersect(treatmentNamesToMatch).isNotEmpty()
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchingTreatmentsWithPD: Set<Treatment> = emptySet(),
        val matchingTreatments: Set<Treatment> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val includesTrial: Boolean = false
    )
}