package com.hartwig.actin.clinical

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.time.LocalDate

class BuildClinicalFromDoidsOnly(private val command: CommandLine) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        val patientId = command.getOptionValue(PATIENT)
        val doids = toStringSet(command.getOptionValue(PRIMARY_TUMOR_DOIDS), ";")
        LOGGER.info("Creating clinical record for {} with doids {}", patientId, doids)
        val record = createRecord(patientId, doids)
        val outputDirectory = command.getOptionValue(OUTPUT_DIRECTORY)
        LOGGER.info("Writing clinical record for {} to {}", patientId, outputDirectory)
        ClinicalRecordJson.write(Lists.newArrayList(record), outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(BuildClinicalFromDoidsOnly::class.java)
        private const val OUTPUT_DIRECTORY = "output_directory"
        private const val PATIENT = "patient"
        private const val PRIMARY_TUMOR_DOIDS = "primary_tumor_doids"
        private const val APPLICATION = "ACTIN Build Clinical From Doids Only"
        private val VERSION = BuildClinicalFromDoidsOnly::class.java.getPackage().implementationVersion

        @Throws(IOException::class, ParseException::class)
        fun main(args: Array<String>) {
            val options = createOptions()
            BuildClinicalFromDoidsOnly(DefaultParser().parse(options, args)).run()
        }

        private fun createRecord(patientId: String, doids: Set<String>): ClinicalRecord {
            return ImmutableClinicalRecord.builder()
                .patientId(patientId)
                .patient(
                    ImmutablePatientDetails.builder()
                        .gender(Gender.FEMALE)
                        .birthYear(LocalDate.now().year)
                        .registrationDate(LocalDate.now())
                        .build()
                )
                .tumor(ImmutableTumorDetails.builder().doids(doids).build())
                .clinicalStatus(ImmutableClinicalStatus.builder().build())
                .build()
        }

        private fun createOptions(): Options {
            val options = Options()
            options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to")
            options.addOption(PATIENT, true, "The patient for which clinical data is generated")
            options.addOption(PRIMARY_TUMOR_DOIDS, true, "A semicolon-separated list of DOIDs representing the primary tumor of patient.")
            return options
        }

        private fun toStringSet(paramValue: String, separator: String): Set<String> {
            return if (paramValue.isNotEmpty()) paramValue.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toSet() else emptySet()
        }
    }
}