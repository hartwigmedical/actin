package com.hartwig.actin.clinical.serialization;

import static com.hartwig.actin.util.json.Json.integer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator;
import com.hartwig.actin.util.Paths;
import com.hartwig.actin.util.json.GsonSerializer;
import org.jetbrains.annotations.NotNull;

public final class ClinicalRecordJson {

    private static final String CLINICAL_JSON_EXTENSION = ".clinical.json";

    private ClinicalRecordJson() {
    }

    public static void write(@NotNull List<ClinicalRecord> records, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        for (ClinicalRecord record : records) {
            String jsonFile = path + record.patientId() + CLINICAL_JSON_EXTENSION;

            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(toJson(record));
            writer.close();
        }
    }

    @NotNull
    public static List<ClinicalRecord> readFromDir(@NotNull String directory) throws IOException {
        List<ClinicalRecord> records = Lists.newArrayList();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Could not retrieve clinical json files from " + directory);
        }

        for (File file : files) {
            records.add(fromJson(Files.readString(file.toPath())));
        }

        records.sort(new ClinicalRecordComparator());

        return records;
    }

    @NotNull
    public static ClinicalRecord read(@NotNull String clinicalJson) throws IOException {
        return fromJson(Files.readString(new File(clinicalJson).toPath()));
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull ClinicalRecord record) {
        return GsonSerializer.create().toJson(record);
    }

    @VisibleForTesting
    @NotNull
    static ClinicalRecord fromJson(@NotNull String json) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(PatientDetails.class, new AbstractClassAdapter<PatientDetails>(ImmutablePatientDetails.class))
                .registerTypeAdapter(TumorDetails.class, new AbstractClassAdapter<TumorDetails>(ImmutableTumorDetails.class))
                .registerTypeAdapter(ClinicalStatus.class, new AbstractClassAdapter<ClinicalStatus>(ImmutableClinicalStatus.class))
                .registerTypeAdapter(InfectionStatus.class, new AbstractClassAdapter<InfectionStatus>(ImmutableInfectionStatus.class))
                .registerTypeAdapter(ECG.class, new AbstractClassAdapter<ECG>(ImmutableECG.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<String>>() {}.getType(),
                        new ImmutableListAdapter<String>(String.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorTumorTreatment>>() {}.getType(),
                        new ImmutableListAdapter<PriorTumorTreatment>(ImmutablePriorTumorTreatment.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorSecondPrimary>>() {}.getType(),
                        new ImmutableListAdapter<PriorSecondPrimary>(ImmutablePriorSecondPrimary.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorOtherCondition>>() {}.getType(),
                        new ImmutableListAdapter<PriorOtherCondition>(ImmutablePriorOtherCondition.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorMolecularTest>>() {}.getType(),
                        new ImmutableListAdapter<PriorMolecularTest>(ImmutablePriorMolecularTest.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Complication>>() {}.getType(),
                        new ImmutableListAdapter<Complication>(ImmutableComplication.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<LabValue>>() {}.getType(),
                        new ImmutableListAdapter<LabValue>(ImmutableLabValue.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Toxicity>>() {}.getType(),
                        new ImmutableListAdapter<Toxicity>(ImmutableToxicity.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Intolerance>>() {}.getType(),
                        new ImmutableListAdapter<Intolerance>(ImmutableIntolerance.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Surgery>>() {}.getType(),
                        new ImmutableListAdapter<Surgery>(ImmutableSurgery.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<BodyWeight>>() {}.getType(),
                        new ImmutableListAdapter<BodyWeight>(ImmutableBodyWeight.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<VitalFunction>>() {}.getType(),
                        new ImmutableListAdapter<VitalFunction>(ImmutableVitalFunction.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<BloodTransfusion>>() {}.getType(),
                        new ImmutableListAdapter<BloodTransfusion>(ImmutableBloodTransfusion.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Medication>>() {}.getType(),
                        new ImmutableListAdapter<Medication>(ImmutableMedication.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<String>>() {}.getType(),
                        new ImmutableSetAdapter<String>(String.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<TreatmentCategory>>() {}.getType(),
                        new ImmutableSetAdapter<TreatmentCategory>(TreatmentCategory.class))
                .create();
        return gson.fromJson(json, ImmutableClinicalRecord.class);
    }

    private static class LocalDateAdapter implements JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonNull()) {
                return null;
            } else {
                JsonObject dateObject = jsonElement.getAsJsonObject();
                return LocalDate.of(integer(dateObject, "year"), integer(dateObject, "month"), integer(dateObject, "day"));
            }
        }
    }

    private static class AbstractClassAdapter<T> implements JsonDeserializer<T> {

        private final Type concreteType;

        public AbstractClassAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public T deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                             @NotNull JsonDeserializationContext context) throws JsonParseException {
            return context.deserialize(jsonElement, concreteType);
        }
    }

    private static class ImmutableListAdapter<T> implements JsonDeserializer<ImmutableList<T>> {

        private final Type concreteType;

        public ImmutableListAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public ImmutableList<T> deserialize(JsonElement jsonElement, Type type,
                                            JsonDeserializationContext context) throws JsonParseException {

            return jsonElement.isJsonNull() ? null : ImmutableList.copyOf(jsonElement.getAsJsonArray().asList().stream()
                    .map(listElement -> (T) context.deserialize(listElement, concreteType))
                    .collect(Collectors.toList())
            );
        }
    }

    private static class ImmutableSetAdapter<T> implements JsonDeserializer<ImmutableSet<T>> {

        private final Type concreteType;

        public ImmutableSetAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public ImmutableSet<T> deserialize(JsonElement jsonElement, Type type,
                                            JsonDeserializationContext context) throws JsonParseException {

            return jsonElement.isJsonNull() ? null : ImmutableSet.copyOf(jsonElement.getAsJsonArray().asList().stream()
                    .map(listElement -> (T) context.deserialize(listElement, concreteType))
                    .collect(Collectors.toSet())
            );
        }
    }
}
