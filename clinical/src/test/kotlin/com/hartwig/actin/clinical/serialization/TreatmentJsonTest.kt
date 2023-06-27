package com.hartwig.actin.clinical.serialization

import com.google.common.io.Resources
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.Therapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.serialization.TreatmentJson.read
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.io.IOException

class TreatmentJsonTest {
    @Test
    @Throws(IOException::class)
    fun shouldReadDrugsFromJsonFile() {
        val expectedDrugs = setOf(
            ImmutableDrug.builder().name("Capecitabine").addDrugClasses(DrugClass.ANTIMETABOLITE).build(),
            ImmutableDrug.builder().name("Oxaliplatin").addDrugClasses(DrugClass.PLATINUM_COMPOUND).build()
        )
        val drugsByName = expectedDrugs.associateBy { it.name().lowercase() }

        val treatments = read(TREATMENT_JSON, drugsByName)
        assertThat(treatments).hasSize(1)
        val therapy = treatments[0] as Therapy
        assertThat(therapy.name()).isEqualTo("Capecitabine+Oxaliplatin")
        assertThat(therapy.isSystemic).isTrue
        assertThat(therapy.categories()).containsOnly(TreatmentCategory.CHEMOTHERAPY)
        assertThat(therapy.drugs()).isEqualTo(expectedDrugs)
    }

    companion object {
        private val CLINICAL_DIRECTORY = Resources.getResource("clinical").path
        private val TREATMENT_JSON = CLINICAL_DIRECTORY + File.separator + "treatment.json"
    }
}