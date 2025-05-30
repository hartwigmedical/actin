package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

private val systemicAntibiotics = setOf("A07A", "G01AA", "R02AB", "J01")
private val systemicAntimycobacterials = setOf("J04")
private val systemicAntimycotics = setOf("J02")
private val systemicAntivirals = setOf("J05")
private val systemicAntimicrobials = systemicAntibiotics + systemicAntimycobacterials + systemicAntimycotics + systemicAntivirals

class MedicationCategories(private val knownCategories: Map<String, Set<AtcLevel>>, private val atcTree: AtcTree) {

    fun resolve(categoryName: String): Set<AtcLevel> {
        return knownCategories[categoryName]
            ?: setOf(atcTree.resolve(categoryName))
    }

    fun resolveCategoryName(categoryName: String): String {
        return if (knownCategories[categoryName] != null) categoryName else {
            atcTree.resolve(categoryName).name
        }
    }

    companion object {
        val ANTI_CANCER_ATC_CODES = setOf("L01", "L02", "H01CC", "H01CA", "G03XA")

        val MEDICATION_CATEGORIES_TO_TREATMENT_CATEGORY = mapOf(
            "Chemotherapy" to setOf(TreatmentCategory.CHEMOTHERAPY),
            "Endocrine therapy" to setOf(TreatmentCategory.HORMONE_THERAPY),
            "Immunotherapy" to setOf(TreatmentCategory.IMMUNOTHERAPY),
            "Anticancer" to TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES,
            "Antineoplastic agents" to TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES
        )

        val MEDICATION_CATEGORIES_TO_DRUG_TYPES = mapOf(
            "Gonadotropin agonists" to setOf(DrugType.GONADOTROPIN_AGONIST),
            "Gonadotropin antagonists" to setOf(DrugType.GONADOTROPIN_ANTAGONIST),
            "Hypomethylating agents" to setOf(DrugType.DNMT_INHIBITOR),
            "Monoclonal antibodies and antibody drug conjugates" to setOf(
                DrugType.MONOCLONAL_ANTIBODY_TARGETED_THERAPY,
                DrugType.MONOCLONAL_ANTIBODY_IMMUNOTHERAPY,
                DrugType.MONOCLONAL_ANTIBODY_MMAE_CONJUGATE,
                DrugType.MONOCLONAL_ANTIBODY_SUPPORTIVE_TREATMENT,
                DrugType.ANTIBODY_DRUG_CONJUGATE_IMMUNOTHERAPY,
                DrugType.ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY
            ),
            "PARP inhibitors" to setOf(DrugType.PARP_INHIBITOR),
            "L01CD" to setOf(DrugType.TAXANE),
            "L02BB" to setOf(DrugType.ANTI_ANDROGEN),
            "L01A" to setOf(DrugType.ALKYLATING_AGENT),
            "L01XL" to setOf(DrugType.ANTI_CLDN6_CAR_T, DrugType.HER2_CAR_T),
            "L01E" to setOf(DrugType.TYROSINE_KINASE_INHIBITOR),
            "L01EB" to setOf(DrugType.EGFR_TYROSINE_KINASE_INHIBITOR),
            "L01FE" to setOf(DrugType.EGFR_INHIBITOR),
            "Nitrosoureas" to setOf(DrugType.NITROSOUREAS)
        )

        fun create(atcTree: AtcTree): MedicationCategories {
            return MedicationCategories(
                mapOf(
                    "Anticancer" to convertToAtcLevel(ANTI_CANCER_ATC_CODES, atcTree),
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
                    "Gonadotropin agonists" to convertToAtcLevel(setOf("H01CA", "L02AE"), atcTree),
                    "Gonadotropin antagonists" to convertToAtcLevel(setOf("H01CC", "G03XA"), atcTree),
                    "Hypomethylating agents" to convertToAtcLevel(setOf("L01BC07", "L01BC08"), atcTree),
                    "Immunostimulants" to convertToAtcLevel(setOf("L03"), atcTree),
                    "Immunosuppressants" to convertToAtcLevel(setOf("L04"), atcTree),
                    "Immunotherapy" to convertToAtcLevel(setOf("L01FF", "L01FX04"), atcTree),
                    "Monoclonal antibodies and antibody drug conjugates" to convertToAtcLevel(setOf("L01F"), atcTree),
                    "Nitrosoureas" to convertToAtcLevel(setOf("L01AD"), atcTree),
                    "NSAIDs" to convertToAtcLevel(setOf("M01A"), atcTree),
                    "Ophthalmic steroids" to convertToAtcLevel(setOf("S01BA", "S01BB", "S01CA", "S01CB"), atcTree),
                    "Opioids" to convertToAtcLevel(setOf("N02A"), atcTree),
                    "Other antianemic preparations" to convertToAtcLevel(setOf("B03X"), atcTree),
                    "PARP inhibitors" to convertToAtcLevel(setOf("L01XK"), atcTree),
                    "Platelet aggregation inhibitors" to convertToAtcLevel(setOf("B01AC"), atcTree),
                    "RANKL targeting agents" to convertToAtcLevel(setOf("M05BX04"), atcTree),
                    "Systemic antibiotics" to convertToAtcLevel(systemicAntibiotics, atcTree),
                    "Systemic antimicrobials" to convertToAtcLevel(systemicAntimicrobials, atcTree),
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

        fun isAntiCancerMedication(atcCode: String?): Boolean {
            return ANTI_CANCER_ATC_CODES.any { antiCancerCode -> atcCode?.startsWith(antiCancerCode) == true } && atcCode?.startsWith("L01XD") != true
        }
    }
}