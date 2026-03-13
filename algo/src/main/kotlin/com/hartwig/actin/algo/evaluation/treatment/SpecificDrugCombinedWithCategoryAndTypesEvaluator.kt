package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class SpecificDrugCombinedWithCategoryAndTypesEvaluator(
    val drugToFind: Drug,
    val category: TreatmentCategory,
    val types: Set<TreatmentType>?
) {

    fun relevantHistory(record: PatientRecord) = record.oncologicalHistory.filter { history ->
        history.allTreatments().any { (it as? DrugTreatment)?.drugs?.contains(drugToFind) == true }
    }

    fun treatmentWithoutDrugMatchesCategoryAndType(pastTreatment: Treatment): Boolean {
        val treatmentWithoutDrug = (pastTreatment as? DrugTreatment)?.let { it.copy(drugs = it.drugs - drugToFind) }
            ?: pastTreatment
        return treatmentWithoutDrug.categories().contains(category) && treatmentWithoutDrug.types().containsAll(types ?: emptySet())
    }

    fun treatmentString() =
        "combined therapy with ${drugToFind.display()} and ${types?.let { concatItemsWithAnd(types) } ?: ""} ${category.display()}"
}