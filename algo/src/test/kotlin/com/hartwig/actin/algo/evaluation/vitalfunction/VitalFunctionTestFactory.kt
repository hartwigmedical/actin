package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.EXPECTED_UNITS
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import java.time.LocalDateTime

internal object VitalFunctionTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
    
    fun withBodyWeights(bodyWeights: List<BodyWeight>): PatientRecord {
        return base.copy(bodyWeights = bodyWeights)
    }

    fun weight(
        date: LocalDateTime = LocalDateTime.of(2017, 7, 7, 12, 30, 0),
        value: Double = 0.0,
        unit: String = EXPECTED_UNITS.first()
    ): BodyWeight {
        return BodyWeight(date = date, value = value, unit = unit, valid = EXPECTED_UNITS.any { it.equals(unit, ignoreCase = true) })
    }

    fun withVitalFunctions(vitalFunctions: List<VitalFunction>): PatientRecord {
        return base.copy(vitalFunctions = vitalFunctions)
    }

    fun vitalFunction(
        category: VitalFunctionCategory,
        subcategory: String = "",
        date: LocalDateTime = LocalDateTime.of(2017, 7, 7, 12, 30, 0),
        value: Double = 0.0,
        unit: String = "",
        valid: Boolean = true
    ): VitalFunction {
        return VitalFunction(
            category = category,
            date = date,
            value = value,
            unit = unit,
            valid = valid,
            subcategory = subcategory
        )
    }
}