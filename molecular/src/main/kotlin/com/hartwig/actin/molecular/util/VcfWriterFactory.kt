package com.hartwig.actin.molecular.util

import com.hartwig.hmftools.common.genome.refgenome.RefGenomeFunctions.stripChrPrefix
import htsjdk.samtools.SAMSequenceDictionary
import htsjdk.samtools.reference.IndexedFastaSequenceFile
import htsjdk.variant.variantcontext.writer.Options
import htsjdk.variant.variantcontext.writer.VariantContextWriter
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder
import htsjdk.variant.vcf.VCFFilterHeaderLine
import htsjdk.variant.vcf.VCFHeader
import htsjdk.variant.vcf.VCFHeaderLine
import htsjdk.variant.vcf.VCFHeaderLineCount
import htsjdk.variant.vcf.VCFHeaderLineType
import htsjdk.variant.vcf.VCFInfoHeaderLine


object VCFWriterFactory {
    const val INPUT_FIELD: String = "input"

    fun openIndexedVCFWriter(outputVcf: String, refSequence: IndexedFastaSequenceFile): VariantContextWriter {
        val sequenceDictionary = refSequence.getSequenceDictionary()
        val writer = createBaseWriterBuilder(outputVcf).modifyOption(Options.INDEX_ON_THE_FLY, true)
            .setReferenceDictionary(sequenceDictionary)
            .build()

        val header = createBaseHeader()

        val condensedDictionary = SAMSequenceDictionary()
        for (sequence in sequenceDictionary.getSequences()) {
            if (isHumanChromosome(sequence.getContig())) {
                condensedDictionary.addSequence(sequence)
            }
        }

        header.setSequenceDictionary(condensedDictionary)
        writer.writeHeader(header)
        return writer
    }

    private fun createBaseWriterBuilder(outputVcf: String): VariantContextWriterBuilder {
        return VariantContextWriterBuilder().setOutputFile(outputVcf)
            .setOutputFileType(VariantContextWriterBuilder.OutputType.BLOCK_COMPRESSED_VCF)
            .modifyOption(Options.USE_ASYNC_IO, false)
    }

    private fun createBaseHeader(): VCFHeader {
        val header = VCFHeader(mutableSetOf<VCFHeaderLine?>(), mutableListOf<String?>())
        header.addMetaDataLine(VCFInfoHeaderLine(INPUT_FIELD,
            VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "Input mutation that generated this record"))

        header.addMetaDataLine(VCFFilterHeaderLine("PASS", "All filters passed"))

        return header
    }

    private val CHROMOSOMES = mutableListOf<String?>("1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "X",
        "Y",
        "M")

    private fun isHumanChromosome(chromosome: String): Boolean {
        return CHROMOSOMES.contains(stripChrPrefix(chromosome))
    }
}