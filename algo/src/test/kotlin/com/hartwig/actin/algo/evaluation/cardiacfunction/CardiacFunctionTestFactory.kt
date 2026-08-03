package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.HeartMeasurement
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType
import com.hartwig.actin.datamodel.clinical.OtherCondition

internal object CardiacFunctionTestFactory {
    fun createMinimal(): HeartMeasurement {
        return HeartMeasurement(null, emptySet(), false, null, null, null, null)
    }

    fun withEcgDescription(description: String? = null): PatientRecord {
        return withEcg(createMinimal().copy(name = description, isECG = true))
    }

    fun withLvef(lvef: Double?): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            clinicalStatus = base.clinicalStatus.copy(lvef = lvef)
        )
    }

    fun withHeartMeasurements(heartMeasurements: List<HeartMeasurement>): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(comorbidities = heartMeasurements)
    }

    fun withEcg(heartMeasurement: HeartMeasurement?) = withHeartMeasurements(listOfNotNull(heartMeasurement?.copy(isECG = true)))

    fun withOtherCondition(otherCondition: OtherCondition): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            comorbidities = listOf(otherCondition)
        )
    }

    fun withValueAndUnit(value: Double, unit: String = EcgUnit.MILLISECONDS.symbol()): PatientRecord {
        return withEcg(
            createMinimal().copy(isECG = true, value = value, unit = unit, measurementType = HeartMeasurementType.QTCF)
        )
    }
}