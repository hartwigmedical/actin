package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.time.LocalDate
import org.junit.jupiter.api.Test

class HasHadRecentResectionTest {

    private val minDate = LocalDate.of(2022, 10, 12)
    private val function = HasHadRecentResection(minDate)
    private val matchingTreatmentName = "some form of " + RESECTION_KEYWORDS.first()
    private val matchingTreatment = setOf(treatment(matchingTreatmentName, false))

    @Test
    fun `Should fail with no treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass with recent resection`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(matchingTreatment, startYear = 2022, startMonth = 11)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass with recent resection if expected term is found in synonym`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                OtherTreatment(
                    "name", false, synonyms = setOf(matchingTreatmentName), categories = setOf(
                        TreatmentCategory.SURGERY
                    )
                )
            ), startYear = 2022, startMonth = 11
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should warn for resection close to minDate`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(matchingTreatment, startYear = 2022, startMonth = 10)
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should be undetermined for resection with missing date`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(matchingTreatment)))
        )
    }

    @Test
    fun `Should be undetermined for unspecified surgery`() {
        val treatments = setOf(treatment("SURGERY", false, categories = setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                withTreatmentHistoryEntry(treatmentHistoryEntry(treatments, startYear = 2022, startMonth = 11))
            )
        )
    }
}