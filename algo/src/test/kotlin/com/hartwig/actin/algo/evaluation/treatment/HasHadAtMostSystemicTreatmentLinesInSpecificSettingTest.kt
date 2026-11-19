package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import org.junit.jupiter.api.Test

class HasHadAtMostSystemicTreatmentLinesInSpecificSettingTest {

    private val referenceDate = LocalDate.of(2024, 11, 26)
    private val recentDate = referenceDate.minusMonths(1)
    private val nonRecentDate = referenceDate.minusMonths(8)
    private val function = HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
        referenceDate = referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = 2,
        category = null,
        types = emptySet()
    )
    private val chemotherapyFunction = HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
        referenceDate = referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = 2,
        category = TreatmentCategory.CHEMOTHERAPY,
        types = emptySet()
    )
    private val platinumChemotherapyFunction = HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
        referenceDate = referenceDate,
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = 2,
        category = TreatmentCategory.CHEMOTHERAPY,
        types = setOf(DrugType.PLATINUM_COMPOUND)
    )

    @Test
    fun `Should pass with no prior systemic treatment`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(emptyList())),
            "No prior systemic treatment in metastatic setting in provided treatments - thus within maximum of 2 line(s)"
        )
    }

    @Test
    fun `Should pass with only excluded intent treatments`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE)))),
            "No prior systemic treatment in metastatic setting in provided treatments - thus within maximum of 2 line(s)"
        )
    }

    @Test
    fun `Should fail if palliative lines alone exceed maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.PALLIATIVE, Intent.PALLIATIVE).map { createTreatment(it) }
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluation,
            "More than 2 systemic treatment line(s) with palliative intent in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should fail when probable lines exceed maximum plus one buffer`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE, stopYear = recentDate.year, stopMonth = recentDate.monthValue),
                createTreatment(Intent.INDUCTION, stopYear = recentDate.year, stopMonth = recentDate.monthValue),
                createTreatment(null, stopYear = recentDate.year, stopMonth = recentDate.monthValue)
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluation,
            "Likely exceeded maximum of 2 systemic treatment line(s) in metastatic setting (4 lines likely in metastatic setting)"
        )
    }

    @Test
    fun `Should be undetermined when uncertain lines could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE, stopYear = recentDate.year, stopMonth = recentDate.monthValue),
                createTreatment(null, stopYear = recentDate.year, stopMonth = recentDate.monthValue)
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Uncertain whether maximum of 2 systemic treatment line(s) in metastatic setting is exceeded (3 lines likely in metastatic setting, setting unclear for some)"
        )
    }

    @Test
    fun `Should be undetermined when non-palliative treatment with unknown stop date could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE),
                createTreatment(null)
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Uncertain whether maximum of 2 systemic treatment line(s) in metastatic setting is exceeded (3 lines likely in metastatic setting, setting unclear for some)"
        )
    }

    @Test
    fun `Should fail when non-palliative treatments with unknown stop date push probable count above maximum plus one buffer`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE),
                createTreatment(Intent.INDUCTION),
                createTreatment(null)
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluation,
            "Likely exceeded maximum of 2 systemic treatment line(s) in metastatic setting (4 lines likely in metastatic setting)"
        )
    }

    @Test
    fun `Should be undetermined when non-recent uncertain lines could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(null, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue),
                createTreatment(null, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue)
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Uncertain whether maximum of 2 systemic treatment line(s) in metastatic setting is exceeded (3 lines with non-excluded intent, setting unclear for older lines)"
        )
    }

    @Test
    fun `Should pass when total included lines are at the maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(Intent.MAINTENANCE, stopYear = recentDate.year, stopMonth = recentDate.monthValue)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "At most 2 systemic treatment line(s) in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should pass when non-recent uncertain lines stay within maximum`() {
        val record = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE),
                createTreatment(null, stopYear = nonRecentDate.year, stopMonth = nonRecentDate.monthValue)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "At most 2 systemic treatment line(s) in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should pass when included lines are equal to the maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.PALLIATIVE).map { createTreatment(it) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record),
            "At most 2 systemic treatment line(s) in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should pass with only treatments of other category when category is specified`() {
        val record = withTreatmentHistory(
            List(3) { createTreatment(Intent.PALLIATIVE, category = TreatmentCategory.IMMUNOTHERAPY) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            chemotherapyFunction.evaluate(record),
            "No prior ${TreatmentCategory.CHEMOTHERAPY.display()} treatment in metastatic setting in provided treatments - thus within maximum of 2 line(s)"
        )
    }

    @Test
    fun `Should only count treatments of specified category`() {
        val record = withTreatmentHistory(
            List(2) { createTreatment(Intent.PALLIATIVE, category = TreatmentCategory.CHEMOTHERAPY) } +
                    List(3) { createTreatment(Intent.PALLIATIVE, category = TreatmentCategory.IMMUNOTHERAPY) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            chemotherapyFunction.evaluate(record),
            "At most 2 ${TreatmentCategory.CHEMOTHERAPY.display()} treatment line(s) in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should fail if treatments of specified category exceed maximum`() {
        val record = withTreatmentHistory(
            List(3) { createTreatment(Intent.PALLIATIVE, category = TreatmentCategory.CHEMOTHERAPY) } +
                    listOf(createTreatment(Intent.PALLIATIVE, category = TreatmentCategory.IMMUNOTHERAPY))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            chemotherapyFunction.evaluate(record),
            "More than 2 ${TreatmentCategory.CHEMOTHERAPY.display()} treatment line(s) with palliative intent in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should only count treatments of specified category and types`() {
        val record = withTreatmentHistory(
            List(2) { createTreatment(Intent.PALLIATIVE, types = setOf(DrugType.PLATINUM_COMPOUND)) } +
                    List(3) { createTreatment(Intent.PALLIATIVE, types = setOf(DrugType.TAXANE)) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            platinumChemotherapyFunction.evaluate(record),
            "At most 2 ${DrugType.PLATINUM_COMPOUND.display()} ${TreatmentCategory.CHEMOTHERAPY.display()} treatment line(s) in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should fail if treatments of specified category and types exceed maximum`() {
        val record = withTreatmentHistory(
            List(3) { createTreatment(Intent.PALLIATIVE, types = setOf(DrugType.PLATINUM_COMPOUND)) }
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            platinumChemotherapyFunction.evaluate(record),
            "More than 2 ${DrugType.PLATINUM_COMPOUND.display()} ${TreatmentCategory.CHEMOTHERAPY.display()} treatment line(s) with palliative intent in metastatic setting in provided treatments"
        )
    }

    @Test
    fun `Should pass with no treatments of specified types`() {
        val record = withTreatmentHistory(
            List(3) { createTreatment(Intent.PALLIATIVE, types = setOf(DrugType.TAXANE)) }
        )
        assertEvaluation(
            EvaluationResult.PASS,
            platinumChemotherapyFunction.evaluate(record),
            "No prior ${DrugType.PLATINUM_COMPOUND.display()} ${TreatmentCategory.CHEMOTHERAPY.display()} treatment in metastatic setting in provided treatments - thus within maximum of 2 line(s)"
        )
    }

    private fun createTreatment(
        intent: Intent?,
        stopYear: Int? = null,
        stopMonth: Int? = null,
        category: TreatmentCategory = TreatmentCategory.CHEMOTHERAPY,
        types: Set<DrugType> = emptySet()
    ): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("treatment", category, types)),
            intents = intent?.let { setOf(it) },
            stopYear = stopYear,
            stopMonth = stopMonth
        )
    }
}
