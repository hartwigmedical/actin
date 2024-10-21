package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test
import java.time.LocalDate

private val MIN_DATE = LocalDate.of(2020, 6, 6)
private val MATCHING_TREATMENT_LIST =
    listOf(TreatmentTestFactory.drugTreatment("correct", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALKYLATING_AGENT)))

class HasRecentlyReceivedCancerTherapyWithDrugTest {

    private val interpreter = WashoutTestFactory.activeFromDate(MIN_DATE)
    private val function = HasRecentlyReceivedCancerTherapyWithDrug(
        setOf(
            Drug(
                name = "correct",
                category = TreatmentCategory.CHEMOTHERAPY,
                drugTypes = setOf(DrugType.ALKYLATING_AGENT)
            )
        ), interpreter, MIN_DATE
    )

    @Test
    fun `Should fail no medications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail on medication with wrong name`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                WashoutTestFactory.withMedications(
                    listOf(
                        WashoutTestFactory.medication(
                            name = "other",
                            stopDate = MIN_DATE.plusDays(1)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail on medication with old date`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                WashoutTestFactory.withMedications(
                    listOf(
                        WashoutTestFactory.medication(
                            name = "correct",
                            stopDate = MIN_DATE.minusDays(1)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass on medication with recent date`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                WashoutTestFactory.withMedications(
                    listOf(
                        WashoutTestFactory.medication(
                            name = "correct",
                            stopDate = MIN_DATE.plusDays(1)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass on medication with wrong name and treatment history entry with drug with correct name`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TreatmentTestFactory.withTreatmentsAndMedications(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            treatments = MATCHING_TREATMENT_LIST,
                            stopYear = MIN_DATE.year,
                            stopMonth = MIN_DATE.plusMonths(1).monthValue
                        )
                    ),
                    listOf(
                        WashoutTestFactory.medication(
                            name = "other",
                            stopDate = MIN_DATE.plusDays(1)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should be undetermined with treatment history entry with drug with correct name but without date `() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistory(
                    listOf(
                        TreatmentTestFactory.treatmentHistoryEntry(
                            treatments = MATCHING_TREATMENT_LIST,
                            stopYear = null,
                            stopMonth = null
                        )
                    )
                )
            )
        )
    }
}