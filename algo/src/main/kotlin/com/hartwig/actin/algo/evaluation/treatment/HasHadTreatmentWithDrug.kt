package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrug(private val drugsToFind: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val namesToMatch = drugsToFind.map { it.name.lowercase() }.toSet()
        val matchingDrugsInTreatment = record.oncologicalHistory
            .flatMap(TreatmentHistoryEntry::allTreatments)
            .flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            .filter { it.name.lowercase() in namesToMatch }.toSet()

        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val matchingDrugsInMedication =
            medications.filter { it.drug?.name?.lowercase() in namesToMatch }.mapNotNull(Medication::drug).toSet()

        val matchingDrugs = matchingDrugsInTreatment + matchingDrugsInMedication

        val drugList = concatItemsWithOr(drugsToFind)
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