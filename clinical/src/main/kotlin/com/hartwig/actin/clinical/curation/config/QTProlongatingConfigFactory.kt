package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk

class QTProlongatingConfigFactory : CurationConfigFactory<QTProlongatingConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<QTProlongatingConfig> {
        val input = parts[fields["Name"]!!]
        val (qtRisk, validationErrors) = validateMandatoryEnum(
            CurationCategory.QT_PROLONGATING,
            input,
            "Risk",
            fields,
            parts
        ) { QTProlongatingRisk.valueOf(it) }
        return ValidatedCurationConfig(
            QTProlongatingConfig(input, false, qtRisk ?: QTProlongatingRisk.UNKNOWN),
            validationErrors
        )
    }
}