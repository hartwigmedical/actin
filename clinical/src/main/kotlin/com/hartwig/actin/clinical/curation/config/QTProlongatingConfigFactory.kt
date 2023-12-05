package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class QTProlongatingConfigFactory : CurationConfigFactory<QTProlongatingConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<QTProlongatingConfig> {
        val riskText = parts[fields["Risk"]!!].trim().uppercase()
        return if (enumContains<QTProlongatingRisk>(riskText))
            ValidatedCurationConfig(
                QTProlongatingConfig(parts[fields["Name"]!!], false, QTProlongatingRisk.valueOf(riskText))
            )
        else
            ValidatedCurationConfig(
                QTProlongatingConfig(parts[fields["Name"]!!], false, QTProlongatingRisk.UNKNOWN),
                listOf(enumInvalid<QTProlongatingRisk>(riskText))
            )
    }
}