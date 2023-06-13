package com.hartwig.actin.clinical.curation.config

class ECGConfigFactory : CurationConfigFactory<ECGConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): ECGConfig {
        val isQTCF = parts[fields["isQTCF"]!!] == "1"
        var qtcfValue: Int? = null
        var qtcfUnit: String? = null
        if (isQTCF) {
            qtcfValue = parts[fields["qtcfValue"]!!].toInt()
            qtcfUnit = parts[fields["qtcfUnit"]!!]
        }
        val isJTC = parts[fields["isJTC"]!!] == "1"
        var jtcValue: Int? = null
        var jtcUnit: String? = null
        if (isJTC) {
            jtcValue = parts[fields["jtcValue"]!!].toInt()
            jtcUnit = parts[fields["jtcUnit"]!!]
        }
        val interpretation = parts[fields["interpretation"]!!]
        return ImmutableECGConfig.builder()
            .input(parts[fields["input"]!!])
            .ignore(interpretation == "NULL")
            .interpretation(interpretation)
            .isQTCF(isQTCF)
            .qtcfValue(qtcfValue)
            .qtcfUnit(qtcfUnit)
            .isJTC(isJTC)
            .jtcValue(jtcValue)
            .jtcUnit(jtcUnit)
            .build()
    }
}