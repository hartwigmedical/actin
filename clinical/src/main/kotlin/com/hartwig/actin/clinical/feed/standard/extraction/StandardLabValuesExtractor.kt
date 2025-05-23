package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.feed.datamodel.FeedLabValue
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardLabValuesExtractor(private val labMeasurementCuration: CurationDatabase<LabMeasurementConfig>) :
    StandardDataExtractor<List<LabValue>> {
    override fun extract(feedPatientRecord: FeedPatientRecord): ExtractionResult<List<LabValue>> {
        return feedPatientRecord.labValues.map { providedLabValue ->
            val inputText = "${providedLabValue.measureCode} | ${providedLabValue.measure}"
            val curationResponse = CurationResponse.createFromConfigs(
                labMeasurementCuration.find(inputText),
                feedPatientRecord.patientDetails.patientId,
                CurationCategory.LAB_MEASUREMENT,
                inputText,
                "lab measurement",
                true
            )
            val labValue = curationResponse.config()?.takeIf { !it.ignore }?.let {
                labValue(providedLabValue, it.labMeasurement)
            }
            ExtractionResult(listOfNotNull(labValue), curationResponse.extractionEvaluation)
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }
    }

    private fun labValue(it: FeedLabValue, labMeasurement: LabMeasurement) = LabValue(
        date = it.date,
        measurement = labMeasurement,
        unit = labUnit(it),
        value = it.value,
        comparator = it.comparator ?: "",
        refLimitUp = it.refUpperBound,
        refLimitLow = it.refLowerBound
    )

    private fun labUnit(it: FeedLabValue): LabUnit {
        val labUnit = ProvidedLabUnit.fromString(it.unit)
        return when (labUnit) {
            ProvidedLabUnit.MILLIONS_PER_LITER -> LabUnit.MILLIONS_PER_LITER
            ProvidedLabUnit.MILLIONS_PER_MILLILITER -> LabUnit.MILLIONS_PER_MILLILITER
            ProvidedLabUnit.BILLIONS_PER_LITER -> LabUnit.BILLIONS_PER_LITER
            ProvidedLabUnit.TRILLIONS_PER_LITER -> LabUnit.TRILLIONS_PER_LITER
            ProvidedLabUnit.OTHER -> LabUnit.NONE
            else -> LabUnit.valueOf(ProvidedLabUnit.fromString(it.unit).name)
        }
    }
}

private enum class ProvidedLabUnit(vararg val externalFormats: String) {
    NANOGRAMS_PER_LITER("ng/L"),
    NANOGRAMS_PER_MILLILITER("ng/mL"),
    MICROGRAMS_PER_LITER("ug/L"),
    MICROGRAMS_PER_MICROLITER("µg/µL"),
    MILLIGRAMS_PER_DECILITER("mg/dL"),
    MILLIGRAMS_PER_MILLIMOLE("mg/mmol"),
    MILLIGRAMS_PER_LITER("mg/L"),
    GRAMS_PER_DECILITER("g/dL"),
    GRAMS_PER_LITER("g/L"),
    GRAMS_PER_MOLE("g/mol"),
    KILOGRAMS_PER_LITER("kg/L"),
    MICROGRAMS_PER_GRAM("µg/g"),
    GRAMS("g"),
    PICOMOLES_PER_LITER("pmol/L"),
    NANOMOLES_PER_LITER("nmol/L"),
    MICROMOLES_PER_LITER("umol/L"),
    MILLIMOLES_PER_LITER("mmol/L"),
    MILLIMOLES_PER_MOLE("mmol/mol"),
    CELLS_PER_CUBIC_MILLIMETER("cells/mm3"),
    MILLIONS_PER_LITER("10E6/L"),
    MILLIONS_PER_MILLILITER("10E6/mL"),
    BILLIONS_PER_LITER("10E9/L"),
    TRILLIONS_PER_LITER("10E12/L"),
    MILLIUNITS_PER_LITER("mU/L"),
    UNITS_PER_LITER("U/L"),
    UNITS_PER_MILLILITER("U/mL"),
    KILOUNITS_PER_LITER("kU/L"),
    INTERNATIONAL_UNITS_PER_LITER("IU/L"),
    UNITS_OF_INR("INR"),
    NANOMOLES_PER_DAY("nmol/24h"),
    MILLIMOLES_PER_DAY("mmol/24h"),
    MILLIMETERS_PER_HOUR("mm/hr"),
    MILLILITERS_PER_MINUTE("mL/min"),
    FEMTOLITERS("fL"),
    MILLILITERS("mL"),
    KILO_PASCAL("kPa"),
    SECONDS("sec"),
    PERCENTAGE("%"),
    PERCENTAGE_OF_LEUKOCYTES("% of leukocytes"),
    PERCENTAGE_OF_T_CELLS("% of T-cells"),
    MILLI_OSMOLE_PER_KILOGRAM("mOsm/kg"),
    INTERNATIONAL_UNITS_PER_MILLILITER("IU/ml"),
    PRNT50("PRNT50"),
    OTHER,
    NONE("");

    companion object {
        fun fromString(input: String?): ProvidedLabUnit {
            return input?.let { inputString ->
                ProvidedLabUnit.entries.firstOrNull {
                    it.externalFormats.map { f -> f.lowercase() }.contains(inputString.lowercase())
                } ?: OTHER
            } ?: NONE
        }
    }
}
