package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.jupiter.api.Test

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumWeeksAsMostRecentTest {

    private val function = HasHadTreatmentCategoryOfOnlyTypesAndMinimumWeeksAsMostRecent(
        TreatmentCategory.CHEMOTHERAPY,
        setOf(DrugType.ALKYLATING_AGENT),
        26
    )

    @Test
    fun `Should fail if there are no systemic treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if most recent treatment is of other type`() {
        val record = recordWith(entry(DrugType.ANTIMETABOLITE, 2023, 1, 2023, 12))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail if matching treatment is not the most recent line`() {
        val record = recordWith(entry(DrugType.ALKYLATING_AGENT, 2022, 1, 2022, 12), entry(DrugType.ANTIMETABOLITE, 2023, 1, 2023, 12))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should pass if most recent treatment of only matching types lasted long enough`() {
        val record = recordWith(entry(DrugType.ALKYLATING_AGENT, 2023, 1, 2023, 12))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail if most recent treatment was too short`() {
        val record = recordWith(entry(DrugType.ALKYLATING_AGENT, 2023, 1, 2023, 3))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined if start date unknown`() {
        val record = recordWith(entry(DrugType.ALKYLATING_AGENT, null, null, 2023, 12))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined if stop date unknown`() {
        val record = recordWith(entry(DrugType.ALKYLATING_AGENT, 2024, 1, null, null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined if most recent treatment of category has no types configured`() {
        val record = recordWith(
            treatmentHistoryEntry(
                setOf(drugTreatment("drug", TreatmentCategory.CHEMOTHERAPY)),
                startYear = 2023,
                startMonth = 1,
                stopYear = 2023,
                stopMonth = 12
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should pass for non-systemic treatment of matching category and type`() {
        val radiotherapyFunction = HasHadTreatmentCategoryOfOnlyTypesAndMinimumWeeksAsMostRecent(
            TreatmentCategory.RADIOTHERAPY,
            setOf(RadiotherapyType.BRACHYTHERAPY),
            26
        )
        val record = recordWith(
            treatmentHistoryEntry(
                setOf(Radiotherapy(name = "brachytherapy", radioType = RadiotherapyType.BRACHYTHERAPY)),
                startYear = 2023,
                startMonth = 1,
                stopYear = 2023,
                stopMonth = 12
            )
        )
        assertEvaluation(EvaluationResult.PASS, radiotherapyFunction.evaluate(record))
    }

    private fun recordWith(vararg entries: TreatmentHistoryEntry): PatientRecord = withTreatmentHistory(entries.toList())

    private fun entry(type: DrugType, startYear: Int?, startMonth: Int?, stopYear: Int?, stopMonth: Int?) = treatmentHistoryEntry(
        setOf(drugTreatment("drug", TreatmentCategory.CHEMOTHERAPY, setOf(type))),
        startYear = startYear,
        startMonth = startMonth,
        stopYear = stopYear,
        stopMonth = stopMonth
    )
}
