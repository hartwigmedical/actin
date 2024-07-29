package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.feed.standard.ProvidedMolecularTestResult

class SequencingTestConfigFactory : CurationConfigFactory<SequencingTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SequencingTestConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["test"]!!])
        val input = parts[fields["input"]!!]
        return ValidatedCurationConfig(
            SequencingTestConfig(
                input = input,
                ignore = ignore,
                curated = if (!ignore) {
                    curateObject(fields, parts)
                } else null
            )
        )
    }

    private fun curateObject(
        fields: Map<String, Int>,
        parts: Array<String>
    ): ProvidedMolecularTestResult {
        return ProvidedMolecularTestResult(
            gene = parts[fields["item"]!!],
        )
    }
}