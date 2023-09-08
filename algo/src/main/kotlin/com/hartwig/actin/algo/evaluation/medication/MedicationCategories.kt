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
                    "Bone resorptive" to setOf("H05", "M05B").map { atcTree.resolve(it) }.toSet(),
                    "Anticoagulants" to setOf("B01", "B02").map { atcTree.resolve(it) }.toSet(),
                    "Azole" to setOf("D01AC", "J02AC", "J02AB").map { atcTree.resolve(it) }.toSet(),
                    "Coumarin derivative" to setOf("B01AA").map { atcTree.resolve(it) }.toSet(),
                    "Gonadorelin" to setOf("H01CC", "H01CA", "G03XA", "L02AE").map { atcTree.resolve(it) }.toSet(),
                    "Anticancer" to setOf("L01", "L02", "L04", "H01CC", "H01CA", "G03XA", "L02AE").map { atcTree.resolve(it) }.toSet(),
                    "Immunotherapy" to setOf("L01FF02", "L01FF01", "L01FX04", "L01FF06", "L01FF04").map { atcTree.resolve(it) }.toSet(),
                    "Hypomethylating agents" to setOf("L01BC07", "L01BC08").map { atcTree.resolve(it) }.toSet(),
                    "Chemotherapy" to setOf("L01XA", "L01BC", "L01CD", "L01A").map { atcTree.resolve(it) }.toSet(),
                    "Gonadorelin" to setOf("H01CC", "H01CA", "G03XA", "L02AE").map { atcTree.resolve(it) }.toSet(),
                    "PARP inhibitors" to setOf("L01XK").map { atcTree.resolve(it) }.toSet()
                ),
                atcTree
            )
        }
    }
}