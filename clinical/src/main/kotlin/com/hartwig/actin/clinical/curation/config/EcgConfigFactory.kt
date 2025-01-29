package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure

class EcgConfigFactory : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val input = parts[fields["input"]!!]
        val isQtcf = parts[fields["isQTCF"]!!] == "1"
        val (qtcfValue: Int?, qtcfUnit: String?, qtcfValidationErrors) = extractMeasurement(input, "qtcf", isQtcf, parts, fields)
        val isJtc = parts[fields["isJTC"]!!] == "1"
        val (jtcValue: Int?, jtcUnit: String?, jtcValidationErrors) = extractMeasurement(input, "jtc", isJtc, parts, fields)
        val interpretation = parts[fields["interpretation"]!!]
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = interpretation == "NULL",
                curated = Ecg(
                    name = interpretation.trim().ifEmpty { null },
                    qtcfMeasure = EcgMeasure(qtcfValue, qtcfUnit).takeIf { isQtcf },
                    jtcMeasure = EcgMeasure(jtcValue, jtcUnit).takeIf { isJtc },
                ),
            ), qtcfValidationErrors + jtcValidationErrors
        )
    }

    private fun extractMeasurement(
        input: String, measurementPrefix: String, isOfType: Boolean, parts: Array<String>, fields: Map<String, Int>
    ): Triple<Int?, String?, List<CurationConfigValidationError>> = if (isOfType) {
        val fieldName = "${measurementPrefix}Value"
        val validatedInt = validateInteger(CurationCategory.ECG, input, fieldName, fields, parts)
        Triple(validatedInt.first, validatedInt.first?.let { parts[fields["${measurementPrefix}Unit"]!!] }, validatedInt.second)
    } else Triple(null, null, emptyList())
}