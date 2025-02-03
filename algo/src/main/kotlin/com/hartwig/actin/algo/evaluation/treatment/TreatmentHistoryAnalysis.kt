package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class TreatmentHistoryAnalysis(private val record: PatientRecord, private val platinumCombinations: Set<Int>) {

    fun receivedPlatinumDoublet() = platinumCombinations.contains(2)

    fun receivedPlatinumTripletOrAbove() = platinumCombinations.any { it >= 3 }

    fun receivedUndefinedChemoradiation(): Boolean {
        return record.oncologicalHistory.any {
            it.treatments.map(Treatment::name).containsAll(listOf("CHEMOTHERAPY", "RADIOTHERAPY"))
        }
    }

    companion object {
        fun create(record: PatientRecord): TreatmentHistoryAnalysis {
            val platinumCombinations = record.oncologicalHistory.asSequence()
                .flatMap { it.allTreatments().toSet() }
                .filterIsInstance<DrugTreatment>()
                .map(DrugTreatment::drugs)
                .filter { drugs -> drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) } }
                .map { it.count { drug -> drug.category == TreatmentCategory.CHEMOTHERAPY } }
                .toSet()
            return TreatmentHistoryAnalysis(record, platinumCombinations)
        }
    }
}
