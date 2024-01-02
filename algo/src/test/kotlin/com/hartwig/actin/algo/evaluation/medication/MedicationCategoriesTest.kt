package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.medication.MedicationCategories
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val CALCIUM_HOMEOSTASIS = "H05"
private const val DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION = "M05B"

class MedicationCategoriesTest {

    @Test
    fun shouldResolveKnownCategory() {
        val atcTree = AtcTree(emptyMap())
        val firstLevel = ImmutableAtcLevel.builder().code(CALCIUM_HOMEOSTASIS).name("").build()
        val secondLevel = ImmutableAtcLevel.builder().code(DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION).name("").build()
        val victim = MedicationCategories(mapOf("Bone resorptive" to setOf(firstLevel, secondLevel)), atcTree)
        assertThat(victim.resolve("Bone resorptive")).containsExactly(firstLevel, secondLevel)
    }

    @Test
    fun shouldFallbackToResolvingATCCodeDirectly() {
        val atcTree = AtcTree(mapOf(CALCIUM_HOMEOSTASIS to ""))
        val firstLevel = ImmutableAtcLevel.builder().code(CALCIUM_HOMEOSTASIS).name("").build()
        val victim = MedicationCategories(emptyMap(), atcTree)
        assertThat(victim.resolve(CALCIUM_HOMEOSTASIS)).containsExactly(firstLevel)
    }
}