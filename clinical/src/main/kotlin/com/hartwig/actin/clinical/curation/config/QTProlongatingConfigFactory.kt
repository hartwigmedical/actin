package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class QTProlongatingConfigFactory : CurationConfigFactory<QTProlongatingConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<QTProlongatingConfig> {
        val input = parts[fields["Name"]!!]
        val riskText = parts[fields["Risk"]!!].trim().uppercase()
        val (qtRisk, validationErrors) = validateEnum(riskText, input) { QTProlongatingRisk.valueOf(it) }
        return ValidatedCurationConfig(
            QTProlongatingConfig(input, false, qtRisk ?: QTProlongatingRisk.UNKNOWN),
            validationErrors
        )
    }
}