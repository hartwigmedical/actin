package com.hartwig.actin.clinical.curation.config

class ECGConfigFactory : CurationConfigFactory<ECGConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ECGConfig {
        val isQTCF = parts[fields["isQTCF"]!!] == "1"
        val (qtcfValue: Int?, qtcfUnit: String?) = extractMeasurement("qtcf", isQTCF, parts, fields)
        val isJTC = parts[fields["isJTC"]!!] == "1"
        val (jtcValue: Int?, jtcUnit: String?) = extractMeasurement("jtc", isJTC, parts, fields)
        val interpretation = parts[fields["interpretation"]!!]
        return ECGConfig(
            input = parts[fields["input"]!!],
            ignore = interpretation == "NULL",
            interpretation = interpretation,
            isQTCF = isQTCF,
            qtcfValue = qtcfValue,
            qtcfUnit = qtcfUnit,
            isJTC = isJTC,
            jtcValue = jtcValue,
            jtcUnit = jtcUnit
        )
    }

    private fun extractMeasurement(
        measurementPrefix: String, isOfType: Boolean, parts: Array<String>, fields: Map<String, Int>
    ) = if (isOfType) {
        Pair(parts[fields["${measurementPrefix}Value"]!!].toInt(), parts[fields["${measurementPrefix}Unit"]!!])
    } else Pair(null, null)
}