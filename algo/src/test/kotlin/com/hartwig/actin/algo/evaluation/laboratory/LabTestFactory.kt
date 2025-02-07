package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

internal object LabTestFactory {
    fun withLabValue(labValue: LabValue): PatientRecord {
        return withLabValues(listOf(labValue))
    }

    fun withLabValues(labValues: List<LabValue>): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(labValues = labValues)
    }

    fun create(
        measurement: LabMeasurement? = null,
        value: Double = 0.0,
        date: LocalDate = LocalDate.of(2020, 1, 1),
        refLimitLow: Double? = null,
        refLimitUp: Double? = null
    ): LabValue {
        return LabValue(
            date = date,
            name = "",
            code = measurement?.code ?: "",
            comparator = "",
            value = value,
            unit = measurement?.defaultUnit ?: LabUnit.NONE,
            refLimitLow = refLimitLow,
            refLimitUp = refLimitUp,
            isOutsideRef = (refLimitUp != null && value > refLimitUp) || (refLimitLow != null && value < refLimitLow)
        )
    }
}