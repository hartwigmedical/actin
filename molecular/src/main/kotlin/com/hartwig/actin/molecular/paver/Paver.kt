package com.hartwig.actin.molecular.paver

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.VariantEffect
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

        if (parts[7] != null && parts[7].isNotEmpty()) {
            logger.warn("Unexpected other reportable effects with no gene driver panel configured: {}", parts[7])
        }

        return PaveImpact(
            gene = parts[0],
            transcript = parts[1],
            canonicalEffect = parts[2],
            canonicalCodingEffect = interpretCodingEffect(parts[3]),
            spliceRegion = interpretSpliceRegion(parts[4]),
            hgvsCodingImpact = parts[5],
            hgvsProteinImpact = parts[6],
            worstCodingEffect = interpretCodingEffect(parts[8]),
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
                    spliceRegion = it[4],
                    hgvsCodingImpact = it[5],
                    hgvsProteinImpact = it[6])
            }
    }

    private fun interpretCodingEffect(codingEffect: String): CodingEffect {
        return when (codingEffect) {
            "NONSENSE_OR_FRAMESHIFT" -> CodingEffect.NONSENSE_OR_FRAMESHIFT
            "SPLICE" -> CodingEffect.SPLICE
            "MISSENSE" -> CodingEffect.MISSENSE
            "SYNONYMOUS" -> CodingEffect.SYNONYMOUS
            else -> {
                logger.warn("Unexpected coding effect, using NONE: {}", codingEffect)
                CodingEffect.NONE
            }
        }
    }

    private fun interpretSpliceRegion(spliceRegion: String): Boolean {
        return when (spliceRegion) {
            "true" -> true
            "false" -> false
            else -> {
                logger.warn("Unexpected splice region value, using false: {}", spliceRegion)
                false
            }
        }
    }

    private fun interpretVariantEffects(variantEffects: String): List<VariantEffect> {
        if (variantEffects.isEmpty()) {
            return emptyList()
        }

        return variantEffects.split("&")
            .map { interpretVariantEffect(it) }
    }

    private fun interpretVariantEffect(variantEffect: String): VariantEffect {
        return when (variantEffect) {
            "stop_gained" -> VariantEffect.STOP_GAINED
            "stop_lost" -> VariantEffect.STOP_LOST
            "start_lost" -> VariantEffect.START_LOST
            "frameshift" -> VariantEffect.FRAMESHIFT
            "splice_acceptor_variant" -> VariantEffect.SPLICE_ACCEPTOR
            "splice_donor_variant" -> VariantEffect.SPLICE_DONOR
            "inframe_insertion" -> VariantEffect.INFRAME_INSERTION
            "inframe_deletion" -> VariantEffect.INFRAME_DELETION
            "missense_variant" -> VariantEffect.MISSENSE
            "phased_missense" -> VariantEffect.PHASED_MISSENSE
            "phased_inframe_insertion" -> VariantEffect.PHASED_INFRAME_INSERTION
            "phased_inframe_deletion" -> VariantEffect.PHASED_INFRAME_DELETION
            "synonymous_variant" -> VariantEffect.SYNONYMOUS
            "phased_synonymous" -> VariantEffect.PHASED_SYNONYMOUS
            "intron_variant" -> VariantEffect.INTRONIC
            "5_prime_UTR_variant" -> VariantEffect.FIVE_PRIME_UTR
            "3_prime_UTR_variant" -> VariantEffect.THREE_PRIME_UTR
            "upstream_gene_variant" -> VariantEffect.UPSTREAM_GENE
            "non_coding_transcript_exon_variant" -> VariantEffect.NON_CODING_TRANSCRIPT
            else -> {
                logger.warn("Unexpected variant effect, using OTHER: {}", variantEffect)
                VariantEffect.OTHER
            }
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

    private fun indexForVcf(vcfFilename: String): String {
        return "$vcfFilename.tbi"
    }
}

private fun chromToIndex(chrom: String): Int {
    return when (chrom) {
        "X" -> 23
        "Y" -> 24
        "MT" -> 25
        else -> chrom.toInt()
    }
}