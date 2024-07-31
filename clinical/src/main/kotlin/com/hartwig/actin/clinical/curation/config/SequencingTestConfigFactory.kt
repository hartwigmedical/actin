package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.feed.standard.ProvidedMolecularTestResult
import com.hartwig.actin.util.ResourceFile

class SequencingTestConfigFactory : CurationConfigFactory<SequencingTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SequencingTestConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["gene"]!!])
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
            gene = ResourceFile.optionalString(parts[fields["gene"]!!]),
            hgvsProteinImpact = ResourceFile.optionalString(parts[fields["hgvsProteinImpact"]!!]),
            hgvsCodingImpact = ResourceFile.optionalString(parts[fields["hgvsCodingImpact"]!!]),
            exon = ResourceFile.optionalInteger(parts[fields["exon"]!!]),
            codon = ResourceFile.optionalInteger(parts[fields["codon"]!!]),
            transcript = ResourceFile.optionalString(parts[fields["transcript"]!!]),
            fusionGeneUp = ResourceFile.optionalString(parts[fields["fusionGeneUp"]!!]),
            fusionGeneDown = ResourceFile.optionalString(parts[fields["fusionGeneDown"]!!]),
            amplifiedGene = ResourceFile.optionalString(parts[fields["amplifiedGene"]!!]),
            exonSkipStart = ResourceFile.optionalInteger(parts[fields["exonSkipStart"]!!]),
            exonSkipEnd = ResourceFile.optionalInteger(parts[fields["exonSkipEnd"]!!]),
            msi = ResourceFile.optionalBool(parts[fields["msi"]!!]),
            tmb = ResourceFile.optionalNumber(parts[fields["tmb"]!!]),
            noMutationsFound = ResourceFile.optionalBool(parts[fields["noMutationsFound"]!!])
        )
    }
}