package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import java.time.LocalDateTime

internal object VitalFunctionTestFactory {
    private val base = TestDataFactory.createMinimalTestPatientRecord()
    
    fun withBodyWeights(bodyWeights: List<BodyWeight>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(bodyWeights = bodyWeights))
    }

    fun bodyWeight(
        dateTime: LocalDateTime = LocalDateTime.of(2017, 7, 7, 12, 30, 0),
        value: Double = 0.0,
        unit: String = "",
        valid: Boolean = true
    ): BodyWeight {
        return BodyWeight(date = dateTime, value = value, unit = unit, valid = valid)
    }

    fun withVitalFunctions(vitalFunctions: List<VitalFunction>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(vitalFunctions = vitalFunctions))
    }

    fun vitalFunction(
        category: VitalFunctionCategory,
        dateTime: LocalDateTime = LocalDateTime.of(2017, 7, 7, 12, 30, 0),
        value: Double = 0.0,
        unit: String = "",
        valid: Boolean = true
    ): VitalFunction {
        return VitalFunction(
            category = category,
            date = dateTime,
            value = value,
            unit = unit,
            valid = valid,
            subcategory = ""
        )
    }
}