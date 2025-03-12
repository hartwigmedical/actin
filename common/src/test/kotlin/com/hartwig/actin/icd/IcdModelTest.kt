package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.Test
import java.time.LocalDate

class IcdModelTest {

    private val date = LocalDate.of(2024, 12, 1)
    private val icdModel =
        TestIcdFactory.createModelWithSpecificNodes(listOf("targetMain", "targetExtension", "targetMainParent", "targetExtensionParent"))
    private val correctMainCode = "targetMainParentCode"
    private val childOfCorrectMainCode = "targetMainCode"
    private val correctExtensionCode = "targetExtensionParentCode"
    private val childOfCorrectExtensionCode = "targetExtensionCode"

    @Test
    fun `Should return true for valid ICD title`() {
        assertThat(icdModel.isValidIcdTitle("targetMainTitle&targetExtensionTitle")).isTrue()
    }

    @Test
    fun `Should ignore case in title validation`() {
        assertThat(icdModel.isValidIcdTitle("TargetMainTitle&targetEXTENSIONTitle")).isTrue()
    }

    @Test
    fun `Should return false for invalid ICD title`() {
        assertThat(icdModel.isValidIcdTitle("invalidTitle")).isFalse()
    }

    @Test
    fun `Should return true for valid ICD code`() {
        assertThat(icdModel.isValidIcdCode("targetMainCode&targetExtensionCode")).isTrue()
    }

    @Test
    fun `Should return false for invalid ICD code`() {
        assertThat(icdModel.isValidIcdCode("invalidCode")).isFalse()
    }

    @Test
    fun `Should handle title validation correctly for regular and extended titles`() {
        assertThat(icdModel.isValidIcdTitle("targetMainTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("targetExtensionTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle&targetExtensionTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle|targetExtensionTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("invalidTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle&invalidTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle&targetExtensionTitle&targetExtensionParentTitle")).isFalse()
    }

    @Test
    fun `Should successfully resolve codes for extended and non-extended titles`() {
        assertThat(icdModel.resolveCodeForTitle("targetMainTitle&targetExtensionTitle"))
            .isEqualTo(IcdCode("targetMainCode", "targetExtensionCode"))
        assertThat(icdModel.resolveCodeForTitle("targetMainTitle")).isEqualTo(IcdCode("targetMainCode", null))
    }

    @Test
    fun `Should ignore case in code to title resolution`() {
        assertThat(icdModel.resolveCodeForTitle("TargetMainTitle&targetEXTENSIONTitle"))
            .isEqualTo(IcdCode("targetMainCode", "targetExtensionCode"))
    }

    @Test
    fun `Should successfully resolve code with parents`() {
        assertThat(icdModel.codeWithAllParents("targetMainCode")).containsExactly("targetMainParentCode", "targetMainCode")
        assertThat(icdModel.codeWithAllParents("targetExtensionCode")).containsExactly(
            "targetExtensionParentCode",
            "targetExtensionCode"
        )
    }

    @Test
    fun `Should correctly resolve titles for main and extension codes`() {
        assertThat(icdModel.resolveTitleForCode(IcdCode("targetMainCode", "targetExtensionCode")))
            .isEqualTo("targetMainTitle & targetExtensionTitle")
        assertThat(icdModel.resolveTitleForCode(IcdCode("targetMainCode", null))).isEqualTo("targetMainTitle")
    }

    @Test
    fun `Should correctly resolve title for code string input`() {
        assertThat(icdModel.resolveTitleForCodeString("targetMainCode&targetExtensionCode"))
            .isEqualTo("targetMainTitle&targetExtensionTitle")
    }

    @Test
    fun `Should throw error when code string contains invalid code or more than two codes`() {
        val tripleCode =  "targetMainCode&targetExtensionCode&targetExtensionCode"
        assertThatIllegalStateException()
            .isThrownBy { icdModel.resolveTitleForCodeString(tripleCode) }
            .withMessage("Invalid ICD code: $tripleCode")

        assertThatIllegalStateException().isThrownBy { icdModel.resolveTitleForCodeString("invalidCode") }
            .withMessage("Invalid ICD code: invalidCode")
    }

    @Test
    fun `Should filter out condition not matching to requested main code`() {
        evaluateFullIcdCodeMatching(createIcdEntityList(setOf(IcdCode("wrongMainCode", null))), emptyList(), emptyList())
    }

    @Test
    fun `Should filter out condition not matching to requested extension code when extension code match is requested`() {
        evaluateFullIcdCodeMatching(createIcdEntityList(setOf(IcdCode(correctMainCode, "wrongExtensionCode"))), emptyList(), emptyList())
    }

    @Test
    fun `Should add all conditions matching main code to full matches when extension code is not requested`() {
        listOf("random", null).map { createIcdEntityList(setOf(IcdCode(correctMainCode, it))) }
            .forEach { evaluateFullIcdCodeMatching(it, it, emptyList(), checkExtension = false) }
    }

    @Test
    fun `Should add conditions matching main code but with unknown extension code to mainCodeMatchesWithUnknownExtension`() {
        val icdEntities = createIcdEntityList(setOf(IcdCode(correctMainCode, null)))
        evaluateFullIcdCodeMatching(icdEntities, emptyList(), icdEntities)
    }

    @Test
    fun `Should match on correct child codes`() {
        val icdEntities = createIcdEntityList(setOf(IcdCode(childOfCorrectMainCode, childOfCorrectExtensionCode)))
        evaluateFullIcdCodeMatching(icdEntities, icdEntities, emptyList())
    }

    @Test
    fun `Should identify icd extension code matches`() {
        listOf(correctExtensionCode, childOfCorrectExtensionCode).forEach {
            val icdEntities = createIcdEntityList(setOf(IcdCode("mainCode", it)))
            assertThat(icdModel.findInstancesMatchingAnyExtensionCode(icdEntities, setOf(correctExtensionCode)))
                .containsExactlyElementsOf(icdEntities)
        }
    }

    private fun evaluateFullIcdCodeMatching(
        input: List<Comorbidity>,
        expectedFullMatches: List<Comorbidity>,
        expectedMainCodeMatchesWithUnknownExtension: List<Comorbidity>,
        checkExtension: Boolean = true
    ) {
        val extension = if (checkExtension) correctExtensionCode else null
        val result = icdModel.findInstancesMatchingAnyIcdCode(
            input,
            setOf(IcdCode(correctMainCode, extension)),
        )
        assertThat(result.fullMatches).isEqualTo(expectedFullMatches)
        assertThat(result.mainCodeMatchesWithUnknownExtension).isEqualTo(expectedMainCodeMatchesWithUnknownExtension)
    }

    private fun createIcdEntityList(icdCodes: Set<IcdCode>): List<Comorbidity> {
        return listOf(
            OtherCondition("name", icdCodes = icdCodes),
            Toxicity("name", icdCodes, date, ToxicitySource.EHR, 3, date),
            Intolerance("name", icdCodes = icdCodes),
            Complication("name", icdCodes = icdCodes)
        )
    }
}