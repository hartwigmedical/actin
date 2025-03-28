package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.characteristics.HrdType
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.report.pdf.util.Formats

object MolecularCharacteristicFormat {

    fun formatTumorMutationalBurden(molecularCharacteristics: MolecularCharacteristics, includeValue: Boolean = true) =
        "TMB " + formatHighLowCharacteristic(
            molecularCharacteristics.tumorMutationalBurden,
            molecularCharacteristics.hasHighTumorMutationalBurden,
            includeValue
        )

    fun formatTumorMutationalLoad(molecularCharacteristics: MolecularCharacteristics, includeValue: Boolean = true) =
        "TML " + formatHighLowCharacteristic(
            molecularCharacteristics.tumorMutationalLoad,
            molecularCharacteristics.hasHighTumorMutationalLoad,
            includeValue
        )

    fun formatHighLowCharacteristic(value: Number?, isHigh: Boolean?, includeValue: Boolean = true): String {
        return if (value == null && isHigh == null) {
            Formats.VALUE_UNKNOWN
        } else {
            isHigh?.let { i ->
                value?.let { v ->
                    "${formHighLow(i)}${if (includeValue) " (${Formats.singleDigitNumber(v)})" else ""}"
                }
            } ?: throw IllegalArgumentException("if tmb/tml value is null so must isHigh and vice-versa")
        }
    }

    private fun formHighLow(i: Boolean) = if (i) "High" else "Low"

    fun formatMicrosatelliteStability(molecularCharacteristics: MolecularCharacteristics): String {
        return molecularCharacteristics.isMicrosatelliteUnstable?.let { unstable -> if (unstable) "Unstable" else "Stable" }
            ?: Formats.VALUE_UNKNOWN
    }

    fun formatHomologousRecombination(molecularCharacteristics: MolecularCharacteristics, includeTypeInterpretation: Boolean = true): String {
        return molecularCharacteristics.isHomologousRecombinationDeficient?.let { isDeficient ->
            val statusInterpretation = if (isDeficient) "Deficient" else "Proficient"
            val scoreInterpretation = molecularCharacteristics.homologousRecombinationScore?.let { "(${Formats.twoDigitNumber(it)})" }

            val typeInterpretation = molecularCharacteristics.hrdType?.let { type ->
                when (type) {
                    HrdType.BRCA1_TYPE -> {
                        "- BRCA1-type (BRCA1 value: ${molecularCharacteristics.brca1Value?.let { Formats.twoDigitNumber(it) }})"
                    }

                    HrdType.BRCA2_TYPE -> {
                        "- BRCA2-type (BRCA2 value: ${molecularCharacteristics.brca2Value?.let { Formats.twoDigitNumber(it) }})"
                    }

                    HrdType.NONE, HrdType.CANNOT_BE_DETERMINED -> null
                }
            }?.takeIf { isDeficient }

            listOfNotNull(
                statusInterpretation,
                scoreInterpretation,
                if (includeTypeInterpretation) typeInterpretation else null
            ).joinToString(" ")
        } ?: Formats.VALUE_UNKNOWN
    }
}