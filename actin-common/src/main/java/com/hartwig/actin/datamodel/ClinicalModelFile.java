package com.hartwig.actin.datamodel;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.jetbrains.annotations.NotNull;

public final class ClinicalModelFile {

    private ClinicalModelFile() {
    }

    public static void write(@NotNull ClinicalModel model, @NotNull String clinicalModelJson) throws IOException {
        String json = new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().create().toJson(model);
        BufferedWriter writer = new BufferedWriter(new FileWriter(clinicalModelJson));

        writer.write(json);
        writer.close();
    }

    @NotNull
    public static ClinicalModel read(@NotNull String clinicalModelJson) throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(clinicalModelJson));
        return new Gson().fromJson(reader, ClinicalModel.class);
    }
}
