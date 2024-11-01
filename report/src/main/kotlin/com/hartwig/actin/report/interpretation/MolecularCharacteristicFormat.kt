package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.HrdType
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.report.pdf.util.Formats

object MolecularCharacteristicFormat {

    fun formatTumorMutationalBurden(molecularCharacteristics: MolecularCharacteristics) =
        interpret(molecularCharacteristics.tumorMutationalBurden, molecularCharacteristics.hasHighTumorMutationalBurden, "TMB")

    fun formatTumorMutationalLoad(molecularCharacteristics: MolecularCharacteristics) =
        interpret(molecularCharacteristics.tumorMutationalLoad, molecularCharacteristics.hasHighTumorMutationalLoad, "TML")

    fun formatMicrosatelliteStability(molecularCharacteristics: MolecularCharacteristics): String {
        return molecularCharacteristics.isMicrosatelliteUnstable?.let { unstable -> if (unstable) "Unstable" else "Stable" }
            ?: Formats.VALUE_UNKNOWN
    }

    fun formatHomologuousRepair(molecularCharacteristics: MolecularCharacteristics): String {
        return molecularCharacteristics.isHomologousRepairDeficient?.let { isDeficient ->
            val statusInterpretation = if (isDeficient) "Deficient" else "Proficient"
            val scoreInterpretation = molecularCharacteristics.homologousRepairScore?.let { "(${Formats.twoDigitNumber(it)})" }

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

            listOfNotNull(statusInterpretation, scoreInterpretation, typeInterpretation).joinToString(" ")
        } ?: Formats.VALUE_UNKNOWN
    }

    private fun interpret(value: Number?, isHigh: Boolean?, label: String): String {
        return if (value == null || isHigh == null) {
            Formats.VALUE_UNKNOWN
        } else {
            String.format(
                "%s %s (%s)",
                label,
                if (isHigh) "high" else "low",
                Formats.singleDigitNumber(value.toDouble())
            )
        }
    }

}