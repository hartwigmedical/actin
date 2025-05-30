package com.hartwig.actin

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

object TestTreatmentDatabaseFactory {

    const val CISPLATIN = "CISPLATIN"
    const val CAPECITABINE_OXALIPLATIN = "CAPECITABINE+OXALIPLATIN"
    const val PEMBROLIZUMAB = "PEMBROLIZUMAB"
    const val RADIOTHERAPY = "RADIOTHERAPY"
    const val ABLATION = "ABLATION"

    fun createProper(): TreatmentDatabase {
        val capecitabine = chemoDrug("CAPECITABINE", DrugType.ANTIMETABOLITE, TreatmentCategory.CHEMOTHERAPY)
        val cisplatin = chemoDrug(CISPLATIN, DrugType.ANTIMETABOLITE, TreatmentCategory.CHEMOTHERAPY)
        val oxaliplatin = chemoDrug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND, TreatmentCategory.CHEMOTHERAPY)
        val pembrolizumab = chemoDrug("PEMBROLIZUMAB", DrugType.TOPO1_INHIBITOR, TreatmentCategory.IMMUNOTHERAPY)
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

    private fun chemoDrug(name: String, drugType: DrugType, category: TreatmentCategory): Drug {
        return Drug(name = name, drugTypes = setOf(drugType), category = category)
    }
}
