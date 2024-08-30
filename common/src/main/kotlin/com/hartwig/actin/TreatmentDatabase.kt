package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class TreatmentDatabase(private val drugsByName: Map<String, Drug>, private val treatmentsByName: Map<String, Treatment>) {

    fun findTreatmentByName(treatmentName: String): Treatment? {
        return treatmentsByName[treatmentName.replace(" ", "_").lowercase()]
    }

    fun findDrugByName(drugName: String): Drug? {
        return drugsByName[drugName.replace(" ", "_").lowercase()]
    }
}
