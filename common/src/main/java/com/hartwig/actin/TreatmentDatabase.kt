package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import java.util.*

class TreatmentDatabase(private val drugsByName: Map<String, Drug>, private val treatmentsByName: Map<String, Treatment>) {
    fun findTreatmentByName(treatmentName: String): Treatment? {
        return treatmentsByName.get(treatmentName.replace(" ", "_").lowercase(Locale.getDefault()))
    }

    fun findDrugByName(drugName: String): Drug? {
        return drugsByName.get(drugName.replace(" ", "_").lowercase(Locale.getDefault()))
    }
}
