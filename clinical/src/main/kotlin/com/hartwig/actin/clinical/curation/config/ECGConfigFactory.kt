package com.hartwig.actin.clinical.curation.config

class ECGConfigFactory : CurationConfigFactory<ECGConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): CurationConfigValidatedResponse<ECGConfig> {
        val isQTCF = parts[fields["isQTCF"]!!] == "1"
        val (qtcfValue: Int?, qtcfUnit: String?, qtcfValidationErrors) = extractMeasurement("qtcf", isQTCF, parts, fields)
        val isJTC = parts[fields["isJTC"]!!] == "1"
        val (jtcValue: Int?, jtcUnit: String?, jtcValidationErrors) = extractMeasurement("jtc", isJTC, parts, fields)
        val interpretation = parts[fields["interpretation"]!!]
        return CurationConfigValidatedResponse(
            ECGConfig(
                input = parts[fields["input"]!!],
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
        measurementPrefix: String, isOfType: Boolean, parts: Array<String>, fields: Map<String, Int>
    ) = if (isOfType) {
        val text = parts[fields["${measurementPrefix}Value"]!!]
        val validatedInt = text.toIntOrNull()
        if (validatedInt != null) {
            Triple(validatedInt, parts[fields["${measurementPrefix}Unit"]!!], emptyList())
        } else {
            Triple(null, null, listOf(CurationConfigValidationError("The input '$text' for '$measurementPrefix' is not a valid integer")))
        }
    } else Triple(null, null, emptyList())
}