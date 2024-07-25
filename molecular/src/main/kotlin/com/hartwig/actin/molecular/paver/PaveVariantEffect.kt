package com.hartwig.actin.molecular.paver

enum class PaveVariantEffect(private val text: String) {
    STOP_GAINED("stop_gained"),
    STOP_LOST("stop_lost"),
    START_LOST("start_lost"),
    FRAMESHIFT("frameshift_variant"),
    SPLICE_ACCEPTOR("splice_acceptor_variant"),
    SPLICE_DONOR("splice_donor_variant"),
    INFRAME_INSERTION("inframe_insertion"),
    INFRAME_DELETION("inframe_deletion"),
    MISSENSE("missense_variant"),
    PHASED_INFRAME_INSERTION("phased_inframe_insertion"),
    PHASED_INFRAME_DELETION("phased_inframe_deletion"),
    PHASED_MISSENSE("phased_missense"),
    SYNONYMOUS("synonymous_variant"),
    PHASED_SYNONYMOUS("phased_synonymous"),
    INTRONIC("intron_variant"),
    FIVE_PRIME_UTR("5_prime_UTR_variant"),
    THREE_PRIME_UTR("3_prime_UTR_variant"),
    UPSTREAM_GENE("upstream_gene_variant"),
    NON_CODING_TRANSCRIPT("non_coding_transcript_exon_variant"),
    OTHER("other");

    companion object {
        private val textToEnum = values().associateBy(PaveVariantEffect::text)

        fun fromString(text: String): PaveVariantEffect {
            return textToEnum[text] ?: throw IllegalArgumentException("Unknown PAVE variant effect: $text")
        }
    }
}