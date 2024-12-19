package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.icd.datamodel.IcdNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val referenceDate: LocalDate = LocalDate.of(2024, 10, 1)
private val icdModel = TestIcdFactory.createTestModel()

class HasToxicityWithGradeTest {

    @Test
    fun `Should fail with no toxicities`() {
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(ToxicityTestFactory.withToxicities(emptyList())))
    }

    @Test
    fun `Should fail with grade 1 questionnaire toxicity`() {
        val toxicities = listOf(toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 1))
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun `Should warn for grade 2 EHR toxicity`() {
        val toxicities = listOf(toxicity(source = ToxicitySource.EHR, grade = 2))
        assertEvaluation(EvaluationResult.WARN, function().evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun `Should pass for grade 2 questionnaire toxicity`() {
        val toxicities = listOf(toxicity(name = "tox", source = ToxicitySource.QUESTIONNAIRE, grade = 2))
        val evaluation = function().evaluate(ToxicityTestFactory.withToxicities(toxicities))
        assertEvaluation(EvaluationResult.PASS, evaluation)
    }

    @Test
    fun `Should pass by default with questionnaire toxicity without grade`() {
        val toxicities = listOf(toxicity(source = ToxicitySource.QUESTIONNAIRE))
        assertEvaluation(EvaluationResult.PASS, function().evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun `Should return undetermined for questionnaire toxicity without grade with higher minimum grade`() {
        val toxicities = listOf(toxicity(source = ToxicitySource.QUESTIONNAIRE))
        val function = function(minGrade = DEFAULT_QUESTIONNAIRE_GRADE + 1)
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(ToxicityTestFactory.withToxicities(toxicities))
        )
    }

    @Test
    fun `Should pass with questionnaire toxicity with higher grade`() {
        val toxicities = listOf(
            toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = DEFAULT_QUESTIONNAIRE_GRADE + 2)
        )
        val function = function(minGrade = DEFAULT_QUESTIONNAIRE_GRADE + 1)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun `Should ignore toxicities that match icd code of icd titles in ignore list`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("ignore", "keep"))
        val function = function(icdModel, ignoreFilters = listOf("ignoreTitle"))
        val toxicities = listOf(
            toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "ignore me", icdMainCode = "ignoreCode"),
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))

        val matchingToxicity = toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "keep me", icdMainCode = "keepCode")
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ToxicityTestFactory.withToxicities(toxicities + matchingToxicity))
        )
    }

    @Test
    fun `Should match selectively using icd codes of icd titles in target list`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("target", "nonTarget"))
        val function = function(icdModel, targetIcdTitles = listOf("targetTitle"))
        val toxicities = listOf(
            toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "not a target", icdMainCode = "nonTargetCode"),
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))

        val matchingToxicity = toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "targetTox", icdMainCode = "targetCode")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities + matchingToxicity)))
    }

    @Test
    fun `Should pass if icd entry in target list is parent of toxicity in patient record`() {
        val icdModel = IcdModel.create(
            listOf(
                IcdNode("targetCode", listOf("parentCode"), "targetTitle"),
                IcdNode("parentCode", emptyList(), "parentTitle")
            )
        )
        val function = function(icdModel, targetIcdTitles = listOf("parentTitle"))
        val toxicities = listOf(
            toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2, name = "target tox", icdMainCode = "targetCode"),
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    @Test
    fun `Should return recoverable warning when questionnaire is not source`() {
        val toxicities = listOf(
            toxicity(
                source = ToxicitySource.EHR, grade = DEFAULT_QUESTIONNAIRE_GRADE + 1, name = "toxicity 1",
            )
        )
        val evaluation = function().evaluate(ToxicityTestFactory.withToxicities(toxicities))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should return recoverable pass when questionnaire is not source but configured not to warn`() {
        val function = function(warnIfToxicitiesNotFromQuestionnaire = false)
        val toxicities = listOf(
            toxicity(ToxicitySource.EHR, DEFAULT_QUESTIONNAIRE_GRADE + 1, "toxicity 1")
        )
        val evaluation = function.evaluate(ToxicityTestFactory.withToxicities(toxicities))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should not pass for toxicity with earlier end date`() {
        val function = function()
        val toxicities = listOf(
            toxicity(
                ToxicitySource.QUESTIONNAIRE, DEFAULT_QUESTIONNAIRE_GRADE, "toxicity 1", endDate = LocalDate.of(2022, 1, 2)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)))
    }

    private fun function(
        icd: IcdModel = icdModel,
        minGrade: Int = DEFAULT_QUESTIONNAIRE_GRADE,
        targetIcdTitles: List<String>? = null,
        ignoreFilters: List<String> = emptyList(),
        warnIfToxicitiesNotFromQuestionnaire: Boolean = true
    ): HasToxicityWithGrade {
        return HasToxicityWithGrade(
            icd, minGrade, targetIcdTitles, ignoreFilters, warnIfToxicitiesNotFromQuestionnaire, referenceDate
        )
    }

    fun toxicity(
        source: ToxicitySource,
        grade: Int? = null,
        name: String = "name",
        icdMainCode: String = icdModel.titleToCodeMap.keys.first(),
        endDate: LocalDate? = null,
        evaluatedDate: LocalDate? = null
    ) =
        Toxicity(name, setOf(IcdCode(icdMainCode)), evaluatedDate ?: referenceDate.minusMonths(1), source, grade, endDate)
}