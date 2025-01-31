package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class TreatmentHistoryAnalyzer(private val record: PatientRecord, private val chemotherapyDrugLists: List<List<Drug>>) {

    fun receivedPlatinumDoublet() = hasSpecificPlatinumCombination { it == 2 }

    fun receivedPlatinumTripletOrAbove() = hasSpecificPlatinumCombination { it >= 3 }

    fun receivedUndefinedChemoradiation(): Boolean {
        return record.oncologicalHistory.any {
            it.treatments.map(Treatment::name).containsAll(listOf("CHEMOTHERAPY", "RADIOTHERAPY"))
        }
    }

    private fun hasSpecificPlatinumCombination(predicate: (Int) -> Boolean): Boolean {
        return chemotherapyDrugLists.any { drugs ->
            predicate.invoke(drugs.size) && drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) }
        }
    }

    companion object {
        fun create(record: PatientRecord): TreatmentHistoryAnalyzer {
            val chemotherapyDrugLists = record.oncologicalHistory.map {
                it.treatments.filterIsInstance<DrugTreatment>().flatMap(DrugTreatment::drugs)
                    .filter { treatment -> treatment.category == TreatmentCategory.CHEMOTHERAPY }
            }
            return TreatmentHistoryAnalyzer(record, chemotherapyDrugLists)
        }
    }
}
