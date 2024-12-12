package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorOtherConditionFunctionsTest {

    private val icdModel =
        TestIcdFactory.createModelWithSpecificNodes(listOf("targetMain", "targetExtension", "targetMainParent", "targetExtensionParent"))
    private val correctMainCode = "targetMainParentCode"
    private val childOfCorrectMainCode = "targetMainCode"
    private val correctExtensionCode = "targetExtensionParentCode"
    private val childOfCorrectExtensionCode = "targetExtensionCode"

    @Test
    fun `Should filter out condition not matching to requested main code`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = "wrongMainCode")
        evaluateSelection(OtherConditionTestFactory.withPriorOtherCondition(condition), emptyList(), emptyList())
    }

    @Test
    fun `Should filter out condition not matching to requested extension code when extension code match is requested`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = correctMainCode, icdExtensionCode = "wrongExtensionCode")
        evaluateSelection(OtherConditionTestFactory.withPriorOtherCondition(condition), emptyList(), emptyList())
    }

    @Test
    fun `Should add all conditions matching main code to full matches when extension code is not requested`() {
        val conditions = listOf("some code", null)
            .map { OtherConditionTestFactory.priorOtherCondition(icdMainCode = correctMainCode, icdExtensionCode = it) }
        evaluateSelection(OtherConditionTestFactory.withPriorOtherConditions(conditions), conditions, emptyList(), checkExtension = false)
    }

    @Test
    fun `Should add conditions matching main code but with unknown extension code to mainCodeMatchesWithUnknownExtension`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = correctMainCode, icdExtensionCode = null)
        evaluateSelection(OtherConditionTestFactory.withPriorOtherCondition(condition), emptyList(), listOf(condition))
    }

    @Test
    fun `Should match on correct child codes`() {
        val condition =
            OtherConditionTestFactory.priorOtherCondition(icdMainCode = childOfCorrectMainCode, icdExtensionCode = childOfCorrectExtensionCode)
        evaluateSelection(OtherConditionTestFactory.withPriorOtherCondition(condition), listOf(condition), emptyList())
    }

    private fun evaluateSelection(
        record: PatientRecord,
        fullMatches: List<PriorOtherCondition>,
        mainCodeMatchesWithUnknownExtension: List<PriorOtherCondition>,
        checkExtension: Boolean = true
    ) {
        val extension = if (checkExtension) correctExtensionCode else null
        val result = PriorOtherConditionFunctions.findPriorOtherConditionsMatchingAnyIcdCode(
            icdModel,
            record,
            setOf(IcdCode(correctMainCode, extension)),
        )
        assertThat(result.fullMatches).isEqualTo(fullMatches)
        assertThat(result.mainCodeMatchesWithUnknownExtension).isEqualTo(mainCodeMatchesWithUnknownExtension)
    }
}