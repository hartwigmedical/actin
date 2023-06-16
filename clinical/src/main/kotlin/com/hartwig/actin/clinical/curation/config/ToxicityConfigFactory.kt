package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.util.ResourceFile

class ToxicityConfigFactory : CurationConfigFactory<ToxicityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ToxicityConfig {
        return ToxicityConfig(
            input = parts[fields["input"]!!],
            ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!]),
            name = parts[fields["name"]!!],
            categories = CurationUtil.toCategories(parts[fields["categories"]!!]),
            grade = ResourceFile.optionalInteger(parts[fields["grade"]!!])
        )
    }
}