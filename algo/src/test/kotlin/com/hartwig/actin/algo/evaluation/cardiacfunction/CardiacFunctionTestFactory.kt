package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

internal object CardiacFunctionTestFactory {
    fun builder(): ImmutableECG.Builder {
        return ImmutableECG.builder().hasSigAberrationLatestECG(false)
    }

    fun withHasSignificantECGAberration(hasSignificantECGAberration: Boolean): PatientRecord {
        return withECG(builder().hasSigAberrationLatestECG(hasSignificantECGAberration).build())
    }

    fun withHasSignificantECGAberration(hasSignificantECGAberration: Boolean, description: String?): PatientRecord {
        return withECG(builder().hasSigAberrationLatestECG(hasSignificantECGAberration).aberrationDescription(description).build())
    }

    fun withLVEF(lvef: Double?): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return ImmutablePatientRecord.builder()
            .from(base)
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(base.clinical())
                    .clinicalStatus(ImmutableClinicalStatus.builder().from(base.clinical().clinicalStatus()).lvef(lvef).build())
                    .build()
            )
            .build()
    }

    fun withECG(ecg: ECG?): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .clinicalStatus(ImmutableClinicalStatus.builder().ecg(ecg).build())
                    .build()
            )
            .build()
    }

    fun withPriorOtherCondition(priorOtherCondition: PriorOtherCondition): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .priorOtherConditions(listOf(priorOtherCondition))
                    .build()
            )
            .build()
    }
}