package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.VitalFunctionCategoryResolver
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.HEART_RATE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory.SPO2
import com.hartwig.feed.datamodel.FeedMeasurement
import com.hartwig.feed.datamodel.FeedPatientRecord

class VitalFunctionsExtractor {

    fun extract(feedRecord: FeedPatientRecord): ExtractionResult<List<VitalFunction>> {

        return ExtractionResult(
            feedRecord.measurements
                .filterNot { it.category == "BODY_WEIGHT" }
                .filter { it.value > 0 }
                .map { entry ->
                    VitalFunction(
                        date = entry.date,
                        category = VitalFunctionCategoryResolver.determineCategory(entry.category),
                        subcategory = entry.subcategory ?: "NA",
                        value = entry.value,
                        unit = entry.unit,
                        valid = vitalFunctionIsValid(entry)
                    )
                }, CurationExtractionEvaluation()
        )
    }

    private fun vitalFunctionIsValid(entry: FeedMeasurement): Boolean {
        return when (VitalFunctionCategoryResolver.determineCategory(entry.category)) {
            NON_INVASIVE_BLOOD_PRESSURE, ARTERIAL_BLOOD_PRESSURE -> {
                entry.value in BLOOD_PRESSURE_MIN..BLOOD_PRESSURE_MAX && entry.unit.lowercase() == BLOOD_PRESSURE_EXPECTED_UNIT
            }

            HEART_RATE -> {
                entry.value in HEART_RATE_MIN..HEART_RATE_MAX && entry.unit.lowercase() == HEART_RATE_EXPECTED_UNIT
            }

            SPO2 -> {
                entry.value in SPO2_MIN..SPO2_MAX && entry.unit.lowercase() == SPO2_EXPECTED_UNIT
            }

            else -> {
                false
            }
        }
    }

    companion object {
        const val HEART_RATE_MIN = 10.0
        const val HEART_RATE_MAX = 300.0
        const val HEART_RATE_EXPECTED_UNIT = "bpm"
        const val BLOOD_PRESSURE_MIN = 10.0
        const val BLOOD_PRESSURE_MAX = 300.0
        const val BLOOD_PRESSURE_EXPECTED_UNIT = "mmhg"
        const val SPO2_MIN = 10.0
        const val SPO2_MAX = 100.0
        const val SPO2_EXPECTED_UNIT = "percent"
    }
}