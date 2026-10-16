package com.hartwig.actin.clinical.serialization

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiopharmaceutical
import com.hartwig.actin.datamodel.clinical.treatment.RadiopharmaceuticalType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.StandardDrug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClinicalRecordJsonMapperTest {

    private val capecitabine = StandardDrug(
        name = "CAPECITABINE", drugTypes = setOf(DrugType.ANTIMETABOLITE), category = TreatmentCategory.CHEMOTHERAPY
    )
    private val lutetiumDotatate = Radiopharmaceutical(
        name = "LUTETIUM_LU_177_DOTATATE", drugTypes = setOf(RadiopharmaceuticalType.RADIOLIGAND_THERAPY)
    )

    @Test
    fun `Should convert drugs of a drug treatment back and forth by name`() {
        val gson = ClinicalRecordJsonMapper.createWithDrugMap(drugMap(capecitabine))
        val treatment = DrugTreatment(name = "CAPECITABINE", drugs = setOf(capecitabine))

        val json = gson.toJson(treatment)

        assertThat(json).contains("\"drugs\":[\"CAPECITABINE\"]")
        assertThat(gson.fromJson(json, DrugTreatment::class.java)).isEqualTo(treatment)
    }

    @Test
    fun `Should convert drugs of a radiotherapy back and forth by name`() {
        val gson = ClinicalRecordJsonMapper.createWithDrugMap(drugMap(lutetiumDotatate))
        val treatment = Radiotherapy(name = "LUTETIUM_LU_177_DOTATATE", drugs = setOf(lutetiumDotatate))

        val json = gson.toJson(treatment)

        assertThat(json).contains("\"drugs\":[\"LUTETIUM_LU_177_DOTATATE\"]")
        assertThat(gson.fromJson(json, Radiotherapy::class.java)).isEqualTo(treatment)
    }

    @Test
    fun `Should convert drugs of every drug class back and forth as full objects`() {
        val gson = ClinicalRecordJsonMapper.create()
        val treatments = listOf(
            DrugTreatment(name = "CAPECITABINE", drugs = setOf(capecitabine)),
            Radiotherapy(name = "LUTETIUM_LU_177_DOTATATE", drugs = setOf(lutetiumDotatate))
        )

        treatments.forEach { treatment ->
            assertThat(gson.fromJson(gson.toJson(treatment), treatment::class.java)).isEqualTo(treatment)
        }
    }

    private fun drugMap(vararg drugs: Drug) = drugs.associateBy { it.name.lowercase() }
}
