package com.hartwig.actin.molecular.paver

import com.hartwig.actin.tools.validation.VCFWriterFactory
import htsjdk.samtools.reference.IndexedFastaSequenceFile
import htsjdk.tribble.AbstractFeatureReader
import htsjdk.variant.variantcontext.Allele
import htsjdk.variant.variantcontext.VariantContext
import htsjdk.variant.variantcontext.VariantContextBuilder
import htsjdk.variant.vcf.VCFCodec
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

object PaveVcf {
    private val logger: Logger = LogManager.getLogger(Paver::class.java)

    fun writePaveInputVcf(vcfFile: String, queries: List<PaveQuery>, refGenomeFasta: String) {
        logger.debug("Writing {} variants to {}", queries.size, vcfFile)
        val refSequence = IndexedFastaSequenceFile(File(refGenomeFasta))
        val writer = VCFWriterFactory.openIndexedVCFWriter(vcfFile, refSequence)

        val sortedQueries = queries.sortedWith(
            compareBy<PaveQuery> { chromToIndex(it.chromosome) }.thenBy { it.position }
        )

        for (query in sortedQueries) {
            val alleles = listOf(Allele.create(query.ref, true), Allele.create(query.alt, false))
            val variant = VariantContextBuilder().noGenotypes()
                .source("TransVar")
                .chr(query.chromosome)
                .start(query.position.toLong())
                .id(query.id)
                .alleles(alleles)
                .computeEndFromAlleles(alleles, query.position)
                .make()
            writer.add(variant)
        }

        writer.close()
    }

     fun loadPaveOutputVcf(paveVcfFile: String): List<PaveResponse> {
        val reader = AbstractFeatureReader.getFeatureReader(paveVcfFile, VCFCodec(), false)

        val response = mutableListOf<PaveResponse>()
        for (variant in reader) {
            val paveImpact = extractPaveImpact(variant)
            val paveTranscriptImpact = extractPaveTranscriptImpact(variant)
            response.add(PaveResponse(variant.id, paveImpact, paveTranscriptImpact))
        }

        return response
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