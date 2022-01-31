package com.hartwig.actin.molecular.orange.datamodel;

// All values are required for deserialization of ORANGE json
public enum EvidenceType {
    VIRAL_PRESENCE,
    SIGNATURE,
    ACTIVATION,
    INACTIVATION,
    AMPLIFICATION,
    DELETION,
    PROMISCUOUS_FUSION,
    FUSION_PAIR,
    HOTSPOT_MUTATION,
    CODON_MUTATION,
    EXON_MUTATION,
    ANY_MUTATION
}
