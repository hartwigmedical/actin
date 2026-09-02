package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadPDFollowingSpecificTreatment(private val treatments: List<Treatment>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentNamesToMatch = treatments.map { it.name.lowercase() }.toSet()
        val treatmentEvaluation = evaluateTreatmentHistory(record, treatmentNamesToMatch)

        return if (treatmentEvaluation.matchingTreatmentsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "PD after receiving ${concatItemsWithAnd(treatmentEvaluation.matchingTreatmentsWithPD)} treatment based on provided treatments"
            )
        } else if (treatmentEvaluation.includesTrial) {
            EvaluationFactory.undetermined(
                "Undetermined if trial treatment in provided treatments contained ${
                    concatItemsWithAnd(
                        treatments
                    )
                }"
            )
        } else if (treatmentEvaluation.matchesWithUnclearPD) {
            EvaluationFactory.undetermined(
                "${concatItemsWithAnd(treatmentEvaluation.matchingTreatments)} treatment in provided treatments but undetermined if PD"
            )
        } else if (treatmentEvaluation.matchingTreatments.isNotEmpty()) {
            EvaluationFactory.fail("${concatItemsWithAnd(treatmentEvaluation.matchingTreatments)} treatment in provided treatments but no PD")
        } else {
            EvaluationFactory.fail("No ${concatItemsWithAnd(treatments)} treatment in provided treatments")
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord, treatmentNamesToMatch: Set<String>): TreatmentHistoryEvaluation {
        val treatmentHistory = record.oncologicalHistory
        val treatmentCategoriesToMatch = treatments.flatMap { it.categories() }.filter(TrialFunctions::categoryAllowsTrialMatches).toSet()

        return treatmentHistory.map { entry ->
            val isPD = treatmentResultedInPD(entry, treatmentHistory)
            val treatmentsMatchingNames = treatmentsMatchingNameListExactly(entry.allTreatments(), treatmentNamesToMatch)
            val includesTrial = TrialFunctions.treatmentMayMatchAsTrial(entry, treatmentCategoriesToMatch)
            if (treatmentsMatchingNames.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingTreatmentsWithPD = if (isPD == true) treatmentsMatchingNames else emptySet(),
                    matchingTreatments = treatmentsMatchingNames,
                    matchesWithUnclearPD = isPD == null,
                    includesTrial = includesTrial
                )
            } else {
                TreatmentHistoryEvaluation(includesTrial = includesTrial)
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