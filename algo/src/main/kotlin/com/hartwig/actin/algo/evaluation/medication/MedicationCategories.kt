package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.AtcLevel

class MedicationCategories(private val knownCategories: Map<String, Set<AtcLevel>>, private val atcTree: AtcTree) {

    fun resolve(categoryName: String): Set<AtcLevel> {
        return knownCategories[categoryName]
            ?: setOf(atcTree.resolve(categoryName))
    }

    companion object {
        fun create(atcTree: AtcTree): MedicationCategories {
            return MedicationCategories(
                mapOf(
                    "Bone resorptive" to setOf("H05", "M05B").map { atcTree.resolve(it) }.toSet()
                ),
                atcTree
            )
        }
    }
}