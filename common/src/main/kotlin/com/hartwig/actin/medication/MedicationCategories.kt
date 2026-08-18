package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.MedicationCategoryMappings

class MedicationCategories(private val knownCategories: Map<String, Set<AtcLevel>>, private val atcTree: AtcTree) {

    fun resolve(categoryName: String): Set<AtcLevel> {
        return knownCategories.entries.firstOrNull { it.key.equals(categoryName, ignoreCase = true) }?.value
            ?: setOf(atcTree.resolve(categoryName))
    }

    fun resolveCategoryName(categoryName: String): String {
        return knownCategories.entries.firstOrNull { it.key.equals(categoryName, ignoreCase = true) }?.key
            ?: atcTree.resolve(categoryName).name
    }

    companion object {

        fun create(atcTree: AtcTree): MedicationCategories {
            return MedicationCategories(
                MedicationCategoryMappings.MEDICATION_CATEGORIES_TO_ATC_CODES.mapValues { (_, atcCodes) ->
                    convertToAtcLevel(atcCodes, atcTree)
                },
                atcTree
            )
        }

        private fun convertToAtcLevel(atcCodes: Set<String>, atcTree: AtcTree): Set<AtcLevel> {
            return atcCodes.map(atcTree::resolve).toSet()
        }

        fun isAntiCancerMedication(atcCode: String?): Boolean {
            return atcCode != null && MedicationCategoryMappings.isAntiCancerAtcCode(atcCode) && !atcCode.startsWith("L01XD")
        }
    }
}