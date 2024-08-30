package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withOncologicalHistory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test


class HasHadCytoreductiveSurgeryTest {

    private val function = HasHadCytoreductiveSurgery()

    @Test
    fun `Should fail with no surgeries`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(createPatientRecord("Hormone therapy", setOf(TreatmentCategory.HORMONE_THERAPY)))
        )
    }

    @Test
    fun `Should fail with non cytoreductive surgery`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(createPatientRecord("Nephrectomy", setOf(TreatmentCategory.SURGERY))))
    }

    @Test
    fun `Should pass with history of cytoreductive surgery or HIPEC`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(createPatientRecord("HIPEC", setOf(TreatmentCategory.CHEMOTHERAPY))))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                createPatientRecord(
                    "Cytoreductive surgery",
                    setOf(TreatmentCategory.SURGERY),
                    setOf(OtherTreatmentType.CYTOREDUCTIVE_SURGERY)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                createPatientRecord(
                    "Colorectal cancer cytoreduction",
                    setOf(TreatmentCategory.SURGERY),
                    setOf(OtherTreatmentType.CYTOREDUCTIVE_SURGERY)
                )
            )
        )

    }

    @Test
    fun `Should return undetermined if surgery name not specified`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(createPatientRecord("Surgery", setOf(TreatmentCategory.SURGERY))))
    }

    @Test
    fun `Should return undetermined if debulking surgery is performed`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                createPatientRecord(
                    "Debulking",
                    setOf(TreatmentCategory.SURGERY),
                    setOf(OtherTreatmentType.DEBULKING_SURGERY)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                createPatientRecord(
                    "complete debulking",
                    setOf(TreatmentCategory.SURGERY),
                    setOf(OtherTreatmentType.DEBULKING_SURGERY)
                )
            )
        )
    }

    private fun createPatientRecord(
        name: String,
        categories: Set<TreatmentCategory>,
        types: Set<OtherTreatmentType> = emptySet()
    ): PatientRecord {
        return withOncologicalHistory(
            listOf(
                treatmentHistoryEntry(
                    treatments = setOf(treatment(name = name, isSystemic = false, categories = categories, types = types))
                )
            )
        )
    }
}