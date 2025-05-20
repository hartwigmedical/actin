package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ToxicityFunctionsTest {

    private val referenceDate = LocalDate.of(2024, 12, 6)
    private val ehrTox = ComorbidityTestFactory.toxicity(name = "tox", toxicitySource = ToxicitySource.EHR, icdMainCode = "code", grade = 2)

    @Test
    fun `Should not select toxicities with code matching the icd entries to ignore`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("ignore", "keep"))
        val keepTox = ehrTox.copy(icdCodes = setOf(IcdCode("keepCode")))
        val record = ComorbidityTestFactory.withToxicities(listOf(keepTox, keepTox.copy(icdCodes = setOf(IcdCode("ignoreCode")))))

        assertThat(ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, listOf("ignoreTitle"))).containsOnly(keepTox)
    }

    @Test
    fun `Should only select most recent EHR toxicities when multiple of same icd code are present with null evaluated date `() {
        val newTox = ehrTox.copy(evaluatedDate = LocalDate.of(2024, 12, 6))
        val record = ComorbidityTestFactory.withToxicities(
            listOf(
                newTox,
                newTox.copy(evaluatedDate = LocalDate.of(2023, 12, 6)),
                newTox.copy(evaluatedDate = null),
            )
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, TestIcdFactory.createTestModel(), referenceDate))
            .containsOnly(newTox)
    }

    @Test
    fun `Should select one EHR toxicities when when all do not have an evaluated date `() {
        val newTox = ehrTox.copy(null)
        val record = ComorbidityTestFactory.withToxicities(
            listOf(
                newTox,
                newTox.copy(evaluatedDate = null),
            )
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, TestIcdFactory.createTestModel(), referenceDate))
            .containsOnly(newTox)
    }

    @Test
    fun `Should only select most recent EHR toxicities when multiple of same icd code are present`() {
        val newTox = ehrTox.copy(evaluatedDate = LocalDate.of(2024, 12, 6))
        val record = ComorbidityTestFactory.withToxicities(
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
        val withEhrTox = ComorbidityTestFactory.withComorbidities(listOf(ehrTox, Complication(ehrTox.name, icdCodes = ehrTox.icdCodes)))
        assertThat(ToxicityFunctions.selectRelevantToxicities(withEhrTox, TestIcdFactory.createTestModel(), referenceDate)).isEmpty()
    }

    @Test
    fun `Should not filter questionnaire toxicities when also present in complications`() {
        val questionnaireTox = ehrTox.copy(source = ToxicitySource.QUESTIONNAIRE)
        val withQuestionnaireTox = ComorbidityTestFactory.withComorbidities(
            listOf(questionnaireTox, Complication(ehrTox.name, icdCodes = ehrTox.icdCodes))
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(withQuestionnaireTox, TestIcdFactory.createTestModel(), referenceDate))
            .containsOnly(questionnaireTox)
    }

    @Test
    fun `Should filter out toxicities with end date before reference date`() {
        val record = ComorbidityTestFactory.withToxicities(listOf(ehrTox.copy(endDate = referenceDate.minusYears(1))))
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, TestIcdFactory.createTestModel(), referenceDate)).isEmpty()
    }
}