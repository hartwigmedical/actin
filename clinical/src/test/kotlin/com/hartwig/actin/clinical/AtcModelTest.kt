package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.*
import com.hartwig.actin.clinical.datamodel.*
import org.assertj.core.api.Assertions.*
import org.junit.*

class AtcModelTest {

    @Test
    fun shouldReturnNullForTrialMedication() {
        assertThat(AtcModel(emptyMap()).resolve("123")).isNull()
    }

    @Test
    fun shouldThrowWhenAtcCodeNotFound() {
        assertThatThrownBy {
            val victim = AtcModel(mapOf("A" to ANATOMICAL))
            victim.resolve("not_a_code")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldResolveFiveLevelsOfAtcClassification() {
        val victim = TestAtcFactory.createProperAtcModel()
        val result = victim.resolve(ATC_CODE)!!
        assertThat(result.anatomicalMainGroup()).isEqualTo(ImmutableAtcLevel.builder().code("N").name(ANATOMICAL).build())
        assertThat(result.therapeuticSubGroup()).isEqualTo(ImmutableAtcLevel.builder().code("N02").name(THERAPEUTIC).build())
        assertThat(result.pharmacologicalSubGroup()).isEqualTo(ImmutableAtcLevel.builder().code("N02B").name(PHARMACOLOGICAL).build())
        assertThat(result.chemicalSubGroup()).isEqualTo(ImmutableAtcLevel.builder().code("N02BE").name(CHEMICAL).build())
        assertThat(result.chemicalSubstance()).isEqualTo(ImmutableAtcLevel.builder().code(ATC_CODE).name(CHEMICAL_SUBSTANCE).build())
    }

}