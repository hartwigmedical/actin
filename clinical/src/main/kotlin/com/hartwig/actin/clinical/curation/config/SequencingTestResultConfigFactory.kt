package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.util.ResourceFile

class SequencingTestResultConfigFactory : CurationConfigFactory<SequencingTestResultConfig> {

    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SequencingTestResultConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["gene"]!!])
        val input = parts[fields["input"]!!]
        val (validatedVirus, sequencingTestResultValidationErrors) = validateOptionalEnum<VirusType>(
            CurationCategory.SEQUENCING_TEST_RESULT,
            input,
            "virus",
            fields,
            parts,
            setOf(VirusType.OTHER)
        ) { VirusType.valueOf(it) }

        return ValidatedCurationConfig(
            if (ignore) SequencingTestResultConfig(input = input, ignore = true) else {
                SequencingTestResultConfig(
                    input = input,
                    gene = ResourceFile.optionalString(parts[fields["gene"]!!]),
                    hgvsProteinImpact = ResourceFile.optionalString(parts[fields["hgvsProteinImpact"]!!]),
                    hgvsCodingImpact = ResourceFile.optionalString(parts[fields["hgvsCodingImpact"]!!]),
                    vaf = ResourceFile.optionalNumber(parts[fields["vaf"]!!]),
                    exon = ResourceFile.optionalInteger(parts[fields["exon"]!!]),
                    codon = ResourceFile.optionalInteger(parts[fields["codon"]!!]),
                    transcript = ResourceFile.optionalString(parts[fields["transcript"]!!]),
                    fusionGeneUp = ResourceFile.optionalString(parts[fields["fusionGeneUp"]!!]),
                    fusionGeneDown = ResourceFile.optionalString(parts[fields["fusionGeneDown"]!!]),
                    fusionExonUp = ResourceFile.optionalInteger(parts[fields["fusionExonUp"]!!]),
                    fusionExonDown = ResourceFile.optionalInteger(parts[fields["fusionExonDown"]!!]),
                    fusionTranscriptUp = ResourceFile.optionalString(parts[fields["fusionTranscriptUp"]!!]),
                    fusionTranscriptDown = ResourceFile.optionalString(parts[fields["fusionTranscriptDown"]!!]),
                    amplifiedGene = ResourceFile.optionalString(parts[fields["amplifiedGene"]!!]),
                    amplifiedGeneCopyNr = ResourceFile.optionalInteger(parts[fields["amplifiedGeneCopyNr"]!!]),
                    deletedGene = ResourceFile.optionalString(parts[fields["deletedGene"]!!]),
                    exonSkipStart = ResourceFile.optionalInteger(parts[fields["exonSkipStart"]!!]),
                    exonSkipEnd = ResourceFile.optionalInteger(parts[fields["exonSkipEnd"]!!]),
                    virus = validatedVirus,
                    virusIsLowRisk = ResourceFile.optionalBool(parts[fields["virusIsLowRisk"]!!]),
                    msi = ResourceFile.optionalBool(parts[fields["msi"]!!]),
                    tmb = ResourceFile.optionalNumber(parts[fields["tmb"]!!]),
                    hrd = ResourceFile.optionalBool(parts[fields["hrd"]!!]),
                    noMutationsFound = ResourceFile.optionalBool(parts[fields["noMutationsFound"]!!])
                )
            },
            sequencingTestResultValidationErrors
        )
    }
}