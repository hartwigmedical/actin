package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment

class TreatmentDatabase(private val drugsByName: Map<String, Drug>, private val treatmentsByName: Map<String, Treatment>) {

    fun findTreatmentByName(treatmentName: String): Treatment? {
        return treatmentsByName[treatmentName.replace(" ", "_").lowercase()]
    }

    fun findDrugByName(drugName: String): Drug? {
        return drugsByName[drugName.replace(" ", "_").lowercase()]
    }
}
