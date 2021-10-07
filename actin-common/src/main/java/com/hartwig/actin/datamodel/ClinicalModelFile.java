package com.hartwig.actin.datamodel;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;

import org.jetbrains.annotations.NotNull;

public final class ClinicalModelFile {

    private ClinicalModelFile() {
    }

    public static void write(@NotNull ClinicalModel model, @NotNull String clinicalModelJson) throws IOException {
        String json = new GsonBuilder().serializeNulls().create().toJson(model);
        BufferedWriter writer = new BufferedWriter(new FileWriter(clinicalModelJson));

        writer.write(json);
        writer.close();
    }

    @NotNull
    public static ClinicalModel read(@NotNull String clinicalModelJson) throws IOException {
        Gson gson = new GsonBuilder().registerTypeAdapter(ClinicalRecord.class, new ClinicalModelCreator()).create();

        return gson.fromJson(new JsonReader(new FileReader(clinicalModelJson)), ClinicalModel.class);
    }

    private static class ClinicalModelCreator implements JsonDeserializer<ClinicalModel> {

        @Override
        public ClinicalModel deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            List<ClinicalRecord> records = Lists.newArrayList();

            return new ClinicalModel(records);
        }
    }
}
