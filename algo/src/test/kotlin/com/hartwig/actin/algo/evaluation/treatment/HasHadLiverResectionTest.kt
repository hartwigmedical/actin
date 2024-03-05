package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadLiverResectionTest {

    private val function = HasHadLiverResection()

    @Test
    fun `Should fail with no treatment history`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with melanoma resection`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    "melanoma resection",
                    false, categories = setOf(TreatmentCategory.SURGERY)
                )
            ), bodyLocations = setOf("Melanoma")
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should pass when patient had liver resection`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    "liver resection",
                    false, categories = setOf(TreatmentCategory.SURGERY)
                )
            ), bodyLocations = setOf("Liver")
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should pass when patient had liver resection with microwave ablation`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    "liver resection with microwave ablation",
                    false, categories = setOf(TreatmentCategory.SURGERY, TreatmentCategory.ABLATION)
                )
            ), bodyLocations = setOf("Liver")
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for unspecified surgery`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    "resection",
                    false, categories = setOf(TreatmentCategory.SURGERY)
                )
            ), bodyLocations = null
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for unspecified surgery to body location liver`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    "surgery",
                    false, categories = setOf(TreatmentCategory.SURGERY)
                )
            ), bodyLocations = setOf("Liver")
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

}