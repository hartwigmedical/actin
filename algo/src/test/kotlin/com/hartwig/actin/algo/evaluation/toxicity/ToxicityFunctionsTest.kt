package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ToxicityFunctionsTest {

    private val referenceDate = LocalDate.of(2024, 12, 6)
    private val ehrTox = OtherConditionTestFactory.toxicity(name = "tox", toxicitySource = ToxicitySource.EHR, icdMainCode = "code", grade = 2)

    @Test
    fun `Should not select toxicities with code matching the icd entries to ignore`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("ignore", "keep"))
        val keepTox = ehrTox.copy(icdCode = IcdCode("keepCode"))
        val record = ToxicityTestFactory.withToxicities(listOf(keepTox, keepTox.copy(icdCode = IcdCode("ignoreCode"))))

        assertThat(ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, listOf("ignoreTitle"))).containsOnly(keepTox)
    }

    @Test
    fun `Should only select most recent EHR toxicities when multiple of same icd code are present`() {
        val newTox = ehrTox.copy(evaluatedDate = LocalDate.of(2024, 12, 6))
        val record = ToxicityTestFactory.withToxicities(
            listOf(
                newTox,
                newTox.copy(evaluatedDate = LocalDate.of(2023, 12, 6)),
                newTox.copy(evaluatedDate = LocalDate.of(2022, 12, 6))
            )
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, TestIcdFactory.createTestModel(), referenceDate))
            .containsOnly(newTox)
    }

    @Test
    fun `Should filter EHR toxicities when also present in complications`() {
        val questionnaireTox = ehrTox.copy(source = ToxicitySource.QUESTIONNAIRE)
        val (withEhrTox, withQuestionnaireTox) = listOf(ehrTox, questionnaireTox)
            .map { ToxicityTestFactory.withToxicityThatIsAlsoComplication(it, "code") }

        assertThat(ToxicityFunctions.selectRelevantToxicities(withEhrTox, TestIcdFactory.createTestModel(), referenceDate)).isEmpty()
        assertThat(ToxicityFunctions.selectRelevantToxicities(withQuestionnaireTox, TestIcdFactory.createTestModel(), referenceDate))
            .containsOnly(questionnaireTox)
    }

    @Test
    fun `Should filter out toxicities with end date before reference date`() {
        val record = ToxicityTestFactory.withToxicities(listOf(ehrTox.copy(endDate = referenceDate.minusYears(1))))
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, TestIcdFactory.createTestModel(), referenceDate)).isEmpty()
    }

    @Test
    fun `Should correctly select toxicity with icd code matching to target icd code`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("node", "nodeParent", "extension", "extensionParent"))
        val targetCode = setOf(IcdCode("nodeParentCode", "extensionParentCode"))
        val fullMatch = ehrTox.copy(icdCode = IcdCode("nodeCode", "extensionCode"))
        val mainMatchAndUnknownExtension = ehrTox.copy(icdCode = IcdCode("nodeCode", null))
        val nonMatch = ehrTox.copy(icdCode = IcdCode("wrongCode"))

        val record = ToxicityTestFactory.withToxicities(listOf(fullMatch, mainMatchAndUnknownExtension, nonMatch))
        val evaluation = ToxicityFunctions.findToxicityMatchingAnyIcdCode(icdModel, record, targetCode)
        assertThat(evaluation.fullMatches).containsOnly(fullMatch)
        assertThat(evaluation.mainCodeMatchesWithUnknownExtension).containsOnly(mainMatchAndUnknownExtension)

        assertThat(
            ToxicityFunctions.findToxicityMatchingAnyIcdCode(
                icdModel,
                record,
                setOf(IcdCode("nodeCode", null))
            ).fullMatches.containsAll(listOf(fullMatch, mainMatchAndUnknownExtension))
        ).isTrue()
    }
}