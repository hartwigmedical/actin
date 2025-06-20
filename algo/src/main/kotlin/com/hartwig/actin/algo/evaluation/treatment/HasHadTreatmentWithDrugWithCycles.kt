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
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrugWithCycles(private val drugsToFind: Set<Drug>, private val minCycles: Int) : EvaluationFunction {
// refactoren met HasHadTreatmentWithDrug

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)

//            .groupBy {
//                when (it.treatmentHistoryDetails?.cycles) {
//                    null -> EvaluationResult.UNDETERMINED
//                    >= minCycles -> EvaluationResult.PASS
//                    else -> EvaluationResult.FAIL
//                }
//            }

        val namesToMatch = drugsToFind.map { it.name.lowercase() }.toSet()
        val matchingDrugs = effectiveTreatmentHistory
            .flatMap(TreatmentHistoryEntry::allTreatments)
            .flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            .filter { it.name.lowercase() in namesToMatch }.toSet()

        val drugList = concatItemsWithOr(drugsToFind)

        // matching drug + pass cycles -> PASS
        // matching drug + undetermined cycles -> UNDETERMINED
        // trial -> UNDETERMINED
        // matching drug + fail cycles -> FAIL
        // no matching drugs + maakt niet uit -> FAIL

        return when {
            matchingDrugs.isNotEmpty() -> {
                EvaluationFactory.pass("Has received treatments with ${concatItemsWithAnd(matchingDrugs)}")
            }

            effectiveTreatmentHistory.any {
                it.isTrial && it.allTreatments().any { treatment ->
                    (treatment as? DrugTreatment)?.drugs?.isEmpty() ?: treatment.categories().isEmpty()
                }
            } -> {
                EvaluationFactory.undetermined("Undetermined if received any treatments containing $drugList")
            }

            else -> {
                EvaluationFactory.fail("Has not received any treatments containing $drugList")
            }
        }
    }
}