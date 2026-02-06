package com.hartwig.actin.molecular.paver

import com.hartwig.actin.tools.validation.VCFWriterFactory
import htsjdk.samtools.reference.IndexedFastaSequenceFile
import htsjdk.tribble.AbstractFeatureReader
import htsjdk.variant.variantcontext.Allele
import htsjdk.variant.variantcontext.VariantContext
import htsjdk.variant.variantcontext.VariantContextBuilder
import htsjdk.variant.vcf.VCFCodec

import java.io.File

private const val LOCAL_PHASE_SET_FIELD = "LPS"

object PaveVcf {

    fun write(vcfFile: String, queries: List<PaveQuery>, refGenomeFasta: String) {
        val refSequence = IndexedFastaSequenceFile(File(refGenomeFasta))
        VCFWriterFactory.openIndexedVCFWriter(vcfFile, refSequence).use { writer ->
            val sortedQueries = queries.sortedWith(
                compareBy<PaveQuery> { chromToIndex(it.chromosome) }.thenBy { it.position }
            )

            for (query in sortedQueries) {
                val alleles = listOf(Allele.create(query.ref, true), Allele.create(query.alt, false))
                val variant = VariantContextBuilder()
                    .noGenotypes()
                    .source("TransVar")
                    .chr(query.chromosome)
                    .start(query.position.toLong())
                    .id(query.id)
                    .alleles(alleles)
                    .computeEndFromAlleles(alleles, query.position)
                    .apply { query.localPhaseSet?.let { attribute(LOCAL_PHASE_SET_FIELD, it) } }
                    .make()

                writer.add(variant)
            }
        }
    }

    fun read(paveVcfFile: String): List<PaveResponse> {
        AbstractFeatureReader.getFeatureReader(paveVcfFile, VCFCodec(), false).use { reader ->
            val response = mutableListOf<PaveResponse>()
            for (variant in reader) {
                val paveImpact = extractPaveImpact(variant)
                val paveTranscriptImpact = extractPaveTranscriptImpact(variant)
                val localPhaseSet = if (variant.hasAttribute(LOCAL_PHASE_SET_FIELD)) {
                    variant.getAttributeAsInt(LOCAL_PHASE_SET_FIELD, -1).takeIf { it >= 0 }
                } else {
                    null
                }
                response.add(PaveResponse(variant.id, paveImpact, paveTranscriptImpact, localPhaseSet))
            }
            return response
        }
    }

    private fun extractPaveImpact(variant: VariantContext): PaveImpact {
        val impact = variant.getAttributeAsStringList("IMPACT", "")
        return parsePaveImpact(impact)
    }

    private fun extractPaveTranscriptImpact(variant: VariantContext): List<PaveTranscriptImpact> {
        val impacts = variant.getAttributeAsStringList("PAVE_TI", "")
        return parsePaveTranscriptImpact(impacts)
    }
}
