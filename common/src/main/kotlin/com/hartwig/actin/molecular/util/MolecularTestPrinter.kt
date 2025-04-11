package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.util.DatamodelPrinter
import com.hartwig.actin.util.DatamodelPrinter.Companion.withDefaultIndentation
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
private val NUMBER_FORMAT = DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
private val PERCENTAGE_FORMAT = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH))

class MolecularTestPrinter(private val printer: DatamodelPrinter) {

    fun printOrangeRecord(record: MolecularRecord) {
        printer.print("Test: ORANGE for sample: '" + record.sampleId + "'")
        printer.print(" Experiment type '" + record.experimentType.display() + "' on " + formatDate(record.date))
        printer.print(" Contains tumor cells: " + toYesNoUnknown(record.containsTumorCells))
        printer.print(" Has sufficient quality and purity: " + toYesNoUnknown(record.hasSufficientQualityAndPurity()))
        with(record.characteristics) {
            printer.print(" Purity: " + formatPercentage(purity))
            printer.print(" Predicted tumor origin: " + predictedTumorString(predictedTumorOrigin))
            printer.print(" Microsatellite unstable?: " + toYesNoUnknown(microsatelliteStability?.isUnstable))
            printer.print(" Homologous recombination deficient?: " + toYesNoUnknown(homologousRecombination?.isDeficient))
            printer.print(" Tumor mutational burden: " + formatDouble(tumorMutationalBurden?.score))
            printer.print(" Tumor mutational load: " + formatInteger(tumorMutationalLoad?.score))
            printer.print(" Number of drivers: " + driverCount(record.drivers))
        }
        printEvidence(record)
    }

    fun printPanelRecord(record: PanelRecord) {
        printer.print("Test: " + formatPanelTestTypeDisplay(record.testTypeDisplay))
        printer.print(" Experiment type '" + record.experimentType.display() + "' on " + formatDate(record.date))
        printer.print(" Number of genes tested: " + record.geneCoverage.size)
        printer.print(" Has sufficient purity: " + toYesNoUnknown(record.hasSufficientPurity))
        printer.print(" Has sufficient quality: " + toYesNoUnknown(record.hasSufficientQuality))
        with(record.characteristics) {
            printer.print(" Microsatellite unstable?: " + toYesNoUnknown(microsatelliteStability?.isUnstable))
            printer.print(" Homologous recombination deficient?: " + toYesNoUnknown(homologousRecombination?.isDeficient))
            printer.print(" Tumor mutational burden: " + formatDouble(tumorMutationalBurden?.score))
            printer.print(" Tumor mutational load: " + formatInteger(tumorMutationalLoad?.score))
            printer.print(" Number of drivers: " + driverCount(record.drivers))
        }
        printEvidence(record)
    }

    private fun printEvidence(molecular: MolecularTest) {
        val aggregatedEvidence = AggregatedEvidenceFactory.create(molecular)
        printer.print(" Events with evidence for approved treatment: " + keys(aggregatedEvidence.approvedTreatmentsPerEvent()))
        printer.print(" Events associated with external trials: " + keys(aggregatedEvidence.eligibleTrialsPerEvent))
        printer.print(
            " Events with evidence for on-label experimental treatment: " +
                    keys(aggregatedEvidence.onLabelExperimentalTreatmentPerEvent())
        )
        printer.print(
            " Events with evidence for off-label experimental treatment: " +
                    keys(aggregatedEvidence.offLabelExperimentalTreatmentsPerEvent())
        )
        printer.print(" Events with evidence for pre-clinical treatment: " + keys(aggregatedEvidence.preClinicalTreatmentsPerEvent()))
        printer.print(" Events with known resistance evidence: " + keys(aggregatedEvidence.knownResistantTreatmentsPerEvent()))
        printer.print(" Events with suspect resistance evidence: " + keys(aggregatedEvidence.suspectResistantTreatmentsPerEvent()))
    }

    private fun driverCount(drivers: Drivers): Int {
        return drivers.variants.size + drivers.copyNumbers.size + drivers.homozygousDisruptions.size +
                drivers.disruptions.size + drivers.fusions.size + drivers.viruses.size
    }

    private fun formatPanelTestTypeDisplay(testTypeDisplay: String?): String {
        return testTypeDisplay ?: "unknown test type display"
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
        } else {
            predictedTumorOrigin.cancerType() + " (" + formatPercentage(predictedTumorOrigin.likelihood()) + ")"
        }
    }

    private fun toYesNoUnknown(bool: Boolean?): String {
        return when (bool) {
            null -> {
                "unknown"
            }

            true -> {
                "yes"
            }

            else -> {
                "no"
            }
        }
    }

    private fun keys(map: Map<String, Any>): String {
        return concat(map.keys)
    }

    private fun concat(strings: Iterable<String>): String {
        return strings.joinToString(", ").ifEmpty { "None" }
    }

    companion object {
        fun printOrangeRecord(record: MolecularRecord) {
            MolecularTestPrinter(withDefaultIndentation()).printOrangeRecord(record)
        }

        fun printPanelRecord(record: PanelRecord) {
            MolecularTestPrinter(withDefaultIndentation()).printPanelRecord(record)
        }
    }
}
