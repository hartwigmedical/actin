package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.util.DatamodelPrinter
import org.apache.logging.log4j.LogManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
private val NUMBER_FORMAT = DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
private val PERCENTAGE_FORMAT = DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH))

class MolecularTestPrinter(private val printer: DatamodelPrinter) {

    private val logger = LogManager.getLogger(MolecularTestPrinter::class.java)

    fun print(tests: List<MolecularTest>) {
        logger.info("Printing ${tests.size} molecular tests")

        tests.forEach { print(it) }
    }
    
    private fun print(test: MolecularTest) {
        printer.print("Test: " + formatTestTypeDisplay(test.testTypeDisplay) + " for sample: '" + test.sampleId + "'")
        printer.print(" Experiment type '" + test.experimentType.display() + "' on " + formatDate(test.date))
        printer.print(" Contains tumor cells: " + toYesNoUnknown(test.containsTumorCells))
        printer.print(" Has sufficient quality and purity: " + toYesNoUnknown(test.hasSufficientQualityAndPurity()))
        with(test.characteristics) {
            printer.print(" Purity: " + formatPercentage(purity))
            printer.print(" Predicted tumor origin: " + predictedTumorString(predictedTumorOrigin))
            printer.print(" Microsatellite unstable?: " + toYesNoUnknown(microsatelliteStability?.isUnstable))
            printer.print(" Homologous recombination deficient?: " + toYesNoUnknown(homologousRecombination?.isDeficient))
            printer.print(" Tumor mutational burden: " + formatDouble(tumorMutationalBurden?.score))
            printer.print(" Tumor mutational load: " + formatInteger(tumorMutationalLoad?.score))
            printer.print(" Number of drivers: " + driverCount(test.drivers))
        }
        printEvidence(test)
    }

    private fun printEvidence(test: MolecularTest) {
        val aggregatedEvidence = AggregatedEvidenceFactory.create(test)
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

    private fun formatTestTypeDisplay(testTypeDisplay: String?): String {
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
}
