package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.AtcLevel

private val systemicAntibiotics = setOf("A07A", "G01AA", "R02AB", "J01", "J02", "J04")
private val systemicAntimycotics = setOf("J02")
private val systemicAntivirals = setOf("J05")

class MedicationCategories(private val knownCategories: Map<String, Set<AtcLevel>>, private val atcTree: AtcTree) {

    fun resolve(categoryName: String): Set<AtcLevel> {
        return knownCategories[categoryName]
            ?: setOf(atcTree.resolve(categoryName))
    }

    companion object {
        fun create(atcTree: AtcTree): MedicationCategories {
            return MedicationCategories(
                mapOf(
                    "Anticancer" to convertToAtcLevel(setOf("L01", "L02", "L04", "H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "Anticoagulants" to convertToAtcLevel(setOf("B01AA", "B01AB", "B01AC", "B01AD", "B01AE", "B01AF", "B01AX"), atcTree),
                    "Antiepileptics" to convertToAtcLevel(setOf("N03"), atcTree),
                    "Antiinflammatory and antirheumatic products" to convertToAtcLevel(setOf("M01"), atcTree),
                    "Antimicrobials" to convertToAtcLevel(systemicAntibiotics + systemicAntimycotics + systemicAntivirals, atcTree),
                    "Antineoplastic agents" to convertToAtcLevel(setOf("L01"), atcTree),
                    "Azole" to convertToAtcLevel(setOf("A07AC", "D01AC", "G01AF", "G01BF", "G01AG", "J02AC", "J02AB"), atcTree),
                    "Bisphosphonates" to convertToAtcLevel(setOf("M05BA", "M05BB"), atcTree),
                    "Bone resorptive" to convertToAtcLevel(setOf("H05", "M05B"), atcTree),
                    "Chemotherapy" to convertToAtcLevel(setOf("L01XA", "L01BC", "L01CD", "L01A"), atcTree),
                    "Colony stimulating factors" to convertToAtcLevel(setOf("L03AA"), atcTree),
                    "Coumarin derivative" to convertToAtcLevel(setOf("B01AA"), atcTree),
                    "Endocrine therapy" to convertToAtcLevel(setOf("L02"), atcTree),
                    "Gonadorelin" to convertToAtcLevel(setOf("H01CC", "H01CA", "G03XA", "L02AE"), atcTree),
                    "Hypomethylating agents" to convertToAtcLevel(setOf("L01BC07", "L01BC08"), atcTree),
                    "Immunostimulants" to convertToAtcLevel(setOf("L03"), atcTree),
                    "Immunosuppressants" to convertToAtcLevel(setOf("L04"), atcTree),
                    "Immunotherapy" to convertToAtcLevel(setOf("L01FF", "L01FX04"), atcTree),
                    "Monoclonal antibodies and antibody drug conjugates" to convertToAtcLevel(setOf("L01F"), atcTree),
                    "Other antianemic preparations" to convertToAtcLevel(setOf("B03X"), atcTree),
                    "PARP inhibitors" to convertToAtcLevel(setOf("L01XK"), atcTree),
                    "Platelet aggregation inhibitors" to convertToAtcLevel(setOf("B01AC"), atcTree),
                    "RANKL targeting agents" to convertToAtcLevel(setOf("M05BX04"), atcTree),
                    "Systemic antibiotics" to convertToAtcLevel(systemicAntibiotics, atcTree),
                    "Systemic antimycotics" to convertToAtcLevel(systemicAntimycotics, atcTree),
                    "Systemic antivirals" to convertToAtcLevel(systemicAntivirals, atcTree),
                    "Systemic corticosteroids" to convertToAtcLevel(setOf("H02", "M01BA"), atcTree),
                ),
                atcTree
            )
        }

        private fun convertToAtcLevel(atcCodes: Set<String>, atcTree: AtcTree): Set<AtcLevel> {
            return atcCodes.map(atcTree::resolve).toSet()
        }
    }
}