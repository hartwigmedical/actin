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
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.Assert.*
import org.junit.Test


class HasHadDefinitiveLocoregionalTherapyWithCurativeIntentTest {

    @Test
    fun `Should WARN when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.WARN,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

    private val radiotherapy = TreatmentTestFactory.treatment("Radiotherapy", true, setOf(TreatmentCategory.RADIOTHERAPY))
    private val surgery = TreatmentTestFactory.treatment("Surgery", true, setOf(TreatmentCategory.SURGERY))
    private val patientRecordRadiotherapy = withTreatmentHistory(
        listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(radiotherapy),
                intents = setOf(Intent.CURATIVE)
            )
        )
    )
    private val patientRecordSurgery = withTreatmentHistory(
        listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(surgery),
                intents = setOf(Intent.CURATIVE)
            )
        )
    )
    @Test
    fun `Should PASS when treatment contains a radiotherapy or surgery with curative intent`() {

        assertEvaluation(
            EvaluationResult.PASS,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordRadiotherapy)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordSurgery)
        )
    }

    private val chemotherapy = TreatmentTestFactory.treatment("CHEMOTHERAPY", true, setOf(TreatmentCategory.CHEMOTHERAPY))
    private val patientRecordNonLocomotive = withTreatmentHistory(
        listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(chemotherapy),
                intents = setOf(Intent.CURATIVE)
            )
        )
    )
    @Test
    fun `Should FAIL when treatment contains no locoregional therapy`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordNonLocomotive)
        )
    }


    @Test
    fun `Should FAIL when treatment contains radiotherapy or surgery without curative intent`() {
        val patientRecordRadiotherapy = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(radiotherapy),
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        val patientRecordSurgery = withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(surgery),
                    intents = setOf(Intent.ADJUVANT)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordRadiotherapy)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordSurgery)
        )
    }

    private val patientRecordRadiotherapyIntentNull = withTreatmentHistory(
        listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(radiotherapy),
                intents = null
            )
        )
    )
    private val patientRecordSurgeryIntentEmptyList = withTreatmentHistory(
        listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(surgery),
                intents = emptySet<Intent>()
            )
        )
    )
    @Test
    fun `Should be UNDETERMINED when treatment contains radiotherapy or surgery with NULL or empty list of intent`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordRadiotherapyIntentNull)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadDefinitiveLocoregionalTherapyWithCurativeIntent().evaluate(patientRecordSurgeryIntentEmptyList)
        )
    }
}