package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

object TestTreatmentDatabaseFactory {
    const val CAPECITABINE_OXALIPLATIN = "CAPECITABINE+OXALIPLATIN"
    const val RADIOTHERAPY = "RADIOTHERAPY"
    const val ABLATION = "ABLATION"

    fun createProper(): TreatmentDatabase {
        val drugMap = listOf(chemoDrug("CAPECITABINE", DrugType.ANTIMETABOLITE), chemoDrug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND))
            .associateBy { it.name.lowercase() }

        val capox = DrugTreatment(name = CAPECITABINE_OXALIPLATIN, drugs = drugMap.values.toSet())
        val radiotherapy = Radiotherapy(name = RADIOTHERAPY)
        val ablation = OtherTreatment(name = ABLATION, isSystemic = false, categories = setOf(TreatmentCategory.ABLATION))
        val treatmentMap = listOf(capox, radiotherapy, ablation).associateBy { it.name.lowercase() }
        
        return TreatmentDatabase(drugMap, treatmentMap)
    }

    private fun chemoDrug(name: String, drugType: DrugType): Drug {
        return Drug(name = name, drugTypes = setOf(drugType), category = TreatmentCategory.CHEMOTHERAPY)
    }
}
