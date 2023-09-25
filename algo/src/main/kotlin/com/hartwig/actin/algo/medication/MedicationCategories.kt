package com.hartwig.actin.algo.medication

import com.hartwig.actin.algo.evaluation.medication.AtcTree
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
                    "Bone resorptive" to convertToAtcLevel(setOf("H05", "M05B"), atcTree),
                    "Anticoagulants" to convertToAtcLevel(setOf("B01", "B02"), atcTree),
                    "Azole" to convertToAtcLevel(setOf("D01AC", "J02AC", "J02AB"), atcTree),
                    "Coumarin derivative" to convertToAtcLevel(setOf("B01AA"), atcTree),
                    "Gonadorelin" to convertToAtcLevel(setOf("H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "Anticancer" to convertToAtcLevel(setOf("L01", "L02", "L04", "H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "Immunotherapy" to convertToAtcLevel(setOf("L01FF02", "L01FF01", "L01FX04", "L01FF06", "L01FF04"), atcTree),
                    "Hypomethylating agents" to convertToAtcLevel(setOf("L01BC07", "L01BC08"), atcTree),
                    "Chemotherapy" to convertToAtcLevel(setOf("L01XA", "L01BC", "L01CD", "L01A"), atcTree),
                    "Gonadorelin" to convertToAtcLevel(setOf("H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "PARP inhibitors" to convertToAtcLevel(setOf("L01XK"), atcTree)
                ),
                atcTree
            )
        }

        private fun convertToAtcLevel(atcCodes: Set<String>, atcTree: AtcTree): Set<AtcLevel> {
            return atcCodes.map { atcTree.resolve(it) }.toSet()
        }
    }
}