package com.hartwig.actin.treatment.input

import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction

class ParameterizedFunctionTestFactory(private val doidTermToUse: String) {
    fun create(rule: EligibilityRule): EligibilityFunction {
        return ImmutableEligibilityFunction.builder().rule(rule).parameters(createTestParameters(rule)).build()
    }

    private fun createTestParameters(rule: EligibilityRule): List<Any> {
        return if (CompositeRules.isComposite(rule)) {
            val inputs: CompositeInput = CompositeRules.inputsForCompositeRule(rule)
            if (inputs == CompositeInput.EXACTLY_1) {
                java.util.List.of<Any>(create(MOCK_RULE))
            } else if (inputs == CompositeInput.AT_LEAST_2) {
                java.util.List.of<Any>(
                    create(MOCK_RULE),
                    create(MOCK_RULE)
                )
            } else {
                throw IllegalStateException("Cannot interpret composite input: $inputs")
            }
        } else {
            createForInputs(FunctionInputMapping.RULE_INPUT_MAP[rule])
        }
    }

    private fun createForInputs(input: FunctionInput): List<Any> {
        return when (input) {
            FunctionInput.NONE -> {
                emptyList<Any>()
            }

            FunctionInput.ONE_INTEGER, FunctionInput.ONE_DOUBLE -> {
                listOf<Any>("1")
            }

            FunctionInput.MANY_INTEGERS -> listOf<Any>("1;2")
            FunctionInput.TWO_INTEGERS, FunctionInput.TWO_DOUBLES -> {
                listOf<Any>("1", "2")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE -> {
                java.util.List.of<Any>(TreatmentCategory.IMMUNOTHERAPY.display())
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES -> {
                java.util.List.of<Any>(
                    TreatmentCategory.IMMUNOTHERAPY.display(),
                    DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1
                )
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_OR_TYPE_ONE_INTEGER -> {
                java.util.List.of<Any>(OtherTreatmentType.ALLOGENIC.display(), "1")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_TYPES_ONE_INTEGER -> {
                java.util.List.of<Any>(
                    TreatmentCategory.IMMUNOTHERAPY.display(),
                    DrugType.ANTI_PD_L1.toString() + ";" + DrugType.ANTI_PD_1,
                    "1"
                )
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT -> {
                listOf<Any>("CAPECITABINE+OXALIPLATIN")
            }

            FunctionInput.ONE_SPECIFIC_TREATMENT_ONE_INTEGER -> {
                listOf<Any>("CAPECITABINE+OXALIPLATIN", "1")
            }

            FunctionInput.MANY_SPECIFIC_TREATMENTS_TWO_INTEGERS -> {
                listOf<Any>("CAPECITABINE+OXALIPLATIN;CAPECITABINE+OXALIPLATIN", "1", "2")
            }

            FunctionInput.ONE_TREATMENT_CATEGORY_MANY_DRUGS -> {
                java.util.List.of<Any>(TreatmentCategory.CHEMOTHERAPY.display(), "CAPECITABINE;OXALIPLATIN")
            }

            FunctionInput.MANY_DRUGS -> {
                listOf<Any>("CAPECITABINE;OXALIPLATIN")
            }

            FunctionInput.ONE_TUMOR_TYPE -> {
                java.util.List.of<Any>(TumorTypeInput.SQUAMOUS_CELL_CARCINOMA.display())
            }

            FunctionInput.ONE_STRING -> {
                listOf<Any>("string")
            }

            FunctionInput.ONE_STRING_ONE_INTEGER -> {
                listOf<Any>("string", "1")
            }

            FunctionInput.MANY_STRINGS_ONE_INTEGER -> {
                listOf<Any>("string1;string2", "1")
            }

            FunctionInput.MANY_STRINGS_TWO_INTEGERS -> {
                listOf<Any>("string1;string2", "1", "2")
            }

            FunctionInput.ONE_INTEGER_ONE_STRING -> {
                listOf<Any>("1", "string")
            }

            FunctionInput.ONE_INTEGER_MANY_STRINGS -> {
                listOf<Any>("1", "string1;string2")
            }

            FunctionInput.ONE_TUMOR_STAGE -> {
                java.util.List.of<Any>(TumorStage.I.display())
            }

            FunctionInput.ONE_HLA_ALLELE -> {
                listOf<Any>("A*02:01")
            }

            FunctionInput.ONE_GENE -> {
                listOf<Any>("gene")
            }

            FunctionInput.ONE_GENE_ONE_INTEGER -> {
                listOf<Any>("gene", "1")
            }

            FunctionInput.ONE_GENE_ONE_INTEGER_ONE_VARIANT_TYPE -> {
                listOf<Any>("gene", "1", "INDEL")
            }

            FunctionInput.ONE_GENE_TWO_INTEGERS -> {
                listOf<Any>("gene", "1", "2")
            }

            FunctionInput.ONE_GENE_MANY_CODONS -> {
                listOf<Any>("gene", "V600;V601")
            }

            FunctionInput.ONE_GENE_MANY_PROTEIN_IMPACTS -> {
                listOf<Any>("gene", "V600E;V601E")
            }

            FunctionInput.MANY_GENES -> {
                listOf<Any>("gene1;gene2")
            }

            FunctionInput.ONE_DOID_TERM -> {
                java.util.List.of<Any>(doidTermToUse)
            }

            else -> {
                throw IllegalStateException("Could not create inputs for $input")
            }
        }
    }

    companion object {
        private val MOCK_RULE: EligibilityRule = firstNonComposite()
        private fun firstNonComposite(): EligibilityRule {
            for (rule in EligibilityRule.values()) {
                if (!CompositeRules.isComposite(rule)) {
                    return rule
                }
            }
            throw IllegalStateException("Only composite functions defined!")
        }
    }
}
