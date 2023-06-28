package com.hartwig.actin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.reflect.TypeToken;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.serialization.ClinicalGsonDeserializer;

import org.jetbrains.annotations.NotNull;

public class TreatmentDatabaseFactory {

    private static final String DRUG_JSON = "drug.json";
    private static final String TREATMENT_JSON = "treatment.json";

    @NotNull
    public static TreatmentDatabase createFromPath(@NotNull String treatmentDbPath) throws IOException {
        Map<String, Drug> drugsByName = drugJsonToMapByName(readFile(treatmentDbPath, DRUG_JSON));
        Map<String, Treatment> treatmentsByName = treatmentJsonToMapByName(readFile(treatmentDbPath, TREATMENT_JSON), drugsByName);

        return new TreatmentDatabase(drugsByName, treatmentsByName);
    }

    @NotNull
    private static Map<String, Drug> drugJsonToMapByName(@NotNull String drugJson) {
        List<Drug> drugs = ClinicalGsonDeserializer.create().fromJson(drugJson, new TypeToken<List<Drug>>() {
        }.getType());
        return drugs.stream().collect(Collectors.toMap(drug -> drug.name().toLowerCase(), Function.identity()));
    }

    @NotNull
    private static Map<String, Treatment> treatmentJsonToMapByName(@NotNull String treatmentJson, @NotNull Map<String, Drug> drugsByName) {
        List<Treatment> treatments =
                ClinicalGsonDeserializer.createWithDrugMap(drugsByName).fromJson(treatmentJson, new TypeToken<List<Treatment>>() {
                }.getType());

        return treatments.stream()
                .flatMap(treatment -> Stream.concat(treatment.synonyms().stream(), Stream.of(treatment.name()))
                        .map(name -> Map.entry(name.toLowerCase(), treatment)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String readFile(final @NotNull String basePath, final @NotNull String filename) throws IOException {
        return Files.readString(Path.of(basePath, filename));
    }
}
