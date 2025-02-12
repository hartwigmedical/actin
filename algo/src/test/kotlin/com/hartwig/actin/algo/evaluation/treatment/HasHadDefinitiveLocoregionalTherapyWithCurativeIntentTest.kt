package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Assert.*
import org.junit.Test

class HasHadDefinitiveLocoregionalTherapyWithCurativeIntentTest {
    private val radiotherapy = TreatmentTestFactory.treatment("Radiotherapy", true, setOf(TreatmentCategory.RADIOTHERAPY))
    private val surgery = TreatmentTestFactory.treatment("Surgery", true, setOf(TreatmentCategory.SURGERY))
    private val chemotherapy = TreatmentTestFactory.treatment("CHEMOTHERAPY", true, setOf(TreatmentCategory.CHEMOTHERAPY))

    private fun generatePatientRecord(treatment: Treatment, intents: Set<Intent>?):PatientRecord {
        return withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = intents
                )
            )
        )
    }

    @Test
    fun `Should WARN when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.WARN,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }


    @Test
    fun `Should PASS when treatment contains a radiotherapy or surgery with curative intent`() {
        val radiotherapyCurative = generatePatientRecord(radiotherapy, setOf(Intent.CURATIVE))
        val surgeryCurative = generatePatientRecord(radiotherapy, setOf(Intent.CURATIVE))
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(radiotherapyCurative)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(surgeryCurative)
        )
    }


    @Test
    fun `Should FAIL when treatment contains no locoregional therapy`() {
        val chemotherapyCurative = generatePatientRecord(chemotherapy, setOf(Intent.CURATIVE))
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(chemotherapyCurative)
        )
    }


    @Test
    fun `Should FAIL when treatment contains radiotherapy or surgery without curative intent`() {
        val radiotherapyNotCurative = generatePatientRecord(radiotherapy, setOf(Intent.ADJUVANT))
        val surgeryNotCurative = generatePatientRecord(radiotherapy, setOf(Intent.ADJUVANT))
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(radiotherapyNotCurative)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(surgeryNotCurative)
        )
    }


    @Test
    fun `Should be UNDETERMINED when treatment contains radiotherapy or surgery with NULL or empty list of intent`() {
        val radiotherapyIntentNull = generatePatientRecord(radiotherapy, null)
        val surgeryIntentEmpty = generatePatientRecord(radiotherapy, emptySet<Intent>())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(radiotherapyIntentNull)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(surgeryIntentEmpty)
        )
    }
}