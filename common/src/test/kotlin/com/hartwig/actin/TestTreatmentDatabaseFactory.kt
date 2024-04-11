package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

object TestTreatmentDatabaseFactory {

    const val CISPLATIN = "CISPLATIN"
    const val CAPECITABINE_OXALIPLATIN = "CAPECITABINE+OXALIPLATIN"
    const val PEMBROLIZUMAB = "PEMBROLIZUMAB"
    const val RADIOTHERAPY = "RADIOTHERAPY"
    const val ABLATION = "ABLATION"

    fun createProper(): TreatmentDatabase {
        val capecitabine = chemoDrug("CAPECITABINE", DrugType.ANTIMETABOLITE)
        val cisplatin = chemoDrug(CISPLATIN, DrugType.ANTIMETABOLITE)
        val oxaliplatin = chemoDrug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND)
        val pembrolizumab = chemoDrug("PEMBROLIZUMAB", DrugType.TOPO1_INHIBITOR)
        val drugMap = listOf(cisplatin, capecitabine, oxaliplatin, pembrolizumab).associateBy { it.name.lowercase() }

        val cisplatinTreatment = DrugTreatment(name = CISPLATIN, drugs = setOf(cisplatin))
        val capoxTreatment = DrugTreatment(name = CAPECITABINE_OXALIPLATIN, drugs = setOf(capecitabine, oxaliplatin))
        val pembrolizumabTreatment = DrugTreatment(name = PEMBROLIZUMAB, drugs = setOf(pembrolizumab))
        val radiotherapy = Radiotherapy(name = RADIOTHERAPY)
        val ablation = OtherTreatment(name = ABLATION, isSystemic = false, categories = setOf(TreatmentCategory.ABLATION))
        val treatmentMap = listOf(
            cisplatinTreatment,
            capoxTreatment,
            pembrolizumabTreatment,
            radiotherapy,
            ablation
        ).associateBy { it.name.lowercase() }

        return TreatmentDatabase(drugMap, treatmentMap)
    }

    private fun chemoDrug(name: String, drugType: DrugType): Drug {
        return Drug(name = name, drugTypes = setOf(drugType), category = TreatmentCategory.CHEMOTHERAPY)
    }
}
