package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.AtcLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val CALCIUM_HOMEOSTASIS = "H05"
private const val DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION = "M05B"

class MedicationCategoriesTest {

    @Test
    fun `Should resolve known category`() {
        val atcTree = AtcTree(emptyMap())
        val firstLevel = AtcLevel(code = CALCIUM_HOMEOSTASIS, name = "")
        val secondLevel = AtcLevel(code = DRUGS_AFFECTING_BONE_STRUCTURE_AND_MINERALIZATION, name = "")
        val victim = MedicationCategories(mapOf("Bone resorptive" to setOf(firstLevel, secondLevel)), atcTree)
        assertThat(victim.resolve("Bone resorptive")).containsExactly(firstLevel, secondLevel)
    }

    @Test
    fun `Should fallback to resolving ATC code directly`() {
        val atcTree = AtcTree(mapOf(CALCIUM_HOMEOSTASIS to ""))
        val firstLevel = AtcLevel(code = CALCIUM_HOMEOSTASIS, name = "")
        val victim = MedicationCategories(emptyMap(), atcTree)
        assertThat(victim.resolve(CALCIUM_HOMEOSTASIS)).containsExactly(firstLevel)
    }
}