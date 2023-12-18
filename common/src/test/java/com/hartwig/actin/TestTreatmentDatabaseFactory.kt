package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import java.util.function.Function
import java.util.stream.Stream

object TestTreatmentDatabaseFactory {
    const val CAPECITABINE_OXALIPLATIN = "CAPECITABINE+OXALIPLATIN"
    const val RADIOTHERAPY = "RADIOTHERAPY"
    const val ABLATION = "ABLATION"
    fun createProper(): TreatmentDatabase {
        val drugMap: Map<String, Drug> =
            Stream.of<Drug>(chemoDrug("CAPECITABINE", DrugType.ANTIMETABOLITE), chemoDrug("OXALIPLATIN", DrugType.PLATINUM_COMPOUND))
                .collect<Map<String, Drug>, Any>(Collectors.toMap<Drug, String, Drug>(Function<Drug, String> { drug: Drug ->
                    drug.name().lowercase(Locale.getDefault())
                }, Function.identity<Drug>()))
        val capox: Treatment =
            ImmutableDrugTreatment.builder().name(CAPECITABINE_OXALIPLATIN).addAllDrugs(drugMap.values).isSystemic(true).build()
        val radiotherapy: Treatment = ImmutableRadiotherapy.builder().name(RADIOTHERAPY).build()
        val ablation: Treatment =
            ImmutableOtherTreatment.builder().name(ABLATION).isSystemic(false).addCategories(TreatmentCategory.ABLATION).build()
        val treatmentMap = Stream.of<Treatment>(capox, radiotherapy, ablation)
            .collect(
                Collectors.toMap<Treatment, String, Treatment>(
                    Function<Treatment, String> { treatment: Treatment -> treatment.name().lowercase(Locale.getDefault()) },
                    Function.identity<Treatment>()
                )
            )
        return TreatmentDatabase(drugMap, treatmentMap)
    }

    private fun chemoDrug(name: String, drugType: DrugType): Drug {
        return ImmutableDrug.builder().name(name).addDrugTypes(drugType).category(TreatmentCategory.CHEMOTHERAPY).build()
    }
}
