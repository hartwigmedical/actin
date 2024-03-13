package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory.create
import com.hartwig.actin.util.DatamodelPrinter
import com.hartwig.actin.util.DatamodelPrinter.Companion.withDefaultIndentation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MolecularPrinter(private val printer: DatamodelPrinter) {

    fun print(record: MolecularRecord) {
        printer.print("Sample: " + record.sampleId)
        printer.print(" Experiment type '" + record.type.display() + "' on " + formatDate(record.date))
        printer.print(" Contains tumor cells: " + toYesNoUnknown(record.containsTumorCells))
        printer.print(" Has sufficient quality and purity: " + toYesNoUnknown(record.hasSufficientQualityAndPurity))
        printer.print(" Purity: " + formatPercentage(record.characteristics.purity))
        printer.print(" Predicted tumor origin: " + predictedTumorString(record.characteristics.predictedTumorOrigin))
        printer.print(" Microsatellite unstable?: " + toYesNoUnknown(record.characteristics.isMicrosatelliteUnstable))
        printer.print(" Homologous repair deficient?: " + toYesNoUnknown(record.characteristics.isHomologousRepairDeficient))
        printer.print(" Tumor mutational burden: " + formatDouble(record.characteristics.tumorMutationalBurden))
        printer.print(" Tumor mutational load: " + formatInteger(record.characteristics.tumorMutationalLoad))
        val evidence = create(record)
        printer.print(" Events with evidence for approved treatment: " + keys(evidence.approvedTreatmentsPerEvent))
        printer.print(" Events associated with external trials: " + keys(evidence.externalEligibleTrialsPerEvent))
        printer.print(
            " Events with evidence for on-label experimental treatment: " + keys(evidence.onLabelExperimentalTreatmentsPerEvent)
        )
        printer.print(
            " Events with evidence for off-label experimental treatment: " + keys(evidence.offLabelExperimentalTreatmentsPerEvent)
        )
        printer.print(" Events with evidence for pre-clinical treatment: " + keys(evidence.preClinicalTreatmentsPerEvent))
        printer.print(" Events with known resistance evidence: " + keys(evidence.knownResistantTreatmentsPerEvent))
        printer.print(" Events with suspect resistance evidence: " + keys(evidence.suspectResistantTreatmentsPerEvent))
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        private val NUMBER_FORMAT = DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
        private val PERCENTAGE_FORMAT = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH))

        @JvmStatic
        fun printRecord(record: MolecularRecord) {
            MolecularPrinter(withDefaultIndentation()).print(record)
        }

        private fun formatDate(date: LocalDate?): String {
            return if (date != null) DATE_FORMAT.format(date) else "unknown date"
        }

        private fun formatDouble(number: Double?): String {
            return if (number != null) NUMBER_FORMAT.format(number) else "unknown"
        }

        private fun formatPercentage(percentage: Double?): String {
            return if (percentage != null) PERCENTAGE_FORMAT.format(percentage * 100) else "unknown"
        }

        private fun formatInteger(integer: Int?): String {
            return integer?.toString() ?: "unknown"
        }

        private fun predictedTumorString(predictedTumorOrigin: PredictedTumorOrigin?): String {
            return if (predictedTumorOrigin == null) {
                "Not determined"
            } else predictedTumorOrigin.cancerType() + " (" + formatPercentage(predictedTumorOrigin.likelihood()) + ")"
        }

        private fun toYesNoUnknown(bool: Boolean?): String {
            return when (bool) {
                null -> {
                    "Unknown"
                }
                true -> {
                    "Yes"
                }
                else -> {
                    "No"
                }
            }
        }

        private fun keys(map: Map<String, Any>): String {
            return concat(map.keys)
        }

        private fun concat(strings: Iterable<String>): String {
            return strings.joinToString(", ").ifEmpty { "None" }
        }
    }
}
