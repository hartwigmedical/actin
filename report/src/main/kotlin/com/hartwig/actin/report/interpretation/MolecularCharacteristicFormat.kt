package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.report.pdf.util.Formats

object MolecularCharacteristicFormat {

    fun formatTumorMutationalBurden(molecularCharacteristics: MolecularCharacteristics, includeValue: Boolean = true) =
        "TMB " + formatHighLowCharacteristic(
            molecularCharacteristics.tumorMutationalBurden?.score,
            molecularCharacteristics.tumorMutationalBurden?.isHigh,
            includeValue
        )

    fun formatTumorMutationalLoad(molecularCharacteristics: MolecularCharacteristics, includeValue: Boolean = true) =
        "TML " + formatHighLowCharacteristic(
            molecularCharacteristics.tumorMutationalLoad?.score,
            molecularCharacteristics.tumorMutationalLoad?.isHigh,
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
        return molecularCharacteristics.microsatelliteStability?.isUnstable?.let { isUnstable -> if (isUnstable) "Unstable" else "Stable" }
            ?: Formats.VALUE_UNKNOWN
    }

    fun formatHomologousRecombination(
        molecularCharacteristics: MolecularCharacteristics,
        includeTypeInterpretation: Boolean = true
    ): String {
        return molecularCharacteristics.homologousRecombination?.isDeficient?.let { isDeficient ->
            val statusInterpretation = if (isDeficient) "Deficient" else "Proficient"
            val scoreInterpretation = molecularCharacteristics.homologousRecombination?.score?.let { "(${Formats.twoDigitNumber(it)})" }

            val typeInterpretation = molecularCharacteristics.homologousRecombination?.type?.let { type ->
                when (type) {
                    HomologousRecombinationType.BRCA1_TYPE -> {
                        "- BRCA1-type (BRCA1 value: ${
                            molecularCharacteristics.homologousRecombination?.brca1Value?.let {
                                Formats.twoDigitNumber(
                                    it
                                )
                            }
                        })"
                    }

                    HomologousRecombinationType.BRCA2_TYPE -> {
                        "- BRCA2-type (BRCA2 value: ${
                            molecularCharacteristics.homologousRecombination?.brca2Value?.let {
                                Formats.twoDigitNumber(
                                    it
                                )
                            }
                        })"
                    }

                    HomologousRecombinationType.NONE, HomologousRecombinationType.CANNOT_BE_DETERMINED -> null
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