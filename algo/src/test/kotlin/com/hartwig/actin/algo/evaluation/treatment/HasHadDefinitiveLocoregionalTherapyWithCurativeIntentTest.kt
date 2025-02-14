package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Test

class HasHadDefinitiveLocoregionalTherapyWithCurativeIntentTest {
    private val radiotherapy = TreatmentTestFactory.treatment("Radiotherapy", true, setOf(TreatmentCategory.RADIOTHERAPY))
    private val surgery = TreatmentTestFactory.treatment("Surgery", true, setOf(TreatmentCategory.SURGERY))
    private val chemotherapy = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))

    private fun generatePatientRecord(treatment: Treatment, intents: Set<Intent>?): PatientRecord {
        return withTreatmentHistory(listOf(TreatmentTestFactory.treatmentHistoryEntry(
            setOf(treatment), intents = intents)))
    }
    private fun test (patientRecord: PatientRecord):Evaluation{
        return HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecord)
    }

    @Test
    fun `Should be UNDETERMINED when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should PASS when treatment contains a radiotherapy or surgery with curative intent`() {
        listOf(radiotherapy, surgery).forEach { treatment ->
            val patientRecord = generatePatientRecord(treatment, setOf(Intent.CURATIVE))
            assertEvaluation(
                EvaluationResult.PASS,
                test(patientRecord)
            )
        }
    }

    @Test
    fun `Should be UNDETERMINED when treatment contains no locoregional therapy`() {
        val chemotherapyCurative = generatePatientRecord(chemotherapy, setOf(Intent.CURATIVE))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            test(chemotherapyCurative)
        )
    }

    @Test
    fun `Should be UNDETERMINED when treatment contains radiotherapy or surgery with NULL or non-curative intent`() {
        val radiotherapyIntentNull = generatePatientRecord(radiotherapy, null)
        val surgeryIntentEmpty = generatePatientRecord(surgery, setOf(Intent.ADJUVANT))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            test(radiotherapyIntentNull)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            test(surgeryIntentEmpty)
        )
    }

    @Test
    fun `Return PASS if there is are multiple locoregional treatments that are CURATIVE, non CURATIVE, and null`() {
        val multiTreatmentTestCase = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(radiotherapy),
                    intents = setOf(Intent.CURATIVE)
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(surgery),
                    intents = setOf(Intent.ADJUVANT)
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(radiotherapy),
                    intents = null
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            test(multiTreatmentTestCase)
        )
    }
}