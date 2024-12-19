package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.IcdCodeHolder
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import org.assertj.core.api.Assertions.assertThat
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
    fun `Should handle title validation correctly for regular and extended titles`() {
        assertThat(icdModel.isValidIcdTitle("targetMainTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("targetExtensionTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle&targetExtensionTitle")).isTrue()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle|targetExtensionTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("invalidTitle")).isFalse()
        assertThat(icdModel.isValidIcdTitle("targetMainTitle&invalidTitle")).isFalse()
    }

    @Test
    fun `Should successfully resolve codes for extended and non-extended titles`() {
        assertThat(icdModel.resolveCodeForTitle("targetMainTitle&targetExtensionTitle"))
            .isEqualTo(IcdCode("targetMainCode", "targetExtensionCode"))
        assertThat(icdModel.resolveCodeForTitle("targetMainTitle")).isEqualTo(IcdCode("targetMainCode", null))
    }

    @Test
    fun `Should successfully resolve code with parents`() {
        assertThat(icdModel.returnCodeWithParents("targetMainCode")).containsExactly("targetMainParentCode", "targetMainCode")
        assertThat(icdModel.returnCodeWithParents("targetExtensionCode")).containsExactly(
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
    fun `Should filter out condition not matching to requested main code`() {
        evaluateCodeMatching(createIcdEntityList(IcdCode("wrongMainCode", null)), emptyList(), emptyList())
    }

    @Test
    fun `Should filter out condition not matching to requested extension code when extension code match is requested`() {
        evaluateCodeMatching(createIcdEntityList(IcdCode(correctMainCode, "wrongExtensionCode")), emptyList(), emptyList())
    }

    @Test
    fun `Should add all conditions matching main code to full matches when extension code is not requested`() {
        val (randomExtension, noExtension) = listOf("random", null).map { createIcdEntityList(IcdCode(correctMainCode, it)) }
        listOf(randomExtension, noExtension).forEach { evaluateCodeMatching(it, it, emptyList(), checkExtension = false) }
    }

    @Test
    fun `Should add conditions matching main code but with unknown extension code to mainCodeMatchesWithUnknownExtension`() {
        val icdEntities = createIcdEntityList(IcdCode(correctMainCode, null))
        evaluateCodeMatching(icdEntities, emptyList(), icdEntities)
    }

    @Test
    fun `Should match on correct child codes`() {
        val icdEntities = createIcdEntityList(IcdCode(childOfCorrectMainCode, childOfCorrectExtensionCode))
        evaluateCodeMatching(icdEntities, icdEntities, emptyList())
    }

    private fun evaluateCodeMatching(
        input: List<IcdCodeHolder>,
        expectedFullMatches: List<IcdCodeHolder>,
        expectedMainCodeMatchesWithUnknownExtension: List<IcdCodeHolder>,
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

    private fun createIcdEntityList(icdCode: IcdCode): List<IcdCodeHolder> {
        return listOf(
            PriorOtherCondition("name", icdCode = icdCode, isContraindicationForTherapy = true),
            Toxicity("name", icdCode, date, ToxicitySource.EHR, 3, date),
            Intolerance("name", icdCode = icdCode),
            Complication("name", icdCode = icdCode, null, null)
        )
    }
}