package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition

internal object CardiacFunctionTestFactory {
    fun createMinimal(): ECG {
        return ECG(hasSigAberrationLatestECG = false, null, null, null)
    }

    fun withHasSignificantECGAberration(hasSignificantECGAberration: Boolean): PatientRecord {
        return withHasSignificantECGAberration(hasSignificantECGAberration, null)
    }

    fun withHasSignificantECGAberration(hasSignificantECGAberration: Boolean, description: String?): PatientRecord {
        return withECG(createMinimal().copy(hasSigAberrationLatestECG = hasSignificantECGAberration, aberrationDescription = description))
    }

    fun withLVEF(lvef: Double?): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return base.copy(
                clinicalStatus = base.clinicalStatus.copy(lvef = lvef)
        )
    }

    fun withECG(ecg: ECG?): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(
                clinicalStatus = ClinicalStatus(ecg = ecg)
        )
    }

    fun withPriorOtherCondition(priorOtherCondition: PriorOtherCondition): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(
                priorOtherConditions = listOf(priorOtherCondition)
        )
    }
}