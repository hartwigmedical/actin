package com.hartwig.actin.molecular.orange.interpretation

import com.google.common.collect.Maps

internal object AminoAcid {
    private val TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER: MutableMap<String, String> = Maps.newHashMap()

    init {
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Ala"] = "A" // Alanine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Cys"] = "C" // Cysteine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Asp"] = "D" // Aspartic Acid
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Glu"] = "E" // Glutamic Acid
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Phe"] = "F" // Phenylalanine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Gly"] = "G" // Glycine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["His"] = "H" // Histidine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Ile"] = "I" // Isoleucine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Lys"] = "K" // Lysine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Leu"] = "L" // Leucine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Met"] = "M" // Methionine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Asn"] = "N" // Asparagine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Pro"] = "P" // Proline
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Gln"] = "Q" // Glutamine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Arg"] = "R" // Arginine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Ser"] = "S" // Serine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Thr"] = "T" // Threonine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Val"] = "V" // Valine
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Trp"] = "W" // Tryptophan
        TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER["Tyr"] = "Y" // Tyrosine
    }

    fun forceSingleLetterAminoAcids(impact: String): String {
        var convertedImpact: String = impact
        for ((key, value) in TRI_LETTER_AMINO_ACID_TO_SINGLE_LETTER) {
            convertedImpact = convertedImpact.replace(key.toRegex(), value)
        }
        return convertedImpact
    }
}
