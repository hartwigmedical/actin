package com.hartwig.actin.datamodel.molecular.evidence

enum class EvidenceType(private val display: String, private val categoryEvent: Boolean) {
    VIRAL_PRESENCE("Viral", false),
    SIGNATURE("Signature", false),
    ACTIVATION("Activation", true),
    INACTIVATION("Inactivation", true),
    AMPLIFICATION("Amplification", false),
    OVER_EXPRESSION("Over expression", false),
    PRESENCE_OF_PROTEIN("Presence of protein", false),
    DELETION("Deletion", true),
    UNDER_EXPRESSION("Under expression", false),
    ABSENCE_OF_PROTEIN("Absence of protein", false),
    PROMISCUOUS_FUSION("Promiscuous fusion", true),
    FUSION_PAIR("Fusion pair", false),
    HOTSPOT_MUTATION("Hotspot", false),
    CODON_MUTATION("Codon", true),
    EXON_MUTATION("Exon", true),
    ANY_MUTATION("Any mutation", true),
    WILD_TYPE("Wild-type", true),
    HLA("hla", false);

    fun display(): String {
        return display
    }

    fun isCategoryEvent() : Boolean {
        return categoryEvent;
    }
}