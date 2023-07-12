package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.ALIMENTARY_TRACT_AND_METABOLISM
import com.hartwig.actin.clinical.curation.BIGUANIDES
import com.hartwig.actin.clinical.curation.BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS
import com.hartwig.actin.clinical.curation.DRUGS_USED_IN_DIABETES
import com.hartwig.actin.clinical.curation.METFORMIN
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class AtcModelTest {

    @Test
    fun shouldThrowWhenAtcCodeNotFound() {
        assertThatThrownBy {
            val victim = AtcModel(mapOf("A" to ALIMENTARY_TRACT_AND_METABOLISM))
            victim.resolve("not_a_code")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldResolveFiveLevelsOfAtcClassification() {
        val victim = TestAtcFactory.createMinimalModel()
        val result = victim.resolve("A10BA02")
        assertThat(result.anatomicalMainGroup()).isEqualTo(ImmutableAtcLevel.builder().code("A").name(ALIMENTARY_TRACT_AND_METABOLISM).build())
        assertThat(result.therapeuticSubGroup()).isEqualTo(ImmutableAtcLevel.builder().code("A10").name(DRUGS_USED_IN_DIABETES).build())
        assertThat(result.pharmacologicalSubGroup()).isEqualTo(ImmutableAtcLevel.builder().code("A10B").name(BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS).build())
        assertThat(result.chemicalSubGroup()).isEqualTo(ImmutableAtcLevel.builder().code("A10BA").name(BIGUANIDES).build())
        assertThat(result.chemicalSubstance()).isEqualTo(ImmutableAtcLevel.builder().code("A10BA02").name(METFORMIN).build())
    }

}