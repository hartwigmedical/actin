package com.hartwig.actin.clinical.serialization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.jetbrains.annotations.NotNull;

public class TreatmentJson {
    @NotNull
    public static List<Treatment> read(@NotNull String file, Map<String, Drug> drugsByName) throws IOException {
        String contents = Files.readString(Path.of(file));
        return ClinicalGsonDeserializer.createWithDrugMap(drugsByName).fromJson(contents, new TypeToken<List<Treatment>>() {
        }.getType());
    }
}
