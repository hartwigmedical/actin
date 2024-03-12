package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.trial.datamodel.Cohort
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.Trial
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
            LOGGER.warn(
                " Found {} eligibility rules that are used while they are configured as unused.",
                configuredUnusedRulesThatAreUsed.size
            )
            for (rule in configuredUnusedRulesThatAreUsed) {
                LOGGER.warn("  '{}' used in at least one trial or cohort but configured as unused", rule.toString())
            }
        }

        val unexpectedUnusedRules = unusedRules - unusedRulesToKeep
        if (unexpectedUnusedRules.isNotEmpty()) {
            LOGGER.warn(" Found {} unused eligibility rules.", unexpectedUnusedRules.size)
            for (rule in unexpectedUnusedRules) {
                LOGGER.warn("  '{}' not used in any trial or cohort", rule.toString())
            }
        } else {
            LOGGER.info(" Found no unused eligibility rules to curate.")
        }
        return unexpectedUnusedRules
    }

    private tailrec fun extractRules(
        functions: List<EligibilityFunction>, accumulated: List<EligibilityRule> = emptyList()
    ): List<EligibilityRule> {
        if (functions.isEmpty()) {
            return accumulated
        }
        val function = functions.first()
        val functionsToAdd = if (CompositeRules.isComposite(function.rule)) {
            function.parameters.map { it as EligibilityFunction }
        } else emptyList()

        return extractRules(functionsToAdd + functions.drop(1), accumulated + function.rule)
    }
}