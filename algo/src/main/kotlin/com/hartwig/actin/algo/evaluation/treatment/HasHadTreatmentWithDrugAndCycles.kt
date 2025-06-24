package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment

class HasHadTreatmentWithDrugAndCycles(private val drugsToFind: Set<Drug>, private val minCycles: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)
        val namesToMatch = drugsToFind.map { it.name.lowercase() }.toSet()
        val drugList = concatItemsWithOr(drugsToFind)

        val drugsByEvaluationResult: Map<EvaluationResult, Set<Drug>> = effectiveTreatmentHistory
            .mapNotNull { entry ->
                TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry) { treatment ->
                    (treatment as? DrugTreatment)?.drugs?.any { it.name.lowercase() in namesToMatch } == true
                }?.let { matchingEntry ->
                    val hasCycles = matchingEntry.treatmentHistoryDetails?.cycles.let { cycles ->
                        when {
                            minCycles == null -> EvaluationResult.PASS
                            cycles == null -> EvaluationResult.UNDETERMINED
                            cycles >= minCycles -> EvaluationResult.PASS
                            else -> EvaluationResult.FAIL
                        }
                    }

                    val matchingDrugs = matchingEntry.allTreatments()
                        .mapNotNull { it as? DrugTreatment }
                        .flatMap { it.drugs }
                        .filter { it.name.lowercase() in namesToMatch }
                    hasCycles to matchingDrugs
                }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry -> entry.value.flatten().toSet() }

        val (drugsMatchingCycles, drugsWithUnknownCycles, drugsNotMatchingCycles) =
            listOf(EvaluationResult.PASS, EvaluationResult.UNDETERMINED, EvaluationResult.FAIL).map(drugsByEvaluationResult::get)

        return when {
            drugsMatchingCycles != null -> {
                val cyclesString = minCycles?.let { " for at least $minCycles cycles" } ?: ""
                EvaluationFactory.pass("Has received treatments with ${concatItemsWithAnd(drugsMatchingCycles)}$cyclesString")
            }

            drugsWithUnknownCycles != null -> {
                EvaluationFactory.undetermined(
                    "Has received treatments with ${concatItemsWithAnd(drugsWithUnknownCycles)} " +
                            "but undetermined if at least $minCycles cycles"
                )
            }

            effectiveTreatmentHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, drugsToFind.map(Drug::category)) } -> {
                EvaluationFactory.undetermined("Undetermined if received any treatments containing $drugList")
            }

            drugsNotMatchingCycles != null -> {
                EvaluationFactory.fail(
                    "Has received treatments with ${concatItemsWithAnd(drugsNotMatchingCycles)} " +
                            "but not at least $minCycles cycles"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not received any treatments containing $drugList")
            }
        }
    }
}