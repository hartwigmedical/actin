package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.TumorDetails
import java.time.LocalDate
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.logging.log4j.LogManager

class BuildClinicalFromDoidsOnly(private val command: CommandLine) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        val patientId = command.getOptionValue(PATIENT)
        val doids = toStringSet(command.getOptionValue(PRIMARY_TUMOR_DOIDS))

        LOGGER.info("Creating clinical record for {} with doids {}", patientId, doids)
        val record = createRecord(patientId, doids)
        val outputDirectory = command.getOptionValue(OUTPUT_DIRECTORY)

        LOGGER.info("Writing clinical record for {} to {}", patientId, outputDirectory)
        ClinicalRecordJson.write(listOf(record), outputDirectory)

        LOGGER.info("Done!")
    }

    private fun createRecord(patientId: String, doids: Set<String>): ClinicalRecord {
        return ClinicalRecord(
            patientId = patientId,
            patient = PatientDetails(
                gender = Gender.FEMALE,
                birthYear = LocalDate.now().year,
                registrationDate = LocalDate.now(),
                questionnaireDate = null,
                hasHartwigSequencing = false
            ),
            tumor = TumorDetails(doids = doids),
            clinicalStatus = ClinicalStatus(),
            oncologicalHistory = emptyList(),
            priorSecondPrimaries = emptyList(),
            comorbidities = emptyList(),
            priorIHCTests = emptyList(),
            priorSequencingTests = emptyList(),
            labValues = emptyList(),
            surgeries = emptyList(),
            bodyWeights = emptyList(),
            bodyHeights = emptyList(),
            vitalFunctions = emptyList(),
            bloodTransfusions = emptyList(),
            medications = emptyList()
        )
    }

    private fun toStringSet(paramValue: String): Set<String> {
        return if (paramValue.isNotEmpty()) {
            paramValue.split(SEPARATOR).dropLastWhile { it.isEmpty() }.toSet()
        } else emptySet()
    }

    companion object {
        const val APPLICATION = "ACTIN Build Clinical From Doids Only"

        private val LOGGER = LogManager.getLogger(BuildClinicalFromDoidsOnly::class.java)
        private val VERSION = BuildClinicalFromDoidsOnly::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"

        private const val SEPARATOR = ";"
    }
}

fun main(args: Array<String>) {
    val options = createOptions()
    BuildClinicalFromDoidsOnly(DefaultParser().parse(options, args)).run()
}

private const val OUTPUT_DIRECTORY = "output_directory"
private const val PATIENT = "patient"
private const val PRIMARY_TUMOR_DOIDS = "primary_tumor_doids"

private fun createOptions(): Options {
    val options = Options()
    options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to")
    options.addOption(PATIENT, true, "The patient for which clinical data is generated")
    options.addOption(PRIMARY_TUMOR_DOIDS, true, "A semicolon-separated list of DOIDs representing the primary tumor of patient.")
    return options
}