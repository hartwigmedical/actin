package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ToxicityFunctionsTest {

    private val referenceDate = LocalDate.of(2024, 12, 6)

    @Test
    fun `Should not select toxicities with code matching the icd entries to ignore`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("ignore", "keep"))
        val keepTox = OtherConditionTestFactory.toxicity(name = "tox", toxicitySource = ToxicitySource.EHR, icdCode = "keepCode", grade = 2)
        val record = ToxicityTestFactory.withToxicities(listOf(keepTox, keepTox.copy(icdCode = "ignoreCode")))

        assertThat(ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, listOf("ignoreTitle"))).containsOnly(keepTox)
    }

    @Test
    fun `Should only select most recent EHR toxicities when multiple of same icd code are present`() {
        val newTox = OtherConditionTestFactory.toxicity(
            name = "tox", toxicitySource = ToxicitySource.EHR, icdCode = "code", grade = 2, date = LocalDate.of(2024, 12, 6)
        )
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

    }

//    @Test
//    fun `Should pass for questionnaire toxicities that are also complications`() {
//        val questionnaireToxicity = toxicity(source = ToxicitySource.QUESTIONNAIRE, grade = 2)
//        assertEvaluation(
//            EvaluationResult.PASS,
//            function().evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(questionnaireToxicity))
//        )
//    }
//
//    @Test
//    fun `Should ignore EHR toxicities that are also complications`() {
//        val ehrToxicity = toxicity(source = ToxicitySource.EHR, grade = 2)
//        assertEvaluation(
//            EvaluationResult.FAIL,
//            function().evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(ehrToxicity, "icdCodeForBoth"))
//        )
//    }
//
//    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
//        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
//        val mostRecentEhrToxicitiesByCode = ehrToxicities.groupBy(Toxicity::icdCode)
//            .map { (_, toxGroup) -> toxGroup.maxBy(Toxicity::evaluatedDate) }
//        return otherToxicities + mostRecentEhrToxicitiesByCode
//    }
//
//    fun hasIcdMatch(toxicity: Toxicity, targetIcdTitles: List<String>?, icdModel: IcdModel): Boolean {
//        if (targetIcdTitles == null) return true
//        val targetIcdCodes = targetIcdTitles.mapNotNull { icdModel.resolveCodeForTitle(it) }
//        return targetIcdCodes.any { it in icdModel.returnCodeWithParents(toxicity.icdCode) }
//    }
}