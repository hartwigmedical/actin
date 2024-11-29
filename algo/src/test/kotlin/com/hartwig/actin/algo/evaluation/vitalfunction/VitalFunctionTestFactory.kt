package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.EXPECTED_UNITS
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.BodyHeight
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import java.time.LocalDateTime

internal object VitalFunctionTestFactory {
    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

    fun withBodyWeights(bodyWeights: List<BodyWeight>): PatientRecord {
        return base.copy(bodyWeights = bodyWeights)
    }

    fun withBodyWeightsAndHeight(bodyWeights: List<BodyWeight>, bodyHeight: BodyHeight): PatientRecord {
        return base.copy(bodyWeights = bodyWeights, bodyHeights = listOf(bodyHeight))
    }

    fun weight(
        date: LocalDateTime = LocalDateTime.of(2017, 7, 7, 12, 30, 0),
        value: Double = 0.0,
        unit: String = EXPECTED_UNITS.first()
    ): BodyWeight {
        return BodyWeight(date = date, value = value, unit = unit, valid = EXPECTED_UNITS.any { it.equals(unit, ignoreCase = true) })
    }

    fun height(
        date: LocalDateTime = LocalDateTime.of(2017, 7, 7, 12, 30, 0),
        value: Double = 0.0,
        valid: Boolean = true,
        unit: String = "centimeters"
    ): BodyHeight {
        return BodyHeight(date = date, value = value, unit = unit, valid = valid)
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