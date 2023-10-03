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
                    "Anticoagulants" to convertToAtcLevel(setOf("B01AA", "B01AB", "B01AE", "B01AF", "B01AX"), atcTree),
                    "Azole" to convertToAtcLevel(setOf("A07AC", "D01AC", "G01AF", "G01BF", "G01AG", "J02AC", "J02AB"), atcTree),
                    "Coumarin derivative" to convertToAtcLevel(setOf("B01AA"), atcTree),
                    "Gonadorelin" to convertToAtcLevel(setOf("H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "Anticancer" to convertToAtcLevel(setOf("L01", "L02", "L04", "H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "Immunotherapy" to convertToAtcLevel(setOf("L01FF", "L01FX04"), atcTree),
                    "Hypomethylating agents" to convertToAtcLevel(setOf("L01BC07", "L01BC08"), atcTree),
                    "Chemotherapy" to convertToAtcLevel(setOf("L01XA", "L01BC", "L01CD", "L01A"), atcTree),
                    "PARP inhibitors" to convertToAtcLevel(setOf("L01XK"), atcTree),
                    "Immunosuppressants" to convertToAtcLevel(setOf("L04"), atcTree),
                    "Systemic antibiotics" to convertToAtcLevel(
                        setOf(
                            "A07A",
                            "G01AA",
                            "R02AB",
                            "J01",
                            "J02",
                            "J04",
                        ), atcTree
                    ),
                    "Corticosteroids for systemic use" to convertToAtcLevel(setOf("H02", "M01BA"), atcTree),
                    "Antiepileptics" to convertToAtcLevel(setOf("N03"), atcTree),
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