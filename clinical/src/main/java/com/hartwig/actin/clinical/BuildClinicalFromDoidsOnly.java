package com.hartwig.actin.clinical;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class BuildClinicalFromDoidsOnly {

    private static final Logger LOGGER = LogManager.getLogger(BuildClinicalFromDoidsOnly.class);

    private static final String OUTPUT_DIRECTORY = "output_directory";
    private static final String SAMPLE = "sample";
    private static final String PRIMARY_TUMOR_DOIDS = "primary_tumor_doids";

    private static final String APPLICATION = "ACTIN Build Clinical From Doids Only";
    private static final String VERSION = BuildClinicalFromDoidsOnly.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException, ParseException {
        Options options = createOptions();

        new BuildClinicalFromDoidsOnly(new DefaultParser().parse(options, args)).run();
    }

    @NotNull
    private final CommandLine command;

    public BuildClinicalFromDoidsOnly(@NotNull final CommandLine command) {
        this.command = command;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        String sampleId = command.getOptionValue(SAMPLE);
        Set<String> doids = toStringSet(command.getOptionValue(PRIMARY_TUMOR_DOIDS), ";");

        LOGGER.info("Creating clinical record for {} with doids {}", sampleId, doids);
        ClinicalRecord record = createRecord(sampleId, doids);

        String outputDirectory = command.getOptionValue(OUTPUT_DIRECTORY);
        LOGGER.info("Writing clinical record for {} to {}", sampleId, outputDirectory);
        ClinicalRecordJson.write(Lists.newArrayList(record), outputDirectory);

        LOGGER.info("Done!");
    }

    @NotNull
    private static ClinicalRecord createRecord(@NotNull String sampleId, @NotNull Set<String> doids) {
        return ImmutableClinicalRecord.builder()
                .sampleId(sampleId)
                .patient(ImmutablePatientDetails.builder().gender(Gender.FEMALE).birthYear(2022).registrationDate(LocalDate.now()).build())
                .tumor(ImmutableTumorDetails.builder().doids(doids).build())
                .clinicalStatus(ImmutableClinicalStatus.builder().build())
                .build();
    }

    @NotNull
    private static Options createOptions() {
        Options options = new Options();

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where clinical data output will be written to");
        options.addOption(SAMPLE, true, "The sample for which clinical data is generated");
        options.addOption(PRIMARY_TUMOR_DOIDS, true, "A semicolon-separated list of DOIDs representing the primary tumor of patient.");

        return options;
    }

    @NotNull
    private static Set<String> toStringSet(@NotNull String paramValue, @NotNull String separator) {
        return !paramValue.isEmpty() ? Sets.newHashSet(paramValue.split(separator)) : Sets.newHashSet();
    }
}
