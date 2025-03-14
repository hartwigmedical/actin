package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.FunctionInput
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.trial.input.composite.CompositeRules
import org.apache.logging.log4j.LogManager

object EligibilityRuleUsageEvaluator {

    private val LOGGER = LogManager.getLogger(EligibilityRuleUsageEvaluator::class.java)

    fun evaluate(trials: List<Trial>, unusedRulesToKeep: Set<EligibilityRule>): Set<EligibilityRule> {
        val usedRules = trials.flatMap { it.generalEligibility + it.cohorts.flatMap(Cohort::eligibility) }
            .flatMap { extractRules(listOf(it.function)) }
            .toSet()
        val unusedRules = EligibilityRule.values().toSet() - usedRules

        val configuredUnusedRulesThatAreUsed = unusedRulesToKeep - unusedRules
        if (configuredUnusedRulesThatAreUsed.isNotEmpty()) {
            LOGGER.warn(" Found ${configuredUnusedRulesThatAreUsed.size} referenced eligibility rules that are configured as unused.")
            for (rule in configuredUnusedRulesThatAreUsed) {
                LOGGER.warn("  '${rule}' used in at least one trial or cohort but configured as unused")
            }
        }

        val unexpectedUnusedRules = unusedRules - unusedRulesToKeep
        if (unexpectedUnusedRules.isNotEmpty()) {
            LOGGER.warn(" Found ${unexpectedUnusedRules.size} unused eligibility rules.")
            for (rule in unexpectedUnusedRules) {
                LOGGER.warn("  '${rule}' not used in any trial or cohort")
            }
        } else {
            LOGGER.info(" Found no unused eligibility rules to curate.")
        }
        return unexpectedUnusedRules
    }

    fun extractIhcProteinParameters(trials: List<Trial>): Set<String> {
        return collectFunctions(trials.flatMap { it.generalEligibility }.map { it.function })
            .filter { it.rule.input in (listOf(FunctionInput.ONE_PROTEIN, FunctionInput.ONE_PROTEIN_ONE_INTEGER)) }
            .map { it.parameters.first().toString() }.toSet()
    }

    private fun extractRules(
        functions: List<EligibilityFunction>
    ): List<EligibilityRule> {
        return collectFunctions(functions).map { it.rule }
    }

    private tailrec fun collectFunctions(
        functions: List<EligibilityFunction>, accumulated: List<EligibilityFunction> = emptyList()
    ): List<EligibilityFunction> {
        if (functions.isEmpty()) {
            return accumulated
        }
        val function = functions.first()
        val functionsToAdd = if (CompositeRules.isComposite(function.rule)) {
            function.parameters.map { it as EligibilityFunction }
        } else emptyList()

        return collectFunctions(functionsToAdd + functions.drop(1), accumulated + function)
    }
}