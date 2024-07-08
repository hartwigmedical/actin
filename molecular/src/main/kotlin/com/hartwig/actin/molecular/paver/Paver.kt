package com.hartwig.actin.molecular.paver

import com.hartwig.actin.tools.validation.VCFWriterFactory
import com.hartwig.hmftools.common.utils.config.ConfigBuilder
import com.hartwig.hmftools.pave.PaveApplication
import com.hartwig.hmftools.pave.PaveConfig
import htsjdk.samtools.reference.IndexedFastaSequenceFile
import htsjdk.tribble.AbstractFeatureReader
import htsjdk.tribble.readers.LineIterator
import htsjdk.variant.variantcontext.Allele
import htsjdk.variant.variantcontext.VariantContext
import htsjdk.variant.variantcontext.VariantContextBuilder
import htsjdk.variant.vcf.VCFCodec
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Paths

class Paver(val config: PaverConfig) {
    private val LOGGER: Logger = LogManager.getLogger(Paver::class.java)

    fun pave(queries: List<PaveQuery>): List<PaveResponse> {
        val configBuilder = ConfigBuilder("Pave")
        PaveConfig.addConfig(configBuilder)

        val runId = generateRunId()
        val paveVcfQueryFile = paveVcfQueryFilename(runId)
        val paveVcfResponseFile = paveVcfResponseFilename(runId)

        buildPaveInputVcf(paveVcfQueryFile, queries)

        configBuilder.checkAndParseCommandLine(arrayOf(
            "-vcf_file", paveVcfQueryFile,
            "-ensembl_data_dir", config.ensemblDataDir,
            "-ref_genome", config.refGenomeFasta,
            "-ref_genome_version", config.refGenomeVersion,
            "-output_dir", config.tempDir,
        ))

        val paveApplication = PaveApplication(configBuilder)
        paveApplication.run()

        val response = loadPaveOutputVcf(paveVcfResponseFile)

        File(paveVcfQueryFile).delete()
        File(paveVcfResponseFile).delete()

        return response
    }

    private fun buildPaveInputVcf(vcfFile: String, queries: List<PaveQuery>) {
        LOGGER.debug("Writing {} variants to {}", queries.size, vcfFile)
        val refSequence: IndexedFastaSequenceFile = IndexedFastaSequenceFile(File(config.refGenomeFasta))
        val writer = VCFWriterFactory.openIndexedVCFWriter(vcfFile, refSequence);

        val sortedQueries = queries.sortedWith(
            compareBy<PaveQuery> { chromToIndex(it.chromosome) }.thenBy { it.position }
        )

        for (query in sortedQueries) {
            val alleles = listOf(Allele.create(query.ref, true), Allele.create(query.alt, false))
            val variant = VariantContextBuilder().noGenotypes()
                .source("TransVar")
                .chr(query.chromosome)
                .start(query.position.toLong())
                .alleles(alleles)
                .computeEndFromAlleles(alleles, query.position)
                .make()
            writer.add(variant)
        }

        writer.close()
    }

    private fun loadPaveOutputVcf(paveVcfFile: String): List<PaveResponse> {
        val reader =
            AbstractFeatureReader.getFeatureReader<VariantContext, LineIterator>(paveVcfFile, VCFCodec(), false)

        var response = mutableListOf<PaveResponse>()
        for (variant in reader) {
            val paveImpact = extractPaveImpact(variant)
            val paveTranscriptImpact = extractPaveTranscriptImpact(variant)
            response.add(PaveResponse(variant.id, paveImpact, paveTranscriptImpact))
        }

        return response
    }

    private fun extractPaveImpact(variant: VariantContext): PaveImpact {
        val parts = variant.getAttributeAsStringList("IMPACT", "")
        // TODO use vcf header attributes to make this safer?
        return PaveImpact(
            gene = parts[0],
            transcript = parts[1],
            canonicalEffect = parts[2],
            canonicalCodingEffect = parts[3],
            spliceRegion = parts[4],
            hgvsCodingImpact = parts[5],
            hgvsProteinImpact = parts[6],
            otherReportableEffects = parts[7],
            worstCodingEffect = parts[8],
            genesAffected = parts[9],
        )
    }

    private fun extractPaveTranscriptImpact(variant: VariantContext): List<PaveTranscriptImpact> {
        return variant.getAttributeAsStringList("PAVE_TI", "")
            .map { it.split("|") }
            .map {
                // TODO use vcf header?
                PaveTranscriptImpact(
                    gene = it[0],
                    geneName = it[1],
                    transcript = it[2],
                    effects = it[3],
                    spliceRegion = it[4],
                    hgvsCodingImpact = it[5],
                    hgvsProteinImpact = it[6])
            }
    }

    private fun generateRunId(): String {
        return java.util.UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8)
    }

    private fun paveVcfQueryFilename(runId: String): String {
        return Paths.get(config.tempDir, "actin-$runId.vcf.gz").toString()
    }

    private fun paveVcfResponseFilename(runId: String): String {
        return Paths.get(config.tempDir, "actin-$runId.pave.vcf.gz").toString()
    }
}

private fun chromToIndex(chrom: String): Int {
    return when (chrom) {
        "X" -> 23
        "Y" -> 24
        else -> chrom.toInt()
    }
}