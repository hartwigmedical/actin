package com.hartwig.actin.clinical;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

public final class ClinicalModelFile {

    private ClinicalModelFile() {
    }

    public static void write(@NotNull ClinicalModel model, @NotNull String clinicalJsonFile) throws IOException {
        String json = new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create().toJson(model);
        BufferedWriter writer = new BufferedWriter(new FileWriter(clinicalJsonFile));

        writer.write(json);
        writer.close();
    }

    @NotNull
    public static ClinicalModel read(@NotNull String clinicalJsonFile) {
        return new Gson().fromJson(clinicalJsonFile, ClinicalModel.class);
    }
}
