package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.FULL_ATC_CODE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class AtcModelTest {

    @Test
    fun `Should return null for trial medication`() {
        assertThat(WhoAtcModel(emptyMap(), emptyMap()).resolveByCode("123", CHEMICAL_SUBSTANCE)).isNull()
    }

    @Test
    fun `Should throw when ATC code not found`() {
        assertThatThrownBy {
            val victim = WhoAtcModel(mapOf("A" to ANATOMICAL), emptyMap())
            victim.resolveByCode("notacode", "notalevelname")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Should resolve five levels of ATC classification`() {
        val victim = createAtcModel()
        val result = victim.resolveByCode(FULL_ATC_CODE, CHEMICAL_SUBSTANCE)!!
        assertThat(result.anatomicalMainGroup).isEqualTo(AtcLevel(code = "N", name = ANATOMICAL))
        assertThat(result.therapeuticSubGroup).isEqualTo(AtcLevel(code = "N02", name = THERAPEUTIC))
        assertThat(result.pharmacologicalSubGroup).isEqualTo(AtcLevel(code = "N02B", name = PHARMACOLOGICAL))
        assertThat(result.chemicalSubGroup).isEqualTo(AtcLevel(code = "N02BE", name = CHEMICAL))
        assertThat(result.chemicalSubstance).isEqualTo(AtcLevel(code = FULL_ATC_CODE, name = CHEMICAL_SUBSTANCE))
    }

    @Test
    fun `Should return classification for three levels ATC classification`() {
        val victim = createAtcModel()
        val result = victim.resolveByCode("N02B", CHEMICAL)!!
        assertThat(result.chemicalSubGroup).isNull()
        assertThat(result.chemicalSubstance).isNull()
    }

    @Test
    fun `Should return null for less than three levels ATC classification`() {
        val victim = createAtcModel()
        val result = victim.resolveByCode("N02", PHARMACOLOGICAL)
        assertThat(result).isNull()
    }

    @Test
    fun `Should return new classification for ATC classification with previous ATC code`() {
        val victim = createAtcModel()
        val result = victim.resolveByCode("N03XZ91", CHEMICAL_SUBSTANCE)!!
        assertThat(result.anatomicalMainGroup).isEqualTo(AtcLevel(code = "N", name = ANATOMICAL))
        assertThat(result.therapeuticSubGroup).isEqualTo(AtcLevel(code = "N02", name = THERAPEUTIC))
        assertThat(result.pharmacologicalSubGroup).isEqualTo(AtcLevel(code = "N02B", name = PHARMACOLOGICAL))
        assertThat(result.chemicalSubGroup).isEqualTo(AtcLevel(code = "N02BE", name = CHEMICAL))
        assertThat(result.chemicalSubstance).isEqualTo(AtcLevel(code = FULL_ATC_CODE, name = CHEMICAL_SUBSTANCE))
    }

    @Test
    fun `Should return set of ATC codes for correct medication name`() {
        val victim = createAtcModel()
        val result = victim.resolveByName("paracetamol")
        assertThat(result).isEqualTo(setOf("N02BE01"))
    }

    @Test
    fun `Should return empty set when medication name not found`() {
        val victim = createAtcModel()
        val result = victim.resolveByName("not a medication name")
        assertThat(result).isNullOrEmpty()
    }

    private fun createAtcModel(): WhoAtcModel {
        return WhoAtcModel.createFromFiles(
            resourceOnClasspath("atc_config/atc_tree.tsv"),
            resourceOnClasspath("atc_config/atc_overrides.tsv")
        )
    }
}