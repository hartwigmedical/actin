package com.hartwig.actin.datamodel.molecular.driver

enum class VariantEffect {
    STOP_GAINED,
    STOP_LOST,
    START_LOST,
    FRAMESHIFT,
    SPLICE_ACCEPTOR,
    SPLICE_DONOR,
    INFRAME_INSERTION,
    INFRAME_DELETION,
    MISSENSE,
    PHASED_MISSENSE,
    PHASED_INFRAME_INSERTION,
    PHASED_INFRAME_DELETION,
    SYNONYMOUS,
    PHASED_SYNONYMOUS,
    INTRONIC,
    FIVE_PRIME_UTR,
    THREE_PRIME_UTR,
    UPSTREAM_GENE,
    NON_CODING_TRANSCRIPT,
    OTHER
}
