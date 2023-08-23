package com.hartwig.actin.clinical.serialization;

import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.string;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.hartwig.actin.clinical.datamodel.AtcClassification;
import com.hartwig.actin.clinical.datamodel.AtcLevel;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyLocationCategory;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.CypInteraction;
import com.hartwig.actin.clinical.datamodel.Dosage;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification;
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction;
import com.hartwig.actin.clinical.datamodel.ImmutableDosage;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicityEvaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.ObservedToxicity;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicityEvaluation;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType;
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentClass;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent;
import com.hartwig.actin.clinical.datamodel.treatment.history.TherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;

import org.jetbrains.annotations.NotNull;

public class ClinicalGsonDeserializer {
    @NotNull
    public static Gson create() {
        return gsonBuilder().registerTypeAdapter(new TypeToken<ImmutableSet<Drug>>() {
        }.getType(), new ImmutableSetAdapter<Drug>(ImmutableDrug.class)).create();
    }

    @NotNull
    public static Gson createWithDrugMap(@NotNull Map<String, Drug> drugsByName) {
        return gsonBuilder().registerTypeAdapter(new TypeToken<ImmutableSet<Drug>>() {
        }.getType(), new DrugNameSetAdapter(drugsByName)).create();
    }

    @NotNull
    private static GsonBuilder gsonBuilder() {
        return new GsonBuilder().serializeNulls()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(PatientDetails.class, new AbstractClassAdapter<PatientDetails>(ImmutablePatientDetails.class))
                .registerTypeAdapter(TumorDetails.class, new AbstractClassAdapter<TumorDetails>(ImmutableTumorDetails.class))
                .registerTypeAdapter(ClinicalStatus.class, new AbstractClassAdapter<ClinicalStatus>(ImmutableClinicalStatus.class))
                .registerTypeAdapter(InfectionStatus.class, new AbstractClassAdapter<InfectionStatus>(ImmutableInfectionStatus.class))
                .registerTypeAdapter(ECG.class, new AbstractClassAdapter<ECG>(ImmutableECG.class))
                .registerTypeAdapter(AtcClassification.class, new AbstractClassAdapter<AtcClassification>(ImmutableAtcClassification.class))
                .registerTypeAdapter(AtcLevel.class, new AbstractClassAdapter<AtcLevel>(ImmutableAtcLevel.class))
                .registerTypeAdapter(Dosage.class, new AbstractClassAdapter<Dosage>(ImmutableDosage.class))
                .registerTypeAdapter(Drug.class, new AbstractClassAdapter<Drug>(ImmutableDrug.class))
                .registerTypeAdapter(DrugTherapy.class, new AbstractClassAdapter<DrugTherapy>(ImmutableDrugTherapy.class))
                .registerTypeAdapter(OtherTreatment.class, new AbstractClassAdapter<OtherTreatment>(ImmutableOtherTreatment.class))
                .registerTypeAdapter(Radiotherapy.class, new AbstractClassAdapter<Radiotherapy>(ImmutableRadiotherapy.class))
                .registerTypeAdapter(Treatment.class, new TreatmentAdapter())
                .registerTypeAdapter(Therapy.class, new TreatmentAdapter())
                .registerTypeAdapter(TherapyHistoryDetails.class,
                        new AbstractClassAdapter<TherapyHistoryDetails>(ImmutableTherapyHistoryDetails.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<String>>() {
                }.getType(), new ImmutableListAdapter<String>(String.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<TreatmentHistoryEntry>>() {
                }.getType(), new ImmutableListAdapter<TreatmentHistoryEntry>(ImmutableTreatmentHistoryEntry.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorSecondPrimary>>() {
                }.getType(), new ImmutableListAdapter<PriorSecondPrimary>(ImmutablePriorSecondPrimary.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorOtherCondition>>() {
                }.getType(), new ImmutableListAdapter<PriorOtherCondition>(ImmutablePriorOtherCondition.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorMolecularTest>>() {
                }.getType(), new ImmutableListAdapter<PriorMolecularTest>(ImmutablePriorMolecularTest.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Complication>>() {
                }.getType(), new ImmutableListAdapter<Complication>(ImmutableComplication.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<LabValue>>() {
                }.getType(), new ImmutableListAdapter<LabValue>(ImmutableLabValue.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Toxicity>>() {
                }.getType(), new ImmutableListAdapter<Toxicity>(ImmutableToxicity.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<ToxicityEvaluation>>() {
                }.getType(), new ImmutableListAdapter<ToxicityEvaluation>(ImmutableToxicityEvaluation.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Intolerance>>() {
                }.getType(), new ImmutableListAdapter<Intolerance>(ImmutableIntolerance.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Surgery>>() {
                }.getType(), new ImmutableListAdapter<Surgery>(ImmutableSurgery.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<BodyWeight>>() {
                }.getType(), new ImmutableListAdapter<BodyWeight>(ImmutableBodyWeight.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<VitalFunction>>() {
                }.getType(), new ImmutableListAdapter<VitalFunction>(ImmutableVitalFunction.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<BloodTransfusion>>() {
                }.getType(), new ImmutableListAdapter<BloodTransfusion>(ImmutableBloodTransfusion.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<Medication>>() {
                }.getType(), new ImmutableListAdapter<Medication>(ImmutableMedication.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<String>>() {
                }.getType(), new ImmutableSetAdapter<String>(String.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<TreatmentCategory>>() {
                }.getType(), new ImmutableSetAdapter<TreatmentCategory>(TreatmentCategory.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Treatment>>() {
                }.getType(), new ImmutableSetAdapter<Treatment>(Treatment.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<TreatmentType>>() {
                }.getType(), new ImmutableSetAdapter<TreatmentType>(OtherTreatmentType.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<BodyLocationCategory>>() {
                }.getType(), new ImmutableSetAdapter<BodyLocationCategory>(BodyLocationCategory.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<DrugType>>() {
                }.getType(), new ImmutableSetAdapter<DrugType>(DrugType.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Intent>>() {
                }.getType(), new ImmutableSetAdapter<Intent>(Intent.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<ObservedToxicity>>() {
                }.getType(), new ImmutableSetAdapter<ObservedToxicity>(ImmutableObservedToxicity.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<CypInteraction>>() {
                }.getType(), new ImmutableListAdapter<CypInteraction>(ImmutableCypInteraction.class));
    }

    private static class LocalDateAdapter implements JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
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
        public T deserialize(@NotNull JsonElement jsonElement, @NotNull Type type, @NotNull JsonDeserializationContext context)
                throws JsonParseException {
            return context.deserialize(jsonElement, concreteType);
        }
    }

    private static class ImmutableListAdapter<T> implements JsonDeserializer<ImmutableList<T>> {

        private final Type concreteType;

        public ImmutableListAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public ImmutableList<T> deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext context) throws JsonParseException {

            return jsonElement.isJsonNull()
                    ? null
                    : (ImmutableList<T>) ImmutableList.copyOf(deserializeJsonCollection(jsonElement, context, concreteType).collect(
                            Collectors.toList()));
        }
    }

    private static class ImmutableSetAdapter<T> implements JsonDeserializer<ImmutableSet<T>> {

        private final Type concreteType;

        public ImmutableSetAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public ImmutableSet<T> deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext context) throws JsonParseException {

            return jsonElement.isJsonNull()
                    ? null
                    : (ImmutableSet<T>) ImmutableSet.copyOf(deserializeJsonCollection(jsonElement,
                            context,
                            concreteType).collect(Collectors.toSet()));
        }
    }

    private static class DrugNameSetAdapter implements JsonDeserializer<ImmutableSet<Drug>> {

        private final Map<String, Drug> drugsByName;

        private DrugNameSetAdapter(Map<String, Drug> drugsByName) {
            this.drugsByName = drugsByName;
        }

        @Override
        public ImmutableSet<Drug> deserialize(@NotNull JsonElement json, @NotNull Type type, @NotNull JsonDeserializationContext context) {
            return json.isJsonNull()
                    ? null
                    : ImmutableSet.copyOf(json.getAsJsonArray().asList().stream().map(this::getDrug).collect(Collectors.toSet()));
        }

        @NotNull
        private Drug getDrug(@NotNull JsonElement listElement) {
            Drug drug = drugsByName.get(listElement.getAsString().toLowerCase());
            if (drug == null) {
                throw new JsonParseException("Failed to resolve: " + listElement);
            }
            return drug;
        }
    }

    private static class TreatmentAdapter implements JsonDeserializer<Treatment> {

        @Override
        public Treatment deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                return jsonElement.isJsonNull()
                        ? null
                        : (Treatment) context.deserialize(jsonElement,
                                TreatmentClass.valueOf(string(jsonElement.getAsJsonObject(), "treatmentClass")).treatmentClass());
            } catch (Exception e) {
                throw new JsonParseException("Failed to deserialize: " + jsonElement, e);
            }
        }
    }

    @NotNull
    private static <T> Stream<T> deserializeJsonCollection(@NotNull JsonElement jsonElement, @NotNull JsonDeserializationContext context,
            @NotNull Type type) {
        return jsonElement.getAsJsonArray().asList().stream().map(listElement -> {
            T deserialized = context.deserialize(listElement, type);
            if (deserialized == null) {
                throw new RuntimeException("Unable to deserialize " + listElement);
            }
            return deserialized;
        });
    }
}
