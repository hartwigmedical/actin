package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLimitedCumulativeAnthracyclineExposureTest {
    @Test
    fun `Should pass when no anthracycline information provided`() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(patientRecord(null, emptyList(), emptyList())))
    }

    @Test
    fun `Should pass with generic chemo for non suspicious cancer type`() {
        val genericChemo = drugTreatment("chemo", TreatmentCategory.CHEMOTHERAPY)
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(patientRecord(setOf("other cancer type"), emptyList(), listOf(treatmentHistoryEntry(setOf(genericChemo)))))
        )
    }

    @Test
    fun `Should pass when prior primary has different treatment history`() {
        val suspectTumorTypeWithOther = priorPrimary("other")
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(patientRecord(null, listOf(suspectTumorTypeWithOther), emptyList()))
        )
    }

    @Test
    fun `Should return undetermined when prior primary has suspicious prior treatment`() {
        val firstSuspiciousTreatment = HasLimitedCumulativeAnthracyclineExposure.PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS.iterator().next()
        val suspectTumorTypeWithSuspectTreatment = priorPrimary(firstSuspiciousTreatment)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(patientRecord(null, listOf(suspectTumorTypeWithSuspectTreatment), emptyList()))
        )
    }

    @Test
    fun `Should return undetermined when prior primary has no prior treatment recorded`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(patientRecord(null, listOf(priorPrimary()), emptyList()))
        )
    }

    @Test
    fun `Should return undetermined when chemo without type is provided and tumor type is suspicious`() {
        val genericChemo = drugTreatment("chemo", TreatmentCategory.CHEMOTHERAPY)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(patientRecord(setOf(SUSPICIOUS_CANCER_TYPE), emptyList(), listOf(treatmentHistoryEntry(setOf(genericChemo)))))
        )
    }

    @Test
    fun `Should return undetermined when actual anthracycline is provided regardless of tumor type`() {
        val priorAnthracycline = drugTreatment("chemo", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTHRACYCLINE))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(patientRecord(null, emptyList(), listOf(treatmentHistoryEntry(setOf(priorAnthracycline)))))
        )
    }

    companion object {
        private val FUNCTION = HasLimitedCumulativeAnthracyclineExposure(TestDoidModelFactory.createMinimalTestDoidModel())
        private val SUSPICIOUS_CANCER_TYPE = HasLimitedCumulativeAnthracyclineExposure.CANCER_DOIDS_FOR_ANTHRACYCLINE.iterator().next()

        private fun patientRecord(
            tumorDoids: Set<String>?, priorPrimaries: List<PriorPrimary>, treatmentHistory: List<TreatmentHistoryEntry>
        ): PatientRecord {
            val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
            return base.copy(
                tumor = base.tumor.copy(doids = tumorDoids),
                oncologicalHistory = treatmentHistory,
                priorPrimaries = priorPrimaries
            )
        }

        private fun priorPrimary(treatmentHistory: String = ""): PriorPrimary {
            return PriorPrimary(
                doids = setOf(SUSPICIOUS_CANCER_TYPE),
                tumorLocation = "",
                tumorSubLocation = "",
                tumorType = "",
                tumorSubType = "",
                treatmentHistory = treatmentHistory,
                status = TumorStatus.INACTIVE
            )
        }
    }
}