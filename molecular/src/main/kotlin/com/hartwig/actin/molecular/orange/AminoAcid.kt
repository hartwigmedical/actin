package com.hartwig.actin.molecular.orange

internal object AminoAcid {

    private val TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER = mapOf(
        "Ala" to "A", // Alanine
        "Cys" to "C", // Cysteine
        "Asp" to "D", // Aspartic Acid
        "Glu" to "E", // Glutamic Acid
        "Phe" to "F", // Phenylalanine
        "Gly" to "G", // Glycine
        "His" to "H", // Histidine
        "Ile" to "I", // Isoleucine
        "Lys" to "K", // Lysine
        "Leu" to "L", // Leucine
        "Met" to "M", // Methionine
        "Asn" to "N", // Asparagine
        "Pro" to "P", // Proline
        "Gln" to "Q", // Glutamine
        "Arg" to "R", // Arginine
        "Ser" to "S", // Serine
        "Thr" to "T", // Threonine
        "Val" to "V", // Valine
        "Trp" to "W", // Tryptophan
        "Tyr" to "Y", // Tyrosine
    )

    fun forceSingleLetterAminoAcids(impact: String): String {
        return TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER.entries.fold(impact) { acc, (key, value) -> acc.replace(key, value) }
    }
}