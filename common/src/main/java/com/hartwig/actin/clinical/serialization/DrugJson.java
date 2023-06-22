package com.hartwig.actin.clinical.serialization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;

import org.jetbrains.annotations.NotNull;

public class DrugJson {
    @NotNull
    public static List<Drug> read(@NotNull String file) throws IOException {
        String contents = Files.readString(Path.of(file));
        return ClinicalGsonDeserializer.create().fromJson(contents, new TypeToken<List<Drug>>() {
        }.getType());
    }
}
