package com.hartwig.actin.datamodel.molecular.evidence

enum class EvidenceType(private val display: String) {
    VIRAL_PRESENCE("Viral"),
    SIGNATURE("Signature"),
    ACTIVATION("Activation"),
    INACTIVATION("Inactivation"),
    AMPLIFICATION("Amplification"),
    OVER_EXPRESSION("Over expression"),
    PRESENCE_OF_PROTEIN("Presence of protein"),
    DELETION("Deletion"),
    UNDER_EXPRESSION("Under expression"),
    ABSENCE_OF_PROTEIN("Absence of protein"),
    PROMISCUOUS_FUSION("Promiscuous fusion"),
    FUSION_PAIR("Fusion pair"),
    HOTSPOT_MUTATION("Hotspot"),
    CODON_MUTATION("Codon"),
    EXON_MUTATION("Exon"),
    ANY_MUTATION("Any mutation"),
    WILD_TYPE("Wild-type"),
    HLA("hla");

    fun display(): String {
        return display
    }
}