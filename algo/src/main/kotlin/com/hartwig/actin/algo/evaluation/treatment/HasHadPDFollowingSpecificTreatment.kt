package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadPDFollowingSpecificTreatment(private val treatments: List<Treatment>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNamesToMatch = treatments.map { it.name.lowercase() }.toSet()
        val treatmentEvaluation = evaluateTreatmentHistory(record, treatmentNamesToMatch)

        return if (treatmentEvaluation.matchingTreatmentsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "Has had PD after receiving ${Format.concatItems(treatmentEvaluation.matchingTreatmentsWithPD)} treatment"
            )
        } else if (treatmentEvaluation.includesTrial) {
            EvaluationFactory.undetermined("Undetermined if received ${Format.concatItems(treatments)} treatment in trial")
        } else if (treatmentEvaluation.matchesWithUnclearPD) {
            EvaluationFactory.undetermined(
                "Has received ${Format.concatItems(treatmentEvaluation.matchingTreatments)} treatment but undetermined if PD"
            )
        } else if (treatmentEvaluation.matchingTreatments.isNotEmpty()) {
            EvaluationFactory.fail("Has received ${Format.concatItems(treatmentEvaluation.matchingTreatments)} treatment, but no PD")
        } else {
            EvaluationFactory.fail("Has not received ${Format.concatItems(treatments)} treatment")
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord, treatmentNamesToMatch: Set<String>): TreatmentHistoryEvaluation {
        val treatmentHistory = record.oncologicalHistory

        return treatmentHistory.map { entry ->
            val isPD = treatmentResultedInPD(entry)
            val treatmentsMatchingNames = treatmentsMatchingNameListExactly(entry.allTreatments(), treatmentNamesToMatch)
            if (treatmentsMatchingNames.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingTreatmentsWithPD = if (isPD == true) treatmentsMatchingNames else emptySet(),
                    matchingTreatments = treatmentsMatchingNames,
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
        private fun treatmentsMatchingNameListExactly(treatments: Set<Treatment>, treatmentNamesToMatch: Set<String>): Set<Treatment> {
            return treatments.filter { it.name.lowercase() in treatmentNamesToMatch }.toSet()
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchingTreatmentsWithPD: Set<Treatment> = emptySet(),
        val matchingTreatments: Set<Treatment> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val includesTrial: Boolean = false
    )
}