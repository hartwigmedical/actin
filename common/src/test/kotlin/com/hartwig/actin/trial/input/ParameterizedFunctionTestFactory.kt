package com.hartwig.actin.trial.input

import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.input.composite.CompositeInput
import com.hartwig.actin.trial.input.composite.CompositeRules
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput
import com.hartwig.actin.trial.input.single.FunctionInput

class ParameterizedFunctionTestFactory(private val doidTermToUse: String) {

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
            createForInputs(FunctionInputMapping.RULE_INPUT_MAP[rule]!!)
        }
    }

    private fun createForInputs(input: FunctionInput): List<Any> {
        return when (input) {
            FunctionInput.NONE -> {
                emptyList()
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

            FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE -> {
                listOf(TreatmentCategory.IMMUNOTHERAPY.display())
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                listOf(TreatmentCategory.IMMUNOTHERAPY.display(), DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1)
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER -> {
                listOf(OtherTreatmentType.ALLOGENIC.display(), "1")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER -> {
                listOf(TreatmentCategory.IMMUNOTHERAPY.display(), DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1, "1")
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT -> {
                listOf("CAPECITABINE+OXALIPLATIN")
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER -> {
                listOf("CAPECITABINE+OXALIPLATIN", "1")
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                listOf(
                    "CAPECITABINE+OXALIPLATIN",
                    TreatmentCategory.CHEMOTHERAPY.display(),
                    "${DrugType.ALKYLATING_AGENT};${DrugType.ANTIMETABOLITE}"
                )
            }

            FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS -> {
                listOf("CAPECITABINE+OXALIPLATIN;CAPECITABINE+OXALIPLATIN", "1", "2")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS -> {
                listOf(TreatmentCategory.CHEMOTHERAPY.display(), "CAPECITABINE;OXALIPLATIN")
            }

            FunctionInput.MANY_DRUGS -> {
                listOf("CAPECITABINE;OXALIPLATIN")
            }

            FunctionInput.MANY_DRUGS_ONE_INTEGER -> {
                listOf("CAPECITABINE;OXALIPLATIN", "1")
            }

            FunctionInput.ONE_TUMOR_TYPE -> {
                listOf(TumorTypeInput.SQUAMOUS_CELL_CARCINOMA.display())
            }

            FunctionInput.ONE_STRING -> {
                listOf("string")
            }

            FunctionInput.ONE_STRING_ONE_INTEGER -> {
                listOf("string", "1")
            }

            FunctionInput.MANY_STRINGS_ONE_INTEGER -> {
                listOf("string1;string2", "1")
            }

            FunctionInput.MANY_STRINGS_TWO_INTEGERS -> {
                listOf("string1;string2", "1", "2")
            }

            FunctionInput.ONE_INTEGER_ONE_STRING -> {
                listOf("1", "string")
            }

            FunctionInput.ONE_INTEGER_MANY_STRINGS -> {
                listOf("1", "string1;string2")
            }

            FunctionInput.ONE_TUMOR_STAGE -> {
                listOf(TumorStage.I.display())
            }

            FunctionInput.MANY_TUMOR_STAGES -> {
                listOf("I;II")
            }

            FunctionInput.ONE_HLA_ALLELE -> {
                listOf("A*02:01")
            }

            FunctionInput.ONE_HAPLOTYPE -> {
                listOf("*1_HOM")
            }

            FunctionInput.ONE_GENE -> {
                listOf("gene")
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

            FunctionInput.ONE_DOID_TERM_ONE_INTEGER -> {
                listOf(doidTermToUse, "1")
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
        }
    }

    private fun firstNonComposite(): EligibilityRule {
        return EligibilityRule.values().find { rule ->
            !CompositeRules.isComposite(rule)
        } ?: throw IllegalStateException("Only composite functions defined!")
    }
}
