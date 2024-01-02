package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory

class ECGConfigFactory : CurationConfigFactory<ECGConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ECGConfig> {
        val input = parts[fields["input"]!!]
        val isQTCF = parts[fields["isQTCF"]!!] == "1"
        val (qtcfValue: Int?, qtcfUnit: String?, qtcfValidationErrors) = extractMeasurement(input, "qtcf", isQTCF, parts, fields)
        val isJTC = parts[fields["isJTC"]!!] == "1"
        val (jtcValue: Int?, jtcUnit: String?, jtcValidationErrors) = extractMeasurement(input, "jtc", isJTC, parts, fields)
        val interpretation = parts[fields["interpretation"]!!]
        return ValidatedCurationConfig(
            ECGConfig(
                input = input,
                ignore = interpretation == "NULL",
                interpretation = interpretation,
                isQTCF = isQTCF,
                qtcfValue = qtcfValue,
                qtcfUnit = qtcfUnit,
                isJTC = isJTC,
                jtcValue = jtcValue,
                jtcUnit = jtcUnit
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