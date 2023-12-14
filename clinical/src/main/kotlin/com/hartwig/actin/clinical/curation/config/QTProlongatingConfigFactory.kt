package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class QTProlongatingConfigFactory : CurationConfigFactory<QTProlongatingConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<QTProlongatingConfig> {
        val riskText = parts[fields["Risk"]!!].trim().uppercase()
        val (qtRisk, validationErrors) = validateEnum(riskText) { QTProlongatingRisk.valueOf(it) }
        return ValidatedCurationConfig(
            QTProlongatingConfig(parts[fields["Name"]!!], false, qtRisk ?: QTProlongatingRisk.UNKNOWN),
            validationErrors
        )
    }
}