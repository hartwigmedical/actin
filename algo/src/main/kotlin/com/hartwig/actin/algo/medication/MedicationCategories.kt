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
                    "PARP inhibitors" to convertToAtcLevel(setOf("L01XK"), atcTree),
                    "Immunosuppressants" to convertToAtcLevel(setOf("L04"), atcTree),
                    "Systemic antibiotics" to convertToAtcLevel(
                        setOf(
                            "A07A",
                            "G01AA",
                            "R02AB",
                            "L01D",
                            "J01",
                            "J02",
                            "J04",
                        ), atcTree
                    ),
                    "Corticosteroids for systemic use" to convertToAtcLevel(setOf("H02"), atcTree),
                    "Antiepileptics" to convertToAtcLevel(setOf("N02"), atcTree),
                    "Monoclonal antibodies and antibody drug conjugates" to convertToAtcLevel(setOf("L01F"), atcTree),
                    "Endocrine therapy" to convertToAtcLevel(setOf("L02"), atcTree),
                    "Other antianemic preparations" to convertToAtcLevel(setOf("B03X"), atcTree),
                    "Colony stimulating factors" to convertToAtcLevel(setOf("L03AA"), atcTree),
                    "Bisphosphonates" to convertToAtcLevel(setOf("M05BA", "M05BB"), atcTree)
                ),
                atcTree
            )
        }

        private fun convertToAtcLevel(atcCodes: Set<String>, atcTree: AtcTree): Set<AtcLevel> {
            return atcCodes.map { atcTree.resolve(it) }.toSet()
        }
    }
}