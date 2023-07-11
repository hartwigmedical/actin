package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

private const val ANATOMICAL_MAIN_GROUP = "Alimentary tract and metabolism"

class AtcModelTest {

    @Test
    fun shouldThrowWhenAtcCodeNotFound() {
        assertThatThrownBy {
            val victim = AtcModel(mapOf("A" to ANATOMICAL_MAIN_GROUP))
            victim.resolve("not_a_code")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun shouldResolveFiveLevelsOfAtcClassification() {
        val victim = AtcModel(mapOf(
            "A" to ANATOMICAL_MAIN_GROUP,
            "A10" to "Drugs used in diabetes",
            "A10B" to "Blood glucose lowering drugs, excl. insulins",
            "A10BA" to "Biguanides",
            "A10BA02" to "metformin"
        ))
        val result = victim.resolve("A10BA02")
        assertThat(result.anatomicalMainGroup()).isEqualTo(ImmutableAtcLevel.builder().code("A").name(ANATOMICAL_MAIN_GROUP).build())
        //TODO Add other levels
    }

}