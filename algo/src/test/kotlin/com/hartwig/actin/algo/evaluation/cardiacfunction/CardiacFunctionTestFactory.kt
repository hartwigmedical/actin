package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import com.hartwig.actin.datamodel.clinical.OtherCondition

internal object CardiacFunctionTestFactory {
    fun createMinimal(): Ecg {
        return Ecg(null, null, null)
    }

    fun withEcgDescription(description: String? = null): PatientRecord {
        return withEcg(createMinimal().copy(name = description))
    }

    fun withLVEF(lvef: Double?): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            clinicalStatus = base.clinicalStatus.copy(lvef = lvef)
        )
    }

    fun withEcg(ecg: Ecg?): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOfNotNull(ecg)
        )
    }

    fun withOtherCondition(otherCondition: OtherCondition): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(otherCondition)
        )
    }

    fun withValueAndUnit(value: Int, unit: String = EcgUnit.MILLISECONDS.symbol()): PatientRecord {
        return withEcg(createMinimal().copy(qtcfMeasure = EcgMeasure(value = value, unit = unit)))
    }
}