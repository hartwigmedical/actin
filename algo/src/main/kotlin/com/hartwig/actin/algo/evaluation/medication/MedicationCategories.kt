package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.AtcLevel

class MedicationCategories(val categories: Map<String, Set<AtcLevel>>) {

    fun resolve(categoryName: String): Set<AtcLevel> {
        return categories[categoryName]
            ?: throw IllegalArgumentException("Could not find category [$categoryName] in existing categories [$categories]")
    }

    companion object {
        fun create(atcTree: AtcTree): MedicationCategories {
            return MedicationCategories(
                mapOf(
                    "Bone resorptive" to setOf("H05", "M05B").map { atcTree.resolve(it) }.toSet()
                )
            )
        }
    }
}