package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import org.junit.jupiter.api.Test

private val CATEGORY_TO_IGNORE = TreatmentCategory.TARGETED_THERAPY

class HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingTest {

    private val referenceDate = LocalDate.of(2024, 11, 26)
    private val recentDate = referenceDate.minusMonths(1)
    private val nonRecentDate = recentDate.minusMonths(7)
    private val function = HasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSetting(
        referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        categoryToIgnore = CATEGORY_TO_IGNORE,
        settingDescription = "metastatic"
    )
    private val nonRecentTreatment = createTreatment(
        intent = null, isSystemic = true, "Treatment a", stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue
    )

    @Test
    fun `Should fail if patient has only had systemic treatments with curative intent`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE, isSystemic = true, "Treatment a")))),
            "Has only had prior systemic treatment with Curative intent - thus presumably not in metastatic setting (Treatment a)"
        )
    }

    @Test
    fun `Should fail for non-systemic treatment`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.PALLIATIVE, isSystemic = false)))),
            "No prior systemic treatment in metastatic setting"
        )
    }

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(emptyList())),
            "No prior systemic treatment in metastatic setting"
        )
    }

    @Test
    fun `Should pass if patient has had systemic treatment with palliative intent`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                withTreatmentHistory(listOf(Intent.PALLIATIVE, Intent.CURATIVE).map { createTreatment(it, isSystemic = true) })
            ),
            "Has had prior systemic treatment in metastatic setting ignoring targeted therapy (Treatment name)"
        )
    }

    @Test
    fun `Should fail if patient has had systemic treatment with palliative intent but of category to ignore`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(Intent.PALLIATIVE, Intent.CURATIVE).map {
                createTreatment(
                    it,
                    isSystemic = true,
                    categories = setOf(CATEGORY_TO_IGNORE)
                )
            })),
            "No prior systemic treatment in metastatic setting"
        )
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with unknown intent`() {
        val record = withTreatmentHistory(
            listOf(createTreatment(null, isSystemic = true, "Treatment", stopYear = recentDate.year, stopMonth = recentDate.monthValue))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "Has had recent systemic treatment ignoring targeted therapy - presumably in metastatic setting (Treatment)"
        )
    }

    @Test
    fun `Should pass if patient has had systemic treatment within 6 months with intent other than curative, adjuvant or neoadjuvant`() {
        val record = withTreatmentHistory(
            listOf(Intent.INDUCTION, Intent.CURATIVE)
                .map { createTreatment(it, isSystemic = true, "Treatment", stopYear = recentDate.year, stopMonth = recentDate.monthValue) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "Has had recent systemic treatment ignoring targeted therapy - presumably in metastatic setting (Treatment)"
        )
    }

    @Test
    fun `Should pass if patient has had more than one systemic lines with unknown or non curative or (neo)adjuvant intent`() {
        val record = withTreatmentHistory(
            listOf(null, Intent.MAINTENANCE, Intent.INDUCTION)
                .map { nonRecentTreatment.copy(intents = it?.let { setOf(it) }) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "Has had more than one systemic treatment line of uncertain setting ignoring targeted therapy- presumably at least one in metastatic setting (Treatment a)"
        )
    }

    @Test
    fun `Should pass if patient has had one systemic line with non curative or (neo)adjuvant intent and not followed by radiotherapy or surgery`() {
        val record = withTreatmentHistory(listOf(createTreatment(null, isSystemic = true, stopYear = null, stopMonth = null)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "Has had a systemic treatment line ignoring targeted therapy not followed by radiotherapy or surgery - presumably in metastatic setting (Treatment name)"
        )
    }

    @Test
    fun `Should evaluate to undetermined if patient has had prior systemic treatment more than 6 months ago with intent other than curative or (neo)adjuvant followed by radiotherapy`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.MAINTENANCE, isSystemic = true, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue),
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = false,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue + 1,
                    categories = setOf(TreatmentCategory.RADIOTHERAPY)
                )
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Has had prior systemic treatment ignoring targeted therapy >6 months ago but undetermined if in metastatic setting (Treatment name)"
        )
    }

    @Test
    fun `Should evaluate to undetermined if patient has had surgery before systemic treatment with intent other than curative or (neo)adjuvant more than 6 months ago`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = false,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue,
                    categories = setOf(TreatmentCategory.SURGERY)
                ),
                createTreatment(
                    Intent.MAINTENANCE,
                    isSystemic = true,
                    stopYear = nonRecentDate.year,
                    stopMonth = nonRecentDate.monthValue + 1
                ),
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Has had prior systemic treatment ignoring targeted therapy >6 months ago but undetermined if in metastatic setting (Treatment name)"
        )
    }

    @Test
    fun `Should evaluate to undetermined if patient has had systemic treatment with unknown date and intent other than curative or (neo)adjuvant and surgery with unknown date`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.MAINTENANCE, isSystemic = true, stopYear = null, stopMonth = null),
                createTreatment(
                    Intent.CURATIVE,
                    isSystemic = false,
                    stopYear = null,
                    stopMonth = null,
                    categories = setOf(TreatmentCategory.SURGERY)
                )
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Has had prior systemic treatment ignoring targeted therapy but undetermined if in metastatic setting (Treatment name)"
        )
    }

    private fun createTreatment(
        intent: Intent?,
        isSystemic: Boolean,
        name: String = "treatment name",
        stopYear: Int? = recentDate.year,
        stopMonth: Int? = recentDate.monthValue,
        categories: Set<TreatmentCategory> = emptySet()
    ): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment(name, isSystemic, categories)),
            intents = intent?.let { setOf(it) },
            stopMonth = stopMonth,
            stopYear = stopYear
        )
    }
}
