package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.createFullExpandedDoidTree
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.DoidModel

class TreatmentHistoryAnalyzer(private val record: PatientRecord, doidModel: DoidModel) {

    val isNsclc = LUNG_NON_SMALL_CELL_CARCINOMA_DOID in createFullExpandedDoidTree(doidModel, record.tumor.doids)

    fun receivedPlatinumDoublet() = hasSpecificPlatinumCombination(2, true)

    fun receivedPlatinumTripletOrAbove() = hasSpecificPlatinumCombination(3, false)

    fun receivedUndefinedChemoradiation(): Boolean {
        return record.oncologicalHistory.any {
            it.treatments.map(Treatment::name).containsAll(listOf("CHEMOTHERAPY", "RADIOTHERAPY"))
        }
    }

    private fun createChemotherapyDrugList(entry: TreatmentHistoryEntry): List<Drug> {
        return entry.treatments.filterIsInstance<DrugTreatment>()
            .flatMap(DrugTreatment::drugs)
            .filter { it.category == TreatmentCategory.CHEMOTHERAPY }
    }

    private fun hasSpecificPlatinumCombination(minSize: Int, exactly: Boolean): Boolean {
        val comparator = if (exactly) { it: Int -> it == minSize } else { it: Int -> it >= minSize }
        return record.oncologicalHistory.any { entry ->
            val chemotherapyDrugs = createChemotherapyDrugList(entry)
            comparator.invoke(chemotherapyDrugs.size) && chemotherapyDrugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) }
        }
    }
}
