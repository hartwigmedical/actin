package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import org.apache.logging.log4j.LogManager

class WashoutRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS to hasRecentlyReceivedCancerTherapyOfNamesCreator(),
            EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS to hasRecentlyReceivedCancerTherapyOfCategoriesCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS to hasRecentlyReceivedRadiotherapyCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS to hasRecentlyReceivedAnyCancerTherapyCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS to hasRecentlyReceivedAnyCancerTherapyButSomeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(),
            EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL to willRequireAnticancerTherapyCreator(),
        )
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsOneIntegerInput(function)
            createReceivedCancerTherapyOfNameFunction(input.strings(), input.integer())
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            createReceivedCancerTherapyOfNameFunction(input.strings(), input.integer1())
        }
    }

    private fun createReceivedCancerTherapyOfNameFunction(names: List<String>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        return HasRecentlyReceivedCancerTherapyOfName(names.toSet(), interpreter)
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsOneIntegerInput(function)
            createReceivedCancerTherapyOfCategoryFunction(input.strings(), input.integer())
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            createReceivedCancerTherapyOfCategoryFunction(input.strings(), input.integer1())
        }
    }

    private fun createReceivedCancerTherapyOfCategoryFunction(categoryInputs: List<String>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)

        // TODO: Dynamically resolve inputs to names and/or categories
        val names = determineNames(categoryInputs)
        return if (names != null) {
            HasRecentlyReceivedCancerTherapyOfName(names, interpreter)
        } else {
            val categories = determineCategories(categoryInputs)
            HasRecentlyReceivedCancerTherapyOfCategory(categories, interpreter)
        }
    }

    private fun hasRecentlyReceivedRadiotherapyCreator(): FunctionCreator {
        return FunctionCreator {
            HasRecentlyReceivedRadiotherapy(
                referenceDateProvider().year(),
                referenceDateProvider().month()
            )
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minWeeks = functionInputResolver().createOneIntegerInput(function)
            createReceivedAnyCancerTherapyFunction(minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            createReceivedAnyCancerTherapyFunction(input.integer1())
        }
    }

    private fun createReceivedAnyCancerTherapyFunction(minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        return HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, interpreter)
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsOneIntegerInput(function)
            createReceivedAnyCancerTherapyButSomeFunction(input.strings(), input.integer())
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            createReceivedAnyCancerTherapyButSomeFunction(input.strings(), input.integer1())
        }
    }

    private fun createReceivedAnyCancerTherapyButSomeFunction(categoriesToIgnore: List<String>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val categoriesToConsider = ALL_ANTI_CANCER_CATEGORIES - determineCategories(categoriesToIgnore)
        return HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, interpreter)
    }

    private fun willRequireAnticancerTherapyCreator(): FunctionCreator {
        return FunctionCreator { WillRequireAnticancerTherapy() }
    }

    private fun createInterpreterForWashout(inputWeeks: Int): MedicationStatusInterpreter {
        val minDate = referenceDateProvider().date().minusWeeks(inputWeeks.toLong()).plusWeeks(2)
        return MedicationStatusInterpreterOnEvaluationDate(minDate)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(WashoutRuleMapper::class.java)
        private val MEDICATIONS_FOR_MAIN_CATEGORY: Map<String, Set<String>> = mapOf(
            "Immunotherapy" to setOf("Pembrolizumab", "Nivolumab", "Ipilimumab", "Cemiplimab", "Avelumab"),
            "Hypomethylating agents" to setOf("Azacitidine", "Decitabine")
        )
        private val chemotherapyCategories = setOf("Platinum compounds", "Pyrimidine analogues", "Taxanes", "Alkylating agents")
        private val endocrineTherapyCategories = setOf("Endocrine therapy")
        private val gonadorelinCategories = setOf(
            "Anti-gonadotropin-releasing hormones",
            "Gonadotropin-releasing hormones",
            "Antigonadotropins and similar agents",
            "Gonadotropin releasing hormone analogues"
        )
        private val immunosuppressantCategories = setOf("Immunosuppressants")
        private val parpInhibitorsCategories = setOf("Poly (ADP-ribose) polymerase (PARP) inhibitors")

        private val CATEGORIES_PER_MAIN_CATEGORY: Map<String, Set<String>> = mapOf(
            "Chemotherapy" to chemotherapyCategories,
            "Endocrine therapy" to endocrineTherapyCategories,
            "Gonadorelin" to gonadorelinCategories,
            "Immunosuppressants" to immunosuppressantCategories,
            "PARP inhibitors" to parpInhibitorsCategories
        )

        private val ALL_ANTI_CANCER_CATEGORIES =
            setOf("Antineoplastic agents", "Endocrine therapy", "Immunosuppressants") + gonadorelinCategories

        private fun determineCategories(inputs: List<String>): Set<String> {
            return inputs.flatMap { CATEGORIES_PER_MAIN_CATEGORY[it] ?: setOf(it) }.toSet()
        }

        private fun determineNames(inputs: List<String>): Set<String>? {
            val result = MEDICATIONS_FOR_MAIN_CATEGORY[inputs[0]]
            if (result != null && inputs.size > 1) {
                LOGGER.warn(
                    "Multiple inputs configured in washout while first input resolves to explicit set of medication names: {}",
                    inputs
                )
            }
            return result
        }
    }
}