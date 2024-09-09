package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.medication.MedicationCategories

class TreatmentDatabase(private val drugsByName: Map<String, Drug>, private val treatmentsByName: Map<String, Treatment>) {

    fun findTreatmentByName(treatmentName: String): Treatment? {
        return treatmentsByName[treatmentName.replace(" ", "_").lowercase()]
    }

    fun findDrugByName(drugName: String): Drug? {
        return drugsByName[drugName.replace(" ", "_").lowercase()]
    }

    fun findDrugByAtcName(atcName: String, atcCode: String): Drug? {
        val antiCancerAtcCodes = MedicationCategories.ANTI_CANCER_ATC_CODES
        return if (antiCancerAtcCodes.any { atcCode.startsWith(it) && !atcCode.startsWith("L01XD") }) {
            findDrugByName(atcName.split(", ").first())
        } else null
    }
}
