package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class TreatmentDatabase(val drugsByName: Map<String, Drug>, val treatmentsByName: Map<String, Treatment>) {

    fun drugs() = drugsByName.values.toSet()

    fun treatments() = treatmentsByName.values.toSet()

    fun findTreatmentByName(treatmentName: String): Treatment? {
        return treatmentsByName[treatmentName.replace(" ", "_").lowercase()]
    }

    fun findDrugByName(drugName: String): Drug? {
        return drugsByName[drugName.replace(" ", "_").lowercase()]
    }

    fun findDrugByAtcName(atcName: String): Drug? {
        return findDrugByName(atcName.lowercase().replace(" and ", "_").split(", ").first())
    }

    fun findDrugTreatmentByDrugs(drugs: Set<Drug>): Treatment? {
        return treatments().filterIsInstance<DrugTreatment>().firstOrNull { it.drugs == drugs }
    }
}
