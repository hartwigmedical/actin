package com.hartwig.actin.clinical.curation.config

data class SequencingTestResultConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val gene: String? = null,
    val hgvsProteinImpact: String? = null,
    val hgvsCodingImpact: String? = null,
    val exon: Int? = null,
    val codon: Int? = null,
    val transcript: String? = null,
    val fusionGeneUp: String? = null,
    val fusionGeneDown: String? = null,
    val amplifiedGene: String? = null,
    val deletedGene: String? = null,
    val exonSkipStart: Int? = null,
    val exonSkipEnd: Int? = null,
    val msi: Boolean? = null,
    val tmb: Double? = null,
    val noMutationsFound: Boolean? = null,
) : CurationConfig