package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withOncologicalHistory
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test


class HasHadCytoreductiveSurgeryTest {

    private val function = HasHadCytoreductiveSurgery()

    @Test
    fun `Should fail with no surgeries`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(createPatientRecord(setOf(TreatmentCategory.HORMONE_THERAPY), "Hormone therapy"))
        )
    }

    @Test
    fun `Should fail with non cytoreductive surgery`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(createPatientRecord(setOf(TreatmentCategory.SURGERY), "Nephrectomy")))
    }

    @Test
    fun `Should pass with history of cytoreductive surgery or HIPEC`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(createPatientRecord(setOf(TreatmentCategory.CHEMOTHERAPY), "HIPEC")))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(createPatientRecord(setOf(TreatmentCategory.SURGERY), "Cytoreductive surgery"))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(createPatientRecord(setOf(TreatmentCategory.SURGERY), "Colorectal cancer cytoreduction"))
        )

    }

    @Test
    fun `Should return undetermined if surgery name not specified`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(createPatientRecord(setOf(TreatmentCategory.SURGERY), "Surgery")))
    }

    @Test
    fun `Should return undetermined if debulking surgery is performed`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(createPatientRecord(setOf(TreatmentCategory.SURGERY), "Debulking"))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(createPatientRecord(setOf(TreatmentCategory.SURGERY), "complete debulking"))
        )
    }

    private fun createPatientRecord(categories: Set<TreatmentCategory>, name: String): PatientRecord {
        return withOncologicalHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = setOf(
                        treatment(
                            name = name,
                            isSystemic = false,
                            categories = categories
                        )
                    )
                )
            )
        )
    }
}