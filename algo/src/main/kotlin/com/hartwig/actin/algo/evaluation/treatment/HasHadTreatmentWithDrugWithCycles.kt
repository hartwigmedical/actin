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

class HasHadTreatmentWithDrugWithCycles(private val drugsToFind: Set<Drug>, private val minCycles: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)
        val namesToMatch = drugsToFind.map { it.name.lowercase() }.toSet()
        val drugList = concatItemsWithOr(drugsToFind)

        val drugsByEvaluationResult: Map<EvaluationResult, Set<Drug>> = effectiveTreatmentHistory
            .flatMap { entry ->
                val result = if (minCycles == null) EvaluationResult.PASS else when (entry.treatmentHistoryDetails?.cycles) {
                    null -> EvaluationResult.UNDETERMINED
                    in minCycles..Int.MAX_VALUE -> EvaluationResult.PASS
                    else -> EvaluationResult.FAIL
                }
                entry.allTreatments()
                    .mapNotNull { it as? DrugTreatment }
                    .flatMap { it.drugs }
                    .filter { it.name.lowercase() in namesToMatch }
                    .map { result to it }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.toSet() }

        return when {
            drugsByEvaluationResult[EvaluationResult.PASS]?.isNotEmpty() == true -> {
                val matchingDrugs = drugsByEvaluationResult[EvaluationResult.PASS]!!
                val cyclesString = minCycles?.let { " for at least $minCycles cycles" } ?: ""
                EvaluationFactory.pass("Has received treatments with ${concatItemsWithAnd(matchingDrugs)}$cyclesString")
            }

            drugsByEvaluationResult[EvaluationResult.UNDETERMINED]?.isNotEmpty() == true -> {
                val matchingDrugs = drugsByEvaluationResult[EvaluationResult.UNDETERMINED]!!
                EvaluationFactory.undetermined("Has received treatments with ${concatItemsWithAnd(matchingDrugs)} but undetermined if at least $minCycles cycles")
            }

            effectiveTreatmentHistory.any {
                it.isTrial && it.allTreatments().any { treatment ->
                    (treatment as? DrugTreatment)?.drugs?.isEmpty() ?: treatment.categories().isEmpty()
                }
            } -> {
                EvaluationFactory.undetermined("Undetermined if received any treatments containing $drugList")
            }

            drugsByEvaluationResult[EvaluationResult.FAIL]?.isNotEmpty() == true -> {
                val matchingDrugs = drugsByEvaluationResult[EvaluationResult.FAIL]!!
                EvaluationFactory.fail("Has received treatments with ${concatItemsWithAnd(matchingDrugs)} but not at least $minCycles cycles")
            }

            else -> {
                EvaluationFactory.fail("Has not received any treatments containing $drugList")
            }
        }
    }
}