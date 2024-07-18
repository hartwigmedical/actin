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

class Paver(private val config: PaverConfig) {
    private val logger: Logger = LogManager.getLogger(Paver::class.java)

    fun run(queries: List<PaveQuery>): List<PaveResponse> {
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
            "-ref_genome_version", config.refGenomeVersion.display(),
            "-driver_gene_panel", config.driverGenePanel,
            "-output_dir", config.tempDir,
        ))

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
                .id(query.id)
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

        val response = mutableListOf<PaveResponse>()
        for (variant in reader) {
            val paveImpact = extractPaveImpact(variant)
            val paveTranscriptImpact = extractPaveTranscriptImpact(variant)
            response.add(PaveResponse(variant.id, paveImpact, paveTranscriptImpact))
        }

        return response
    }

    private fun extractPaveImpact(variant: VariantContext): PaveImpact {
        val parts = variant.getAttributeAsStringList("IMPACT", "")
        if (parts == null || parts.isEmpty()) {
            throw RuntimeException("Missing PAVE impact field")
        }

        if (parts.size != 10) {
            throw RuntimeException("Unexpected number of parts in PAVE impact field: ${parts.size}")
        }

        return PaveImpact(
            gene = parts[0],
            transcript = parts[1],
            canonicalEffect = parts[2],
            canonicalCodingEffect = PaveCodingEffect.fromString(parts[3]),
            spliceRegion = interpretSpliceRegion(parts[4]),
            hgvsCodingImpact = parts[5],
            hgvsProteinImpact = parts[6],
            otherReportableEffects = parts[7].ifEmpty { null },
            worstCodingEffect = PaveCodingEffect.fromString(parts[8]),
            genesAffected = parts[9].toInt(),
        )
    }

    private fun extractPaveTranscriptImpact(variant: VariantContext): List<PaveTranscriptImpact> {
        val impacts = variant.getAttributeAsStringList("PAVE_TI", "")
        if (impacts == null || impacts.isEmpty()) {
            throw RuntimeException("Missing PAVE_TI field")
        }

        return impacts.map { it.split("|") }
            .map {
                if (it.size != 7) {
                    throw RuntimeException("Unexpected number of parts in PAVE_TI field: ${it.size}")
                }

                PaveTranscriptImpact(
                    gene = it[0],
                    geneName = it[1],
                    transcript = it[2],
                    effects = interpretVariantEffects(it[3]),
                    spliceRegion = interpretSpliceRegion(it[4]),
                    hgvsCodingImpact = it[5],
                    hgvsProteinImpact = it[6])
            }
    }

    private fun interpretSpliceRegion(spliceRegion: String): Boolean {
        return when (spliceRegion) {
            "true" -> true
            "false" -> false
            else -> {
                throw RuntimeException("Unexpected splice region value: $spliceRegion")
            }
        }
    }

    private fun interpretVariantEffects(variantEffects: String): List<PaveVariantEffect> {
        if (variantEffects.isEmpty()) {
            return emptyList()
        }

        return variantEffects.split("&")
            .map { PaveVariantEffect.fromString(it) }
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

    private fun indexForVcf(vcfFilename: String): String {
        return "$vcfFilename.tbi"
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