package com.hartwig.actin.clinical.serialization

import com.google.common.io.Resources
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.serialization.DrugJson.read
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File
import java.io.IOException

class DrugJsonTest {
    @Test
    @Throws(IOException::class)
    fun shouldReadDrugsFromJsonFile() {
        assertThat(read(DRUG_JSON)).hasSize(2)
            .containsOnly(
                ImmutableDrug.builder().name("Capecitabine").addDrugClasses(DrugClass.ANTIMETABOLITE).build(),
                ImmutableDrug.builder().name("Oxaliplatin").addDrugClasses(DrugClass.PLATINUM_COMPOUND).build()
            )
    }

    companion object {
        private val CLINICAL_DIRECTORY = Resources.getResource("clinical").path
        private val DRUG_JSON = CLINICAL_DIRECTORY + File.separator + "drug.json"
    }
}