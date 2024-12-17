package com.hartwig.actin.algo.evaluation.intolerance

import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IntoleranceFunctionsTest {

    private val icdModel =
        TestIcdFactory.createModelWithSpecificNodes(listOf("targetMain", "targetExtension", "targetMainParent", "targetExtensionParent"))
    private val correctMainCode = "targetMainParentCode"
    private val childOfCorrectMainCode = "targetMainCode"
    private val correctExtensionCode = "targetExtensionParentCode"
    private val childOfCorrectExtensionCode = "targetExtensionCode"

    @Test
    fun `Should filter out intolerance not matching to requested main code`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = "wrongMainCode")
        evaluateSelection(OtherConditionTestFactory.withPriorOtherCondition(condition), emptyList(), emptyList())
    }

    @Test
    fun `Should filter out intolerance not matching to requested extension code when extension code match is requested`() {
        val intolerance = OtherConditionTestFactory.intolerance(icdMainCode = correctMainCode, icdExtensionCode = "wrongExtensionCode")
        evaluateSelection(OtherConditionTestFactory.withIntolerances(listOf(intolerance)), emptyList(), emptyList())
    }

    @Test
    fun `Should add all conditions matching main code to full matches when extension code is not requested`() {
        val intolerances = listOf("some code", null)
            .map { OtherConditionTestFactory.intolerance(icdMainCode = correctMainCode, icdExtensionCode = it) }
        evaluateSelection(OtherConditionTestFactory.withIntolerances(intolerances), intolerances, emptyList(), checkExtension = false)
    }

    @Test
    fun `Should add conditions matching main code but with unknown extension code to mainCodeMatchesWithUnknownExtension`() {
        val intolerances = listOf(OtherConditionTestFactory.intolerance(icdMainCode = correctMainCode, icdExtensionCode = null))
        evaluateSelection(OtherConditionTestFactory.withIntolerances(intolerances), emptyList(), intolerances)
    }

    @Test
    fun `Should match on correct child codes`() {
        val intolerances = listOf(
            OtherConditionTestFactory.intolerance(
                icdMainCode = childOfCorrectMainCode,
                icdExtensionCode = childOfCorrectExtensionCode
            )
        )
        evaluateSelection(OtherConditionTestFactory.withIntolerances(intolerances), intolerances, emptyList())
    }

    private fun evaluateSelection(
        record: PatientRecord,
        fullMatches: List<Intolerance>,
        mainCodeMatchesWithUnknownExtension: List<Intolerance>,
        checkExtension: Boolean = true
    ) {
        val extension = if (checkExtension) correctExtensionCode else null
        val result = IntoleranceFunctions.findIntoleranceMatchingAnyIcdCode(
            icdModel,
            record,
            setOf(IcdCode(correctMainCode, extension)),
        )
        assertThat(result.fullMatches).isEqualTo(fullMatches)
        assertThat(result.mainCodeMatchesWithUnknownExtension).isEqualTo(mainCodeMatchesWithUnknownExtension)
    }
}