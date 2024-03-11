package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TumorStatus
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLimitedCumulativeAnthracyclineExposureTest {
    @Test
    fun shouldPassWhenNoAnthracyclineInformationProvided() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(patientRecord(null, emptyList(), emptyList())))
    }

    @Test
    fun shouldPassWithGenericChemoForNonSuspiciousCancerType() {
        val genericChemo = drugTreatment("chemo", TreatmentCategory.CHEMOTHERAPY)
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(patientRecord(setOf("other cancer type"), emptyList(), listOf(treatmentHistoryEntry(setOf(genericChemo)))))
        )
    }

    @Test
    fun shouldPassWhenPriorSecondPrimaryHasDifferentTreatmentHistory() {
        val suspectTumorTypeWithOther = priorSecondPrimary("other")
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(patientRecord(null, listOf(suspectTumorTypeWithOther), emptyList()))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenPriorSecondPrimaryHasSuspiciousPriorTreatment() {
        val firstSuspiciousTreatment = HasLimitedCumulativeAnthracyclineExposure.PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS.iterator().next()
        val suspectTumorTypeWithSuspectTreatment = priorSecondPrimary(firstSuspiciousTreatment)

        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(patientRecord(null, listOf(suspectTumorTypeWithSuspectTreatment), emptyList()))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenPriorSecondPrimaryHasNoPriorTreatmentRecorded() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(patientRecord(null, listOf(priorSecondPrimary()), emptyList()))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenChemoWithoutTypeIsProvidedAndTumorTypeIsSuspicious() {
        val genericChemo = drugTreatment("chemo", TreatmentCategory.CHEMOTHERAPY)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(patientRecord(setOf(SUSPICIOUS_CANCER_TYPE), emptyList(), listOf(treatmentHistoryEntry(setOf(genericChemo)))))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenActualAnthracyclineIsProvidedRegardlessOfTumorType() {
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
            tumorDoids: Set<String>?, priorSecondPrimaries: List<PriorSecondPrimary>, treatmentHistory: List<TreatmentHistoryEntry>
        ): PatientRecord {
            val base = TestDataFactory.createMinimalTestPatientRecord()
            return base.copy(
                tumor = base.tumor.copy(doids = tumorDoids),
                oncologicalHistory = treatmentHistory,
                priorSecondPrimaries = priorSecondPrimaries
            )
        }

        private fun priorSecondPrimary(treatmentHistory: String = ""): PriorSecondPrimary {
            return PriorSecondPrimary(
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