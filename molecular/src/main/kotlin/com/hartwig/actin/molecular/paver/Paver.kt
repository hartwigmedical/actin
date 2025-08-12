package com.hartwig.actin.molecular.paver

import com.hartwig.actin.molecular.util.VCFWriterFactory
import com.hartwig.hmftools.common.utils.config.ConfigBuilder
import com.hartwig.hmftools.pave.PaveApplication
import com.hartwig.hmftools.pave.PaveConfig
import htsjdk.samtools.reference.IndexedFastaSequenceFile
import htsjdk.tribble.AbstractFeatureReader
import htsjdk.variant.variantcontext.Allele
import htsjdk.variant.variantcontext.VariantContext
import htsjdk.variant.variantcontext.VariantContextBuilder
import htsjdk.variant.vcf.VCFCodec
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Paths

class Paver(
    private val ensemblDataDir: String,
    private val refGenomeFasta: String,
    private val refGenomeVersion: PaveRefGenomeVersion,
    private val driverGenePanel: String,
    private val tempDir: String
) {

    private val logger: Logger = LogManager.getLogger(Paver::class.java)

    fun run(queries: List<PaveQuery>): List<PaveResponse> {
        val configBuilder = ConfigBuilder("Pave")
        PaveConfig.addConfig(configBuilder)

        val runId = generateRunId()
        val paveVcfQueryFile = paveVcfQueryFilename(runId)
        val paveVcfResponseFile = paveVcfResponseFilename(runId)

        buildPaveInputVcf(paveVcfQueryFile, queries)

        configBuilder.checkAndParseCommandLine(
            arrayOf(
                "-sample", "SAMPLE_ID",
                "-input_vcf", paveVcfQueryFile,
                "-ensembl_data_dir", ensemblDataDir,
                "-ref_genome", refGenomeFasta,
                "-ref_genome_version", refGenomeVersion.display(),
                "-driver_gene_panel", driverGenePanel,
                "-output_dir", tempDir,
            )
        )

        val paveApplication = PaveApplication(configBuilder)
        paveApplication.run()

        val response = loadPaveOutputVcf(paveVcfResponseFile)

        File(paveVcfQueryFile).delete()
        File(indexForVcf(paveVcfQueryFile)).delete()
        File(paveVcfResponseFile).delete()
        File(indexForVcf(paveVcfResponseFile)).delete()

        return response
    }

    private fun buildPaveInputVcf(vcfFile: String, queries: List<PaveQuery>) {
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

    private fun loadPaveOutputVcf(paveVcfFile: String): List<PaveResponse> {
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

    private fun generateRunId(): String {
        return java.util.UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8)
    }

    private fun paveVcfQueryFilename(runId: String): String {
        return Paths.get(tempDir, "actin-$runId.vcf.gz").toString()
    }

    private fun paveVcfResponseFilename(runId: String): String {
        return Paths.get(tempDir, "actin-$runId.pave.vcf.gz").toString()
    }

    private fun indexForVcf(vcfFilename: String): String {
        return "$vcfFilename.tbi"
    }
}

fun parsePaveImpact(impact: List<String>?): PaveImpact {
    if (impact.isNullOrEmpty()) {
        throw RuntimeException("Missing PAVE impact field")
    }

    if (impact.size != 10) {
        throw RuntimeException("Unexpected number of parts in PAVE impact field: ${impact.size}")
    }

    return PaveImpact(
        gene = impact[0],
        canonicalTranscript = impact[1],
        canonicalEffects = interpretVariantEffects(impact[2]),
        canonicalCodingEffect = PaveCodingEffect.valueOf(impact[3]),
        spliceRegion = interpretSpliceRegion(impact[4]),
        hgvsCodingImpact = impact[5],
        hgvsProteinImpact = impact[6],
        otherReportableEffects = impact[7].ifEmpty { null },
        worstCodingEffect = PaveCodingEffect.valueOf(impact[8]),
        genesAffected = impact[9].toInt(),
    )
}

fun parsePaveTranscriptImpact(impacts: List<String>?): List<PaveTranscriptImpact> {
    if (impacts.isNullOrEmpty()) {
        throw RuntimeException("Missing PAVE_TI field")
    }

    return impacts.map { it.split("|") }
        .map {
            if (it.size != 10) {
                throw RuntimeException("Unexpected number of parts in PAVE_TI field: ${it.size}")
            }

            PaveTranscriptImpact(
                geneId = it[0],
                gene = it[1],
                transcript = it[2],
                effects = interpretVariantEffects(it[3]),
                spliceRegion = interpretSpliceRegion(it[4]),
                hgvsCodingImpact = it[5],
                hgvsProteinImpact = it[6],
                refSeqId = it[7].ifEmpty { null },
                affectedExon = it[8].toIntOrNull() ?: throw RuntimeException("Invalid exon number: ${it[8]}"),
                affectedCodon = it[9].toIntOrNull() ?: throw RuntimeException("Invalid codon number: ${it[9]}"),
            )
        }
}

private fun interpretVariantEffects(variantEffects: String): List<PaveVariantEffect> {
    if (variantEffects.isEmpty()) {
        return emptyList()
    }

    return variantEffects.split("&")
        .map { PaveVariantEffect.fromString(it) }
}

fun interpretSpliceRegion(spliceRegion: String): Boolean {
    return when (spliceRegion) {
        "true" -> true
        "false" -> false
        else -> {
            throw RuntimeException("Unexpected splice region value: $spliceRegion")
        }
    }
}

fun chromToIndex(chrom: String): Int {
    return when (val cleanedChrom = chrom.replace("chr", "")) {
        "X" -> 23
        "Y" -> 24
        "MT" -> 25
        else -> cleanedChrom.toInt()
    }
}