package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrug(private val drugs: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val namesToMatch = drugs.map { it.name.lowercase() }.toSet()
        val matchingDrugs = record.oncologicalHistory
            .flatMap(TreatmentHistoryEntry::allTreatments)
            .flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            .filter { it.name.lowercase() in namesToMatch }

        val drugList = concatItemsWithOr(drugs)
        return when {
            matchingDrugs.isNotEmpty() -> {
                EvaluationFactory.pass("Has received treatments with ${concatItemsWithAnd(matchingDrugs)}")
            }

            record.oncologicalHistory.any {
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