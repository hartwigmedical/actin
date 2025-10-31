package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class TreatmentHistoryAnalysis(private val record: PatientRecord, private val platinumCombinations: Set<Int>) {

    fun receivedPlatinumDoublet() = platinumCombinations.contains(2)

    fun receivedPlatinumTripletOrAbove() = platinumCombinations.any { it >= 3 }

    fun receivedUndefinedChemoradiation(): Boolean = hasTreatments(setOf("CHEMOTHERAPY", "RADIOTHERAPY"))

    fun receivedUndefinedChemoImmunotherapy(): Boolean = hasTreatments(setOf("CHEMOTHERAPY+IMMUNOTHERAPY"))

    fun receivedUndefinedChemotherapy(): Boolean = hasTreatments(setOf("CHEMOTHERAPY"))

    private fun hasTreatments(treatmentNames: Set<String>): Boolean =
        record.oncologicalHistory.any { it.treatments.map(Treatment::name).containsAll(treatmentNames) }

    companion object {
        fun create(record: PatientRecord): TreatmentHistoryAnalysis {
            val platinumCombinations = record.oncologicalHistory.asSequence()
                .flatMap { it.allTreatments() }
                .filterIsInstance<DrugTreatment>()
                .filter { treatment -> treatment.drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) } }
                .map { it.drugs.count { drug -> drug.category == TreatmentCategory.CHEMOTHERAPY } }
                .toSet()
            return TreatmentHistoryAnalysis(record, platinumCombinations)
        }
    }
}
