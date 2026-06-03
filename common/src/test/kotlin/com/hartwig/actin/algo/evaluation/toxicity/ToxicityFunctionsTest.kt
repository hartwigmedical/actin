package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ToxicityFunctionsTest {

    private val referenceDate = LocalDate.of(2024, 12, 6)
    private val ehrTox = Toxicity(
        name = "tox",
        icdCodes = setOf(IcdCode("code")),
        year = 2024,
        month = 10,
        day = 6,
        source = ToxicitySource.EHR,
        grade = 2
    )

    @Test
    fun `Should not select toxicities with code matching the icd entries to ignore`() {
        val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("ignore", "keep"))
        val codesToIgnore = listOf("ignoreTitle").mapNotNull(icdModel::resolveCodeForTitle).map { it.mainCode }.toSet()
        val keepTox = ehrTox.copy(icdCodes = setOf(IcdCode("keepCode")))
        val record = withComorbidities(listOf(keepTox, keepTox.copy(icdCodes = setOf(IcdCode("ignoreCode")))))

        assertThat(ToxicityFunctions.selectRelevantToxicities(record, referenceDate, codesToIgnore)).containsOnly(keepTox)
    }

    @Test
    fun `Should only select most recent EHR toxicities when multiple of same icd code are present with null evaluated date `() {
        val newTox = ehrTox.copy(year = 2024, month = 12, day = 6)
        val record = withComorbidities(
            listOf(
                newTox,
                newTox.copy(year = 2023, month = 12, day = 6),
                newTox.copy(year = null, month = null, day = null),
            )
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, referenceDate)).containsOnly(newTox)
    }

    @Test
    fun `Should select one EHR toxicities when when all do not have an evaluated date `() {
        val newTox = ehrTox.copy(year = null, month = null, day = null)
        val record = withComorbidities(
            listOf(
                newTox,
                newTox.copy(year = null, month = null, day = null),
            )
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, referenceDate)).containsOnly(newTox)
    }

    @Test
    fun `Should only select most recent EHR toxicities when multiple of same icd code are present`() {
        val newTox = ehrTox.copy(year = 2024, month = 12, day = 6)
        val record = withComorbidities(
            listOf(
                newTox,
                newTox.copy(year = 2023, month = 12, day = 6),
                newTox.copy(year = 2022, month = 12, day = 6)
            )
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, referenceDate)).containsOnly(newTox)
    }

    @Test
    fun `Should filter EHR toxicities when also present in other conditions`() {
        val withEhrTox = withComorbidities(listOf(ehrTox, OtherCondition(ehrTox.name!!, icdCodes = ehrTox.icdCodes)))
        assertThat(ToxicityFunctions.selectRelevantToxicities(withEhrTox, referenceDate)).isEmpty()
    }

    @Test
    fun `Should not filter questionnaire toxicities when also present in other conditions`() {
        val questionnaireTox = ehrTox.copy(source = ToxicitySource.QUESTIONNAIRE)
        val withQuestionnaireTox = withComorbidities(
            listOf(questionnaireTox, OtherCondition(ehrTox.name!!, ehrTox.icdCodes))
        )
        assertThat(ToxicityFunctions.selectRelevantToxicities(withQuestionnaireTox, referenceDate))
            .containsOnly(questionnaireTox)
    }

    @Test
    fun `Should filter out toxicities with date 5 years before reference date`() {
        val record = withComorbidities(listOf(ehrTox.copy(year = 2019, month = 12, day = 6)))
        assertThat(ToxicityFunctions.selectRelevantToxicities(record, referenceDate)).isEmpty()
    }

    private fun withComorbidities(comorbidities: List<Comorbidity>): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(comorbidities = comorbidities)
    }
}
