package com.hartwig.actin.trial.input

import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.trial.input.composite.CompositeInput
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput

class ParameterizedFunctionTestFactory(private val doidTermToUse: String, private val icdTitleToUse: String) {

    private val arbitraryRule: EligibilityRule = firstNonComposite()

    fun create(rule: EligibilityRule): EligibilityFunction {
        return EligibilityFunction(rule, createTestParameters(rule))
    }

    private fun createTestParameters(rule: EligibilityRule): List<Any> {
        return if (CompositeRules.isComposite(rule)) {
            when (CompositeRules.inputsForCompositeRule(rule)) {
                CompositeInput.EXACTLY_1 -> {
                    listOf(create(arbitraryRule))
                }

                CompositeInput.AT_LEAST_2 -> {
                    listOf(create(arbitraryRule), create(arbitraryRule))
                }
            }
        } else {
            createForInputs(rule.input!!)
        }
    }

    private fun createForInputs(input: FunctionInput): List<Any> {
        return when (input) {
            FunctionInput.NONE -> {
                emptyList()
            }

            FunctionInput.ONE_ALBI_GRADE -> {
                listOf("1")
            }

            FunctionInput.ONE_INTEGER, FunctionInput.ONE_DOUBLE -> {
                listOf("1")
            }

            FunctionInput.MANY_INTEGERS -> {
                listOf("1;2")
            }

            FunctionInput.TWO_INTEGERS, FunctionInput.TWO_DOUBLES -> {
                listOf("1", "2")
            }

            FunctionInput.ONE_DOUBLE_ONE_GENDER -> {
                listOf("1", "female")
            }

            FunctionInput.ONE_SYSTEMIC_TREATMENT -> {
                listOf("CAPECITABINE+OXALIPLATIN")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE -> {
                listOf(TreatmentCategory.IMMUNOTHERAPY.display())
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                listOf(TreatmentCategory.IMMUNOTHERAPY.display(), DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1)
            }

            FunctionInput.TWO_TREATMENT_CATEGORIES_MANY_TYPES -> {
                listOf(
                    TreatmentCategory.IMMUNOTHERAPY.display(),
                    DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1,
                    TreatmentCategory.CHEMOTHERAPY.display(),
                    DrugType.PLATINUM_COMPOUND.toString() + ";" + DrugType.ANTIMETABOLITE
                )
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER -> {
                listOf(OtherTreatmentType.ALLOGENIC.display(), "1")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER -> {
                listOf(TreatmentCategory.IMMUNOTHERAPY.display(), DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1, "1")
            }

            FunctionInput.ONE_TREATMENT_TYPE_ONE_INTEGER -> {
                listOf(DrugType.ANTI_PD_L1.toString(), "1")
            }

            FunctionInput.MANY_TREATMENT_CATEGORIES -> {
                listOf("${TreatmentCategory.IMMUNOTHERAPY.display()};${TreatmentCategory.CHEMOTHERAPY.display()}")
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT -> {
                listOf("CAPECITABINE+OXALIPLATIN")
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER -> {
                listOf("CAPECITABINE+OXALIPLATIN", "1")
            }

            FunctionInput.ONE_SPECIFIC_DRUG_ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                listOf(
                    "CAPECITABINE",
                    TreatmentCategory.CHEMOTHERAPY.display(),
                    "${DrugType.ALKYLATING_AGENT};${DrugType.ANTIMETABOLITE}"
                )
            }

            FunctionInput.MANY_SPECIFIC_TREATMENTS -> {
                listOf("CAPECITABINE+OXALIPLATIN;CAPECITABINE+OXALIPLATIN")
            }

            FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS -> {
                listOf("CAPECITABINE+OXALIPLATIN;CAPECITABINE+OXALIPLATIN", "1", "2")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS -> {
                listOf(TreatmentCategory.CHEMOTHERAPY.display(), "CAPECITABINE;OXALIPLATIN")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_MANY_DRUGS -> {
                listOf(
                    TreatmentCategory.CHEMOTHERAPY.display(),
                    "${DrugType.ALKYLATING_AGENT};${DrugType.ANTIMETABOLITE}",
                    "CAPECITABINE;OXALIPLATIN"
                )
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS -> {
                listOf(
                    TreatmentCategory.CHEMOTHERAPY.display(),
                    "${Intent.ADJUVANT};${Intent.PALLIATIVE}"
                )
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_INTENTS_ONE_INTEGER -> {
                listOf(
                    TreatmentCategory.CHEMOTHERAPY.display(),
                    "${Intent.ADJUVANT};${Intent.PALLIATIVE}",
                    "1"
                )
            }

            FunctionInput.MANY_DRUGS -> {
                listOf("CAPECITABINE;OXALIPLATIN")
            }

            FunctionInput.MANY_DRUGS_ONE_INTEGER -> {
                listOf("CAPECITABINE;OXALIPLATIN", "1")
            }

            FunctionInput.MANY_DRUGS_TWO_INTEGERS -> {
                listOf("CAPECITABINE;OXALIPLATIN", "1", "5")
            }

            FunctionInput.ONE_ICD_TITLE -> {
                listOf(icdTitleToUse)
            }

            FunctionInput.MANY_ICD_TITLES -> {
                listOf("$icdTitleToUse;$icdTitleToUse")
            }

            FunctionInput.ONE_NYHA_CLASS -> {
                listOf("I")
            }

            FunctionInput.ONE_TUMOR_TYPE -> {
                listOf(TumorTypeInput.SQUAMOUS_CELL_CARCINOMA.display())
            }

            FunctionInput.ONE_STRING -> {
                listOf("string")
            }

            FunctionInput.TWO_STRINGS -> {
                listOf("string1", "string2")
            }

            FunctionInput.ONE_STRING_ONE_INTEGER -> {
                listOf("string", "1")
            }

            FunctionInput.MANY_STRINGS -> {
                listOf("string1;string2")
            }

            FunctionInput.MANY_BODY_LOCATIONS -> {
                listOf("${BodyLocationCategory.LIVER};${BodyLocationCategory.LUNG}")
            }

            FunctionInput.ONE_INTEGER_ONE_BODY_LOCATION -> {
                listOf("1", BodyLocationCategory.LIVER.toString())
            }

            FunctionInput.ONE_INTEGER_MANY_DOID_TERMS -> {
                listOf("1", "$doidTermToUse;$doidTermToUse")
            }

            FunctionInput.ONE_INTEGER_MANY_ICD_TITLES -> {
                listOf("1", "$icdTitleToUse;$icdTitleToUse")
            }

            FunctionInput.MANY_TUMOR_STAGES -> {
                listOf("I;II")
            }

            FunctionInput.MANY_HLA_ALLELES -> {
                listOf("A*02:01;A*02:02")
            }

            FunctionInput.ONE_HLA_GROUP -> {
                listOf("A*02")
            }

            FunctionInput.ONE_HAPLOTYPE -> {
                listOf("*1_HOM")
            }

            FunctionInput.ONE_GENE -> {
                listOf("gene")
            }

            FunctionInput.ONE_PROTEIN_ONE_STRING -> {
                listOf("protein", "string")
            }

            FunctionInput.ONE_GENE_ONE_INTEGER -> {
                listOf("gene", "1")
            }

            FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE -> {
                listOf("gene", "1", "INDEL")
            }

            FunctionInput.ONE_GENE_TWO_INTEGERS -> {
                listOf("gene", "1", "2")
            }

            FunctionInput.ONE_GENE_MANY_CODONS -> {
                listOf("gene", "V600;V601")
            }

            FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS -> {
                listOf("gene", "V600E;V601E")
            }

            FunctionInput.MANY_GENES -> {
                listOf("gene1;gene2")
            }

            FunctionInput.ONE_DOID_TERM -> {
                listOf(doidTermToUse)
            }

            FunctionInput.ONE_ICD_TITLE_ONE_INTEGER -> {
                listOf(icdTitleToUse, "1")
            }

            FunctionInput.MANY_DOID_TERMS -> {
                listOf("$doidTermToUse; $doidTermToUse")
            }

            FunctionInput.ONE_RECEPTOR_TYPE -> {
                listOf("ER")
            }

            FunctionInput.MANY_INTENTS_ONE_INTEGER -> {
                listOf(Intent.ADJUVANT.display() + ";" + Intent.NEOADJUVANT.display(), "1")
            }

            FunctionInput.MANY_INTENTS -> {
                listOf(Intent.ADJUVANT.display() + ";" + Intent.NEOADJUVANT.display())
            }

            FunctionInput.ONE_MEDICATION_CATEGORY -> {
                listOf(ATC_CODE_1)
            }

            FunctionInput.ONE_MEDICATION_CATEGORY_ONE_INTEGER -> {
                listOf(ATC_CODE_1, "1")
            }

            FunctionInput.MANY_MEDICATION_CATEGORIES_ONE_INTEGER -> {
                listOf("$ATC_CODE_1;$ATC_CODE_2", "1")
            }

            FunctionInput.MANY_MEDICATION_CATEGORIES_TWO_INTEGERS -> {
                listOf("$ATC_CODE_1;$ATC_CODE_2", "1", "2")
            }

            FunctionInput.ONE_CYP -> {
                listOf("CYP3A4_5")
            }

            FunctionInput.ONE_CYP_ONE_INTEGER -> {
                listOf("CYP3A4_5", "1")
            }

            FunctionInput.ONE_TRANSPORTER -> {
                listOf("OATP1B1")
            }

            FunctionInput.ONE_PROTEIN -> {
                listOf("FGFR2b")
            }

            FunctionInput.ONE_PROTEIN_ONE_INTEGER -> {
                listOf("FGFR2b", "1")
            }

            FunctionInput.MANY_TNM_T -> {
                listOf("T2A")
            }
        }
    }

    private fun firstNonComposite(): EligibilityRule {
        return EligibilityRule.entries.find { rule ->
            !CompositeRules.isComposite(rule)
        } ?: throw IllegalStateException("Only composite functions defined!")
    }
}
