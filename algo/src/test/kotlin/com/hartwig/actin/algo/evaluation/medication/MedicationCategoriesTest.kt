package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val CALCIUM_HOMEOSTASIS = "H05"
private const val DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION = "M05B"

class MedicationCategoriesTest {

    @Test
    fun shouldResolveKnownCategory() {
        val atcTree = mockk<AtcTree>()
        val firstLevel = ImmutableAtcLevel.builder().code(CALCIUM_HOMEOSTASIS).name("").build()
        val secondLevel = ImmutableAtcLevel.builder().code(DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION).name("").build()
        every { atcTree.resolve(CALCIUM_HOMEOSTASIS) } returns firstLevel
        every { atcTree.resolve(DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION) } returns secondLevel
        val victim = MedicationCategories.create(atcTree)
        assertThat(victim.resolve("Bone resorptive")).containsExactly(firstLevel, secondLevel)
    }

    @Test
    fun shouldFallbackToResolvingATCCodeDirectly() {
        val atcTree = mockk<AtcTree>()
        val firstLevel = ImmutableAtcLevel.builder().code(CALCIUM_HOMEOSTASIS).name("").build()
        every { atcTree.resolve(CALCIUM_HOMEOSTASIS) } returns firstLevel
        val victim = MedicationCategories(emptyMap(), atcTree)
        assertThat(victim.resolve(CALCIUM_HOMEOSTASIS)).containsExactly(firstLevel)
    }
}