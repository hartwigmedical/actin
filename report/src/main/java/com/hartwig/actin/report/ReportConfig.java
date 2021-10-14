package com.hartwig.actin.report;

import com.hartwig.actin.util.Config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface ReportConfig {

    String SAMPLE = "sample";

    String CLINICAL_JSON = "clinical_json";

    String OUTPUT_DIRECTORY = "output_directory";

    @NotNull
    static Options createOptions() {
        Options options = new Options();

        options.addOption(SAMPLE, true, "Sample for which an ACTIN report will be generated");

        options.addOption(CLINICAL_JSON, true, "File containing the clinical record of the sample");

        options.addOption(OUTPUT_DIRECTORY, true, "Directory where the report will be written to");

        return options;
    }

    @NotNull
    String sample();

    @NotNull
    String clinicalJson();

    @NotNull
    String outputDirectory();

    @NotNull
    static ReportConfig createConfig(@NotNull CommandLine cmd) throws ParseException {
        return ImmutableReportConfig.builder()
                .sample(Config.nonOptionalValue(cmd, SAMPLE))
                .clinicalJson(Config.nonOptionalFile(cmd, CLINICAL_JSON))
                .outputDirectory(Config.nonOptionalDir(cmd, OUTPUT_DIRECTORY))
                .build();
    }
}
