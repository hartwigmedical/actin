package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TestTrialFactory
import com.hartwig.actin.datamodel.trial.TestTrialFactory.createMinimalTestTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityRuleUsageEvaluatorTest {

    @Test
    fun `Should evaluate eligibility rule usage`() {
        val trials = listOf(TestTrialFactory.createMinimalTestTrial(), TestTrialFactory.createProperTestTrial())
        val expectedUnusedRule = EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X
        val expectedUsedRules = setOf(
            EligibilityRule.IS_AT_LEAST_X_YEARS_OLD,
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS,
            EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES,
            EligibilityRule.NOT
        )
        val unusedRulesToKeep = EligibilityRule.entries.toSet() - expectedUnusedRule - expectedUsedRules
        assertThat(EligibilityRuleUsageEvaluator.evaluate(trials, unusedRulesToKeep)).containsExactly(expectedUnusedRule)
    }

    @Test
    fun `Should generate list of IHC-adjacent proteins based on parameters and IHC metadata`() {
        val minimal = createMinimalTestTrial()
        val trials = listOf(
            createMinimalTestTrial().copy(
                identification = minimal.identification.copy(
                    acronym = "TEST-TRIAL",
                    title = "This is an ACTIN test trial",
                    locations = setOf("Amsterdam UMC", "Antoni van Leeuwenhoek")
                ),
                generalEligibility = listOf(
                    Eligibility(
                        function = EligibilityFunction(rule = EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC, parameters = listOf("ABC")),
                        references = setOf(CriterionReference(id = "I-01", text = "ref 01"))
                    ),
                    Eligibility(
                        function = EligibilityFunction(
                            rule = EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y,
                            parameters = listOf("DEF", "1")
                        ),
                        references = setOf(CriterionReference(id = "I-02", text = "ref 02"))
                    ),
                    Eligibility(
                        function = EligibilityFunction(rule = EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X, parameters = listOf("1")),
                        references = setOf(CriterionReference(id = "I-03", text = "ref 03"))
                    )
                )
            )
        )
        assertThat(EligibilityRuleUsageEvaluator.extractIhcProteins(trials)).containsOnly("ABC", "DEF", "PD_L1")
    }
}