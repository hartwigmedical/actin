package com.hartwig.actin.algo.serialization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.GsonBuilder;
import com.hartwig.actin.algo.datamodel.SampleTreatmentMatch;
import com.hartwig.actin.util.Paths;

import org.jetbrains.annotations.NotNull;

public final class SampleTreatmentMatchJson {

    private static final String TREATMENT_MATCH_EXTENSION = ".treatment_match.json";

    private SampleTreatmentMatchJson() {
    }

    public static void write(@NotNull SampleTreatmentMatch match, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        String jsonFile = path + match.sampleId() + TREATMENT_MATCH_EXTENSION;

        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
        writer.write(toJson(match));
        writer.close();
    }

    @NotNull
    private static String toJson(@NotNull SampleTreatmentMatch match) {
        return new GsonBuilder().serializeNulls().create().toJson(match);
    }
}
