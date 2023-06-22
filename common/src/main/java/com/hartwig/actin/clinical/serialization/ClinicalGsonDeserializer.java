package com.hartwig.actin.clinical.serialization;

import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.string;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
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
import com.hartwig.actin.clinical.datamodel.treatment.Chemotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.CombinedTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass;
import com.hartwig.actin.clinical.datamodel.treatment.HormoneTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Immunotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableChemotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableCombinedTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableHormoneTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableImmunotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRecommendationCriteria;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableSurgicalTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableTargetedTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.OtherTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.RecommendationCriteria;
import com.hartwig.actin.clinical.datamodel.treatment.SurgicalTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.TargetedTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableSurgeryHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent;
import com.hartwig.actin.clinical.datamodel.treatment.history.SurgeryHistoryDetails;
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
                .registerTypeAdapter(Drug.class, new AbstractClassAdapter<Drug>(ImmutableDrug.class))
                .registerTypeAdapter(Chemotherapy.class, new AbstractClassAdapter<Chemotherapy>(ImmutableChemotherapy.class))
                .registerTypeAdapter(CombinedTherapy.class, new AbstractClassAdapter<CombinedTherapy>(ImmutableCombinedTherapy.class))
                .registerTypeAdapter(HormoneTherapy.class, new AbstractClassAdapter<HormoneTherapy>(ImmutableHormoneTherapy.class))
                .registerTypeAdapter(Immunotherapy.class, new AbstractClassAdapter<Immunotherapy>(ImmutableImmunotherapy.class))
                .registerTypeAdapter(OtherTherapy.class, new AbstractClassAdapter<OtherTherapy>(ImmutableOtherTherapy.class))
                .registerTypeAdapter(Radiotherapy.class, new AbstractClassAdapter<Radiotherapy>(ImmutableRadiotherapy.class))
                .registerTypeAdapter(SurgicalTreatment.class, new AbstractClassAdapter<SurgicalTreatment>(ImmutableSurgicalTreatment.class))
                .registerTypeAdapter(TargetedTherapy.class, new AbstractClassAdapter<TargetedTherapy>(ImmutableTargetedTherapy.class))
                .registerTypeAdapter(Treatment.class, new TreatmentAdapter())
                .registerTypeAdapter(Therapy.class, new TreatmentAdapter())
                .registerTypeAdapter(SurgeryHistoryDetails.class,
                        new AbstractClassAdapter<SurgeryHistoryDetails>(ImmutableSurgeryHistoryDetails.class))
                .registerTypeAdapter(TherapyHistoryDetails.class,
                        new AbstractClassAdapter<TherapyHistoryDetails>(ImmutableTherapyHistoryDetails.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<String>>() {
                }.getType(), new ImmutableListAdapter<String>(String.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<TreatmentHistoryEntry>>() {
                }.getType(), new ImmutableListAdapter<TreatmentHistoryEntry>(ImmutableTreatmentHistoryEntry.class))
                .registerTypeAdapter(new TypeToken<ImmutableList<PriorTumorTreatment>>() {
                }.getType(), new ImmutableListAdapter<PriorTumorTreatment>(ImmutablePriorTumorTreatment.class))
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
                .registerTypeAdapter(new TypeToken<ImmutableSet<Therapy>>() {
                }.getType(), new ImmutableSetAdapter<Therapy>(Therapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Treatment>>() {
                }.getType(), new ImmutableSetAdapter<Treatment>(Treatment.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<DrugClass>>() {
                }.getType(), new ImmutableSetAdapter<DrugClass>(DrugClass.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Intent>>() {
                }.getType(), new ImmutableSetAdapter<Intent>(Intent.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Chemotherapy>>() {
                }.getType(), new ImmutableSetAdapter<Chemotherapy>(ImmutableChemotherapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<CombinedTherapy>>() {
                }.getType(), new ImmutableSetAdapter<CombinedTherapy>(ImmutableCombinedTherapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Immunotherapy>>() {
                }.getType(), new ImmutableSetAdapter<Immunotherapy>(ImmutableImmunotherapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<OtherTherapy>>() {
                }.getType(), new ImmutableSetAdapter<OtherTherapy>(ImmutableOtherTherapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<Radiotherapy>>() {
                }.getType(), new ImmutableSetAdapter<Radiotherapy>(ImmutableRadiotherapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<SurgicalTreatment>>() {
                }.getType(), new ImmutableSetAdapter<SurgicalTreatment>(ImmutableSurgicalTreatment.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<TargetedTherapy>>() {
                }.getType(), new ImmutableSetAdapter<TargetedTherapy>(ImmutableTargetedTherapy.class))
                .registerTypeAdapter(new TypeToken<ImmutableSet<ObservedToxicity>>() {
                }.getType(), new ImmutableSetAdapter<ObservedToxicity>(ImmutableObservedToxicity.class))
                .registerTypeAdapter(new TypeToken<ImmutableMap<String, RecommendationCriteria>>() {
                }.getType(), new ImmutableMapAdapter<String, RecommendationCriteria>(ImmutableRecommendationCriteria.class));
    }

    private static class LocalDateAdapter implements JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
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
        public ImmutableList<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {

            return (ImmutableList<T>) ImmutableList.copyOf(deserializeJsonCollection(jsonElement,
                    context,
                    concreteType).collect(Collectors.toList()));
        }
    }

    private static class ImmutableSetAdapter<T> implements JsonDeserializer<ImmutableSet<T>> {

        private final Type concreteType;

        public ImmutableSetAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public ImmutableSet<T> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
                throws JsonParseException {

            return (ImmutableSet<T>) ImmutableSet.copyOf(deserializeJsonCollection(jsonElement,
                    context,
                    concreteType).collect(Collectors.toSet()));
        }
    }

    private static class ImmutableMapAdapter<K, V> implements JsonDeserializer<ImmutableMap<K, V>> {

        private final Type concreteType;

        public ImmutableMapAdapter(Type concreteType) {
            this.concreteType = concreteType;
        }

        @Override
        public ImmutableMap<K, V> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return (ImmutableMap<K, V>) ImmutableMap.copyOf(json.getAsJsonObject()
                    .asMap()
                    .entrySet()
                    .stream()
                    .map(entry -> Map.entry(entry.getKey(), context.deserialize(entry.getValue(), concreteType)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }

    private static class DrugNameSetAdapter implements JsonDeserializer<ImmutableSet<Drug>> {

        private final Map<String, Drug> drugsByName;

        private DrugNameSetAdapter(Map<String, Drug> drugsByName) {
            this.drugsByName = drugsByName;
        }

        @Override
        public ImmutableSet<Drug> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return json.isJsonNull()
                    ? null
                    : ImmutableSet.copyOf(json.getAsJsonArray()
                            .asList()
                            .stream()
                            .map(listElement -> drugsByName.get(listElement.getAsString().toLowerCase()))
                            .collect(Collectors.toSet()));
        }
    }

    private static class TreatmentAdapter implements JsonDeserializer<Treatment> {

        @Override
        public Treatment deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {

            return jsonElement.isJsonNull()
                    ? null
                    : (Treatment) context.deserialize(jsonElement,
                            TreatmentType.valueOf(string(jsonElement.getAsJsonObject(), "treatmentType")).treatmentClass());
        }
    }

    private static <T> Stream<T> deserializeJsonCollection(JsonElement jsonElement, JsonDeserializationContext context, Type type) {
        return jsonElement.isJsonNull() ? null : jsonElement.getAsJsonArray().asList().stream().map(listElement -> {
            T deserialized = context.deserialize(listElement, type);
            if (deserialized == null) {
                throw new RuntimeException("Unable to deserialize " + listElement);
            }
            return deserialized;
        });
    }
}
