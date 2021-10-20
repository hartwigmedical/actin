package com.hartwig.actin.clinical.serialization;

import static com.hartwig.actin.util.Json.array;
import static com.hartwig.actin.util.Json.bool;
import static com.hartwig.actin.util.Json.date;
import static com.hartwig.actin.util.Json.integer;
import static com.hartwig.actin.util.Json.nullableBool;
import static com.hartwig.actin.util.Json.nullableDate;
import static com.hartwig.actin.util.Json.nullableInteger;
import static com.hartwig.actin.util.Json.nullableNumber;
import static com.hartwig.actin.util.Json.nullableString;
import static com.hartwig.actin.util.Json.nullableStringList;
import static com.hartwig.actin.util.Json.number;
import static com.hartwig.actin.util.Json.object;
import static com.hartwig.actin.util.Json.string;
import static com.hartwig.actin.util.Json.stringList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BloodPressure;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodPressure;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableCancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.util.Paths;

import org.jetbrains.annotations.NotNull;

public final class ClinicalRecordJson {

    private static final String CLINICAL_JSON_EXTENSION = ".clinical.json";

    private ClinicalRecordJson() {
    }

    public static void write(@NotNull List<ClinicalRecord> records, @NotNull String outputDirectory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(outputDirectory);
        for (ClinicalRecord record : records) {
            String jsonFile = path + record.sampleId() + CLINICAL_JSON_EXTENSION;

            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(toJson(record));
            writer.close();
        }
    }

    @NotNull
    public static List<ClinicalRecord> readFromDir(@NotNull String clinicalDirectory) throws IOException {
        List<ClinicalRecord> records = Lists.newArrayList();
        File[] files = new File(clinicalDirectory).listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Could not retrieve clinical json files from " + clinicalDirectory);
        }

        for (File file : files) {
            records.add(fromJson(Files.readString(file.toPath())));
        }

        return records;
    }

    @NotNull
    public static ClinicalRecord read(@NotNull String clinicalJson) throws IOException {
        return fromJson(Files.readString(new File(clinicalJson).toPath()));
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull ClinicalRecord record) {
        return new GsonBuilder().serializeNulls().create().toJson(record);
    }

    @VisibleForTesting
    @NotNull
    static ClinicalRecord fromJson(@NotNull String json) {
        Gson gson = new GsonBuilder().registerTypeAdapter(ClinicalRecord.class, new ClinicalRecordCreator()).create();
        return gson.fromJson(json, ClinicalRecord.class);
    }

    private static class ClinicalRecordCreator implements JsonDeserializer<ClinicalRecord> {

        @Override
        public ClinicalRecord deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject record = jsonElement.getAsJsonObject();

            return ImmutableClinicalRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .patient(toPatientDetails(object(record, "patient")))
                    .tumor(toTumorDetails(object(record, "tumor")))
                    .clinicalStatus(toClinicalStatus(object(record, "clinicalStatus")))
                    .priorTumorTreatments(toPriorTumorTreatments(array(record, "priorTumorTreatments")))
                    .priorSecondPrimaries(toPriorSecondPrimaries(array(record, "priorSecondPrimaries")))
                    .priorOtherConditions(toPriorOtherConditions(array(record, "priorOtherConditions")))
                    .cancerRelatedComplications(toCancerRelatedComplications(array(record, "cancerRelatedComplications")))
                    .otherComplications(toOtherComplications(array(record, "otherComplications")))
                    .labValues(toLabValues(array(record, "labValues")))
                    .toxicities(toToxicities(array(record, "toxicities")))
                    .allergies(toAllergies(array(record, "allergies")))
                    .surgeries(toSurgeries(array(record, "surgeries")))
                    .bloodPressures(toBloodPressures(array(record, "bloodPressures")))
                    .bloodTransfusions(toBloodTransfusions(array(record, "bloodTransfusions")))
                    .medications(toMedications(array(record, "medications")))
                    .build();
        }

        @NotNull
        private static PatientDetails toPatientDetails(@NotNull JsonObject patient) {
            return ImmutablePatientDetails.builder()
                    .gender(Gender.valueOf(string(patient, "gender")))
                    .birthYear(integer(patient, "birthYear"))
                    .registrationDate(date(patient, "registrationDate"))
                    .questionnaireDate(nullableDate(patient, "questionnaireDate"))
                    .build();
        }

        @NotNull
        private static TumorDetails toTumorDetails(@NotNull JsonObject tumor) {
            String stageString = nullableString(tumor, "stage");

            return ImmutableTumorDetails.builder()
                    .primaryTumorLocation(nullableString(tumor, "primaryTumorLocation"))
                    .primaryTumorSubLocation(nullableString(tumor, "primaryTumorSubLocation"))
                    .primaryTumorType(nullableString(tumor, "primaryTumorType"))
                    .primaryTumorSubType(nullableString(tumor, "primaryTumorSubType"))
                    .primaryTumorExtraDetails(nullableString(tumor, "primaryTumorExtraDetails"))
                    .doids(nullableStringList(tumor, "doids"))
                    .stage(stageString != null ? TumorStage.valueOf(stageString) : null)
                    .hasMeasurableLesionRecist(nullableBool(tumor, "hasMeasurableLesionRecist"))
                    .hasBrainLesions(nullableBool(tumor, "hasBrainLesions"))
                    .hasActiveBrainLesions(nullableBool(tumor, "hasActiveBrainLesions"))
                    .hasSymptomaticBrainLesions(nullableBool(tumor, "hasSymptomaticBrainLesions"))
                    .hasCnsLesions(nullableBool(tumor, "hasCnsLesions"))
                    .hasActiveCnsLesions(nullableBool(tumor, "hasActiveCnsLesions"))
                    .hasSymptomaticCnsLesions(nullableBool(tumor, "hasSymptomaticCnsLesions"))
                    .hasBoneLesions(nullableBool(tumor, "hasBoneLesions"))
                    .hasLiverLesions(nullableBool(tumor, "hasLiverLesions"))
                    .hasOtherLesions(nullableBool(tumor, "hasOtherLesions"))
                    .otherLesions(nullableStringList(tumor, "otherLesions"))
                    .biopsyLocation(nullableString(tumor, "biopsyLocation"))
                    .build();
        }

        @NotNull
        private static ClinicalStatus toClinicalStatus(@NotNull JsonObject clinicalStatus) {
            return ImmutableClinicalStatus.builder()
                    .who(nullableInteger(clinicalStatus, "who"))
                    .hasActiveInfection(nullableBool(clinicalStatus, "hasActiveInfection"))
                    .hasSigAberrationLatestEcg(nullableBool(clinicalStatus, "hasSigAberrationLatestEcg"))
                    .ecgAberrationDescription(nullableString(clinicalStatus, "ecgAberrationDescription"))
                    .build();
        }

        @NotNull
        private static List<PriorTumorTreatment> toPriorTumorTreatments(@NotNull JsonArray priorTumorTreatments) {
            List<PriorTumorTreatment> priorTumorTreatmentList = Lists.newArrayList();
            for (JsonElement element : priorTumorTreatments) {
                JsonObject object = element.getAsJsonObject();
                priorTumorTreatmentList.add(ImmutablePriorTumorTreatment.builder()
                        .name(string(object, "name"))
                        .year(nullableInteger(object, "year"))
                        .category(string(object, "category"))
                        .isSystemic(bool(object, "isSystemic"))
                        .chemoType(nullableString(object, "chemoType"))
                        .immunoType(nullableString(object, "immunoType"))
                        .targetedType(nullableString(object, "targetedType"))
                        .hormoneType(nullableString(object, "hormoneType"))
                        .stemCellTransType(nullableString(object, "stemCellTransType"))
                        .build());
            }
            return priorTumorTreatmentList;
        }

        @NotNull
        private static List<PriorSecondPrimary> toPriorSecondPrimaries(@NotNull JsonArray priorSecondPrimaries) {
            List<PriorSecondPrimary> priorSecondPrimaryList = Lists.newArrayList();
            for (JsonElement element : priorSecondPrimaries) {
                JsonObject object = element.getAsJsonObject();
                priorSecondPrimaryList.add(ImmutablePriorSecondPrimary.builder()
                        .tumorLocation(string(object, "tumorLocation"))
                        .tumorSubLocation(string(object, "tumorSubLocation"))
                        .tumorType(string(object, "tumorType"))
                        .tumorSubType(string(object, "tumorSubType"))
                        .doids(stringList(object, "doids"))
                        .diagnosedYear(nullableInteger(object, "diagnosedYear"))
                        .isSecondPrimaryActive(bool(object, "isSecondPrimaryActive"))
                        .build());
            }
            return priorSecondPrimaryList;
        }

        @NotNull
        private static List<PriorOtherCondition> toPriorOtherConditions(@NotNull JsonArray priorOtherConditions) {
            List<PriorOtherCondition> priorOtherConditionList = Lists.newArrayList();
            for (JsonElement element : priorOtherConditions) {
                JsonObject object = element.getAsJsonObject();
                priorOtherConditionList.add(ImmutablePriorOtherCondition.builder()
                        .name(string(object, "name"))
                        .doids(stringList(object, "doids"))
                        .category(string(object, "category"))
                        .build());
            }
            return priorOtherConditionList;
        }

        @NotNull
        private static List<CancerRelatedComplication> toCancerRelatedComplications(@NotNull JsonArray cancerRelatedComplications) {
            List<CancerRelatedComplication> cancerRelatedComplicationList = Lists.newArrayList();
            for (JsonElement element : cancerRelatedComplications) {
                JsonObject object = element.getAsJsonObject();
                cancerRelatedComplicationList.add(ImmutableCancerRelatedComplication.builder().name(string(object, "name")).build());
            }
            return cancerRelatedComplicationList;
        }

        @NotNull
        private static List<Complication> toOtherComplications(@NotNull JsonArray otherComplications) {
            List<Complication> otherComplicationList = Lists.newArrayList();
            for (JsonElement element : otherComplications) {
                JsonObject object = element.getAsJsonObject();
                otherComplicationList.add(ImmutableComplication.builder()
                        .name(string(object, "name"))
                        .doids(stringList(object, "doids"))
                        .specialty(string(object, "specialty"))
                        .onsetDate(date(object, "onsetDate"))
                        .category(string(object, "category"))
                        .status(string(object, "status"))
                        .build());
            }
            return otherComplicationList;
        }

        @NotNull
        private static List<LabValue> toLabValues(@NotNull JsonArray labValues) {
            List<LabValue> labValueList = Lists.newArrayList();
            for (JsonElement element : labValues) {
                JsonObject object = element.getAsJsonObject();
                labValueList.add(ImmutableLabValue.builder()
                        .date(date(object, "date"))
                        .code(string(object, "code"))
                        .name(string(object, "name"))
                        .comparator(string(object, "comparator"))
                        .value(number(object, "value"))
                        .unit(string(object, "unit"))
                        .refLimitLow(nullableNumber(object, "refLimitLow"))
                        .refLimitUp(nullableNumber(object, "refLimitUp"))
                        .isOutsideRef(nullableBool(object, "isOutsideRef"))
                        .build());
            }
            return labValueList;
        }

        @NotNull
        private static List<Toxicity> toToxicities(@NotNull JsonArray toxicities) {
            List<Toxicity> toxicityList = Lists.newArrayList();
            for (JsonElement element : toxicities) {
                JsonObject object = element.getAsJsonObject();
                toxicityList.add(ImmutableToxicity.builder()
                        .name(string(object, "name"))
                        .evaluatedDate(date(object, "evaluatedDate"))
                        .source(ToxicitySource.valueOf(string(object, "source")))
                        .grade(nullableInteger(object, "grade"))
                        .build());
            }
            return toxicityList;
        }

        @NotNull
        private static List<Allergy> toAllergies(@NotNull JsonArray allergies) {
            List<Allergy> allergyList = Lists.newArrayList();
            for (JsonElement element : allergies) {
                JsonObject object = element.getAsJsonObject();
                allergyList.add(ImmutableAllergy.builder()
                        .name(string(object, "name"))
                        .category(string(object, "category"))
                        .criticality(string(object, "criticality"))
                        .build());
            }
            return allergyList;
        }

        @NotNull
        private static List<Surgery> toSurgeries(@NotNull JsonArray surgeries) {
            List<Surgery> surgeryList = Lists.newArrayList();
            for (JsonElement element : surgeries) {
                JsonObject object = element.getAsJsonObject();
                surgeryList.add(ImmutableSurgery.builder().endDate(date(object, "endDate")).build());
            }
            return surgeryList;
        }

        @NotNull
        private static List<BloodPressure> toBloodPressures(@NotNull JsonArray bloodPressures) {
            List<BloodPressure> bloodPressureList = Lists.newArrayList();
            for (JsonElement element : bloodPressures) {
                JsonObject object = element.getAsJsonObject();
                bloodPressureList.add(ImmutableBloodPressure.builder()
                        .date(date(object, "date"))
                        .category(string(object, "category"))
                        .value(number(object, "value"))
                        .unit(string(object, "unit"))
                        .build());
            }
            return bloodPressureList;
        }

        @NotNull
        private static List<BloodTransfusion> toBloodTransfusions(@NotNull JsonArray bloodTransfusions) {
            List<BloodTransfusion> bloodTransfusionList = Lists.newArrayList();
            for (JsonElement element : bloodTransfusions) {
                JsonObject object = element.getAsJsonObject();
                bloodTransfusionList.add(ImmutableBloodTransfusion.builder()
                        .date(date(object, "date"))
                        .product(string(object, "product"))
                        .build());
            }
            return bloodTransfusionList;
        }

        @NotNull
        private static List<Medication> toMedications(@NotNull JsonArray medications) {
            List<Medication> medicationList = Lists.newArrayList();
            for (JsonElement element : medications) {
                JsonObject object = element.getAsJsonObject();
                medicationList.add(ImmutableMedication.builder()
                        .name(string(object, "name"))
                        .type(string(object, "type"))
                        .dosageMin(nullableNumber(object, "dosageMin"))
                        .dosageMax(nullableNumber(object, "dosageMax"))
                        .dosageUnit(nullableString(object, "dosageUnit"))
                        .frequency(nullableNumber(object, "frequency"))
                        .frequencyUnit(nullableString(object, "frequencyUnit"))
                        .ifNeeded(nullableBool(object, "ifNeeded"))
                        .startDate(nullableDate(object, "startDate"))
                        .stopDate(nullableDate(object, "stopDate"))
                        .active(nullableBool(object, "active"))
                        .build());
            }
            return medicationList;
        }
    }
}
