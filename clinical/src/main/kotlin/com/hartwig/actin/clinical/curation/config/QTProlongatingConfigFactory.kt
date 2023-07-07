package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class QTProlongatingConfigFactory : CurationConfigFactory<QTProlongatingConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): QTProlongatingConfig {
        val acceptedValues = QTProlongatingRisk.values().map { it.name }
        val riskText = parts[fields["Risk"]!!].trim().uppercase()
        val risk = if (riskText in acceptedValues) QTProlongatingRisk.valueOf(riskText) else
            throw IllegalStateException("Risk status of $riskText is not an acceptable value. Values can be [$acceptedValues]")
        return QTProlongatingConfig(parts[fields["Name"]!!], false, risk)
    }
}