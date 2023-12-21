package com.hartwig.actin

import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug

class TreatmentDatabaseTest {
    @org.junit.Test
    @Throws(java.io.IOException::class)
    fun shouldFindExistingTreatmentByName() {
        org.assertj.core.api.Assertions.assertThat(treatmentDatabase().findTreatmentByName("nonexistent")).isNull()
        val treatment = treatmentDatabase().findTreatmentByName("Capecitabine+Oxaliplatin")
        org.assertj.core.api.Assertions.assertThat(treatment).isNotNull()
        org.assertj.core.api.Assertions.assertThat<TreatmentCategory>(treatment!!.categories())
            .containsExactly(TreatmentCategory.CHEMOTHERAPY)
        org.assertj.core.api.Assertions.assertThat(treatment!!.isSystemic).isTrue()
        org.assertj.core.api.Assertions.assertThat<Drug>((treatment as DrugTreatment?).drugs())
            .extracting(java.util.function.Function<Drug, Any> { obj: Drug -> obj.name() },
                java.util.function.Function<Drug, Any> { obj: Drug -> obj.drugTypes() })
            .containsExactlyInAnyOrder(
                org.assertj.core.api.Assertions.tuple("CAPECITABINE", java.util.Set.of<DrugType>(DrugType.ANTIMETABOLITE)),
                org.assertj.core.api.Assertions.tuple("OXALIPLATIN", java.util.Set.of<DrugType>(DrugType.PLATINUM_COMPOUND))
            )
    }

    @org.junit.Test
    fun shouldEquateSpacesAndUnderscoresInTreatmentLookups() {
        val treatment: com.hartwig.actin.clinical.datamodel.treatment.Treatment =
            ImmutableOtherTreatment.builder().isSystemic(false).name("MULTIWORD_NAME").build()
        val treatmentDatabase = TreatmentDatabase(
            emptyMap<String, Drug>(),
            java.util.Map.of<String, com.hartwig.actin.clinical.datamodel.treatment.Treatment>(
                treatment.name().lowercase(Locale.getDefault()), treatment
            )
        )
        org.assertj.core.api.Assertions.assertThat(treatmentDatabase.findTreatmentByName("Multiword name")).isEqualTo(treatment)
    }

    @org.junit.Test
    @Throws(java.io.IOException::class)
    fun shouldFindExistingDrugByName() {
        org.assertj.core.api.Assertions.assertThat<Drug>(treatmentDatabase().findDrugByName("nonexistent")).isNull()
        val drug: Drug? = treatmentDatabase().findDrugByName("Capecitabine")
        org.assertj.core.api.Assertions.assertThat<Drug>(drug).isNotNull()
        org.assertj.core.api.Assertions.assertThat(drug.name()).isEqualTo("CAPECITABINE")
        org.assertj.core.api.Assertions.assertThat<DrugType>(drug.drugTypes()).containsExactly(DrugType.ANTIMETABOLITE)
    }

    @org.junit.Test
    fun shouldEquateSpacesAndUnderscoresInDrugLookups() {
        val drug: Drug = ImmutableDrug.builder().name("MULTIWORD_NAME").category(TreatmentCategory.CHEMOTHERAPY).build()
        val treatmentDatabase = TreatmentDatabase(
            java.util.Map.of<String, Drug>(drug.name().lowercase(Locale.getDefault()), drug),
            emptyMap<String, com.hartwig.actin.clinical.datamodel.treatment.Treatment>()
        )
        org.assertj.core.api.Assertions.assertThat<Drug>(treatmentDatabase.findDrugByName("Multiword name")).isEqualTo(drug)
    }

    companion object {
        @Throws(java.io.IOException::class)
        private fun treatmentDatabase(): TreatmentDatabase {
            return TreatmentDatabaseFactory.createFromPath(com.google.common.io.Resources.getResource("clinical").path)
        }
    }
}