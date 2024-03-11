package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

internal object LabTestFactory {
    fun withLabValue(labValue: LabValue): PatientRecord {
        return withLabValues(listOf(labValue))
    }

    fun withLabValues(labValues: List<LabValue>): PatientRecord {
        return TestDataFactory.createMinimalTestPatientRecord().copy(labValues = labValues)
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
            refLimitUp = refLimitUp
        )
    }
}