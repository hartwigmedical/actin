package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.util.ResourceFile

class SequencingTestConfigFactory : CurationConfigFactory<SequencingTestConfig> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SequencingTestConfig> {
        val name = parts[fields["name"]!!]
        val ignore = CurationUtil.isIgnoreString(name)
        val input = parts[fields["input"]!!]
        return ValidatedCurationConfig(
            SequencingTestConfig(
                input = input,
                ignore = ignore,
                curatedName = name,
                allowFromAnyLab = ResourceFile.optionalBool(parts[fields["allowFromAnyLab"]!!])
            )
        )
    }
}