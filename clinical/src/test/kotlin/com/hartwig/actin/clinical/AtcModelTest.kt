package com.hartwig.actin.clinical

import com.google.common.io.Resources
import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.FULL_ATC_CODE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.clinical.datamodel.AtcLevel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class AtcModelTest {

    @Test
    fun shouldReturnNullForTrialMedication() {
        assertThat(WhoAtcModel(emptyMap()).resolveByCode("123")).isNull()
    }

    @Test
    fun shouldThrowWhenAtcCodeNotFound() {
        assertThatThrownBy {
            val victim = WhoAtcModel(mapOf("A" to ANATOMICAL))
            victim.resolveByCode("notacod")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldResolveFiveLevelsOfAtcClassification() {
        val victim = createAtcModel()
        val result = victim.resolveByCode(FULL_ATC_CODE)!!
        assertThat(result.anatomicalMainGroup).isEqualTo(AtcLevel(code = "N", name = ANATOMICAL))
        assertThat(result.therapeuticSubGroup).isEqualTo(AtcLevel(code = "N02", name = THERAPEUTIC))
        assertThat(result.pharmacologicalSubGroup).isEqualTo(AtcLevel(code = "N02B", name = PHARMACOLOGICAL))
        assertThat(result.chemicalSubGroup).isEqualTo(AtcLevel(code = "N02BE", name = CHEMICAL))
        assertThat(result.chemicalSubstance).isEqualTo(AtcLevel(code = FULL_ATC_CODE, name = CHEMICAL_SUBSTANCE))
    }

    @Test
    fun shouldReturnClassificationForFourLevelsAtcClassification() {
        val victim = createAtcModel()
        val result = victim.resolveByCode("N02BE")!!
        assertThat(result.chemicalSubstance).isNull()
    }

    @Test
    fun shouldReturnNullForLessThanFourLevelsAtcClassification() {
        val victim = createAtcModel()
        val result = victim.resolveByCode("N02B")
        assertThat(result).isNull()
    }

    @Test
    fun shouldReturnSetOfAtcCodesForCorrectMedicationName() {
        val victim = createAtcModel()
        val result = victim.resolveByName("paracetamol")
        assertThat(result).isEqualTo(setOf("N02BE01"))
    }

    @Test
    fun shouldReturnEmptySetWhenMedicationNameNotFound() {
        val victim = createAtcModel()
        val result = victim.resolveByName("not a medication name")
        assertThat(result).isNullOrEmpty()
    }

    private fun createAtcModel() = WhoAtcModel.createFromFile(Resources.getResource("atc_config/atc_tree.tsv").path)
}