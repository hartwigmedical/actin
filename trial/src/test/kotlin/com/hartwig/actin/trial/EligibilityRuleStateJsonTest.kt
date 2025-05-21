package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.EligibilityRuleState
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class EligibilityRuleStateJsonTest {

    private val trialDirectory = resourceOnClasspath("rules")
    private val outputFileName: String = "rule-state-result.json"

    @After
    fun cleanDirectory() {
        Files.delete(Path.of(trialDirectory, outputFileName))
    }

    @Test
    fun `Should write to valid JSON`() {
        val eligibilityRules = EligibilityRule.entries.sorted().map {
            EligibilityRuleState.unused(it)
        }
        EligibilityRuleStateJson.write(eligibilityRules, trialDirectory)
        val expected = Files.readString(Path.of(trialDirectory, "valid_eligibility_rules.json"))
        val actual = Files.readString(Path.of(trialDirectory, outputFileName))
        assertThat(expected.normalise()).isEqualTo(actual.normalise())
    }

    private fun String.normalise() = this.replace("\\s+".toRegex(), "")
}