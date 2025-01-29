package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import com.hartwig.actin.datamodel.clinical.OtherCondition

internal object CardiacFunctionTestFactory {
    fun createMinimal(): Ecg {
        return Ecg(hasSigAberrationLatestECG = false, null, null, null)
    }

    fun withHasSignificantECGAberration(hasSignificantECGAberration: Boolean): PatientRecord {
        return withHasSignificantECGAberration(hasSignificantECGAberration, null)
    }

    fun withHasSignificantECGAberration(hasSignificantECGAberration: Boolean, description: String?): PatientRecord {
        return withECG(createMinimal().copy(hasSigAberrationLatestECG = hasSignificantECGAberration, name = description))
    }

    fun withLVEF(lvef: Double?): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            clinicalStatus = base.clinicalStatus.copy(lvef = lvef)
        )
    }

    fun withECG(ecg: Ecg?): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            clinicalStatus = ClinicalStatus(ecg = ecg)
        )
    }

    fun withOtherCondition(otherCondition: OtherCondition): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(otherCondition)
        )
    }

    fun withValueAndUnit(value: Int, unit: String = ECGUnit.MILLISECONDS.symbol()): PatientRecord {
        return withECG(createMinimal().copy(qtcfMeasure = EcgMeasure(value = value, unit = unit)))
    }
}