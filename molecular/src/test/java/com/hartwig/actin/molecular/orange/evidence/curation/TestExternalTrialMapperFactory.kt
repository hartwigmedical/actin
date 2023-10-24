package com.hartwig.actin.molecular.orange.evidence.curation

import com.google.common.collect.Lists

object TestExternalTrialMapperFactory {
    fun createMinimalTestMapper(): ExternalTrialMapper {
        return ExternalTrialMapper(Lists.newArrayList())
    }

    fun create(externalTrial: String, actinTrial: String): ExternalTrialMapper {
        val mapping: ExternalTrialMapping? = ImmutableExternalTrialMapping.builder().externalTrial(externalTrial).actinTrial(actinTrial).build()
        return ExternalTrialMapper(Lists.newArrayList(mapping))
    }
}
