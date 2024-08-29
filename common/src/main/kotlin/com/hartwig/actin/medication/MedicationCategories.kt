package com.hartwig.actin.medication

import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

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
                    "Systemic corticosteroids" to convertToAtcLevel(setOf("H02", "M01BA"), atcTree),
                ),
                atcTree
            )
        }

        private fun convertToAtcLevel(atcCodes: Set<String>, atcTree: AtcTree): Set<AtcLevel> {
            return atcCodes.map(atcTree::resolve).toSet()
        }

        val MEDICATION_CATEGORIES_TO_DRUG_TYPES = mapOf(
            "Chemotherapy" to setOf(TreatmentCategory.CHEMOTHERAPY),
            "Endocrine therapy" to setOf(TreatmentCategory.HORMONE_THERAPY),
            "Gonadorelin" to setOf(DrugType.GONADOTROPIN_AGONIST, DrugType.GONADOTROPIN_ANTAGONIST),
            "Hypomethylating agents" to setOf(DrugType.DNMT_INHIBITOR),
            "Immunotherapy" to setOf(TreatmentCategory.IMMUNOTHERAPY),
            "Monoclonal antibodies and antibody drug conjugates" to setOf(DrugType.MONOCLONAL_ANTIBODY_TARGETED_THERAPY, DrugType.MONOCLONAL_ANTIBODY_IMMUNOTHERAPY, DrugType.MONOCLONAL_ANTIBODY_MMAE_CONJUGATE, DrugType.MONOCLONAL_ANTIBODY_SUPPORTIVE_TREATMENT, DrugType.ANTIBODY_DRUG_CONJUGATE_IMMUNOTHERAPY, DrugType.ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY),
            "PARP inhibitors" to setOf(DrugType.PARP_INHIBITOR),
            "L01CD" to setOf(DrugType.TAXANE),
            "L02BB" to setOf(DrugType.ANTI_ANDROGEN),
            "L01A" to setOf(DrugType.ALKYLATING_AGENT),
            "LO1XL" to setOf(DrugType.ANTI_CLDN6_CAR_T, DrugType.HER2_CAR_T),
            "L01E" to setOf(DrugType.TYROSINE_KINASE_INHIBITOR),
            "Anticancer" to setOf(
                TreatmentCategory.CHEMOTHERAPY,
                TreatmentCategory.TARGETED_THERAPY,
                TreatmentCategory.IMMUNOTHERAPY,
                TreatmentCategory.HORMONE_THERAPY
            ),
            "Antineoplastic agents" to setOf(
                TreatmentCategory.CHEMOTHERAPY,
                TreatmentCategory.TARGETED_THERAPY,
                TreatmentCategory.IMMUNOTHERAPY,
                TreatmentCategory.HORMONE_THERAPY
            )
        )
    }
}