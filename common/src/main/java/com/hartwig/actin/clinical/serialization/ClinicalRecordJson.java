package com.hartwig.actin.clinical.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.date;
import static com.hartwig.actin.util.json.Json.integer;
import static com.hartwig.actin.util.json.Json.nullableBool;
import static com.hartwig.actin.util.json.Json.nullableDate;
import static com.hartwig.actin.util.json.Json.nullableInteger;
import static com.hartwig.actin.util.json.Json.nullableNumber;
import static com.hartwig.actin.util.json.Json.nullableObject;
import static com.hartwig.actin.util.json.Json.nullableString;
import static com.hartwig.actin.util.json.Json.nullableStringList;
import static com.hartwig.actin.util.json.Json.number;
import static com.hartwig.actin.util.json.Json.object;
import static com.hartwig.actin.util.json.Json.string;
import static com.hartwig.actin.util.json.Json.stringList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.Gender;
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
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;
import com.hartwig.actin.clinical.sort.ClinicalRecordComparator;
import com.hartwig.actin.util.Paths;
import com.hartwig.actin.util.json.GsonSerializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ClinicalRecordJson {

    private static final String CLINICAL_JSON_EXTENSION = ".clinical.json";

    private ClinicalRecordJson() {
    }

    public static void write(@NotNull List<ClinicalRecord> records, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        for (ClinicalRecord record : records) {
            String jsonFile = path + record.sampleId() + CLINICAL_JSON_EXTENSION;

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
                    .priorMolecularTests(toPriorMolecularTests(array(record, "priorMolecularTests")))
                    .complications(toComplications(array(record, "complications")))
                    .labValues(toLabValues(array(record, "labValues")))
                    .toxicities(toToxicities(array(record, "toxicities")))
                    .intolerances(toIntolerances(array(record, "intolerances")))
                    .surgeries(toSurgeries(array(record, "surgeries")))
                    .bodyWeights(toBodyWeights(array(record, "bodyWeights")))
                    .vitalFunctions(toVitalFunctions(array(record, "vitalFunctions")))
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
                    .hasMeasurableDisease(nullableBool(tumor, "hasMeasurableDisease"))
                    .hasBrainLesions(nullableBool(tumor, "hasBrainLesions"))
                    .hasActiveBrainLesions(nullableBool(tumor, "hasActiveBrainLesions"))
                    .hasCnsLesions(nullableBool(tumor, "hasCnsLesions"))
                    .hasActiveCnsLesions(nullableBool(tumor, "hasActiveCnsLesions"))
                    .hasBoneLesions(nullableBool(tumor, "hasBoneLesions"))
                    .hasLiverLesions(nullableBool(tumor, "hasLiverLesions"))
                    .hasLungLesions(nullableBool(tumor, "hasLungLesions"))
                    .otherLesions(nullableStringList(tumor, "otherLesions"))
                    .biopsyLocation(nullableString(tumor, "biopsyLocation"))
                    .build();
        }

        @NotNull
        private static ClinicalStatus toClinicalStatus(@NotNull JsonObject clinicalStatus) {
            return ImmutableClinicalStatus.builder()
                    .who(nullableInteger(clinicalStatus, "who"))
                    .infectionStatus(toInfectionStatus(nullableObject(clinicalStatus, "infectionStatus")))
                    .ecg(toECG(nullableObject(clinicalStatus, "ecg")))
                    .lvef(nullableNumber(clinicalStatus, "lvef"))
                    .build();
        }

        @Nullable
        private static InfectionStatus toInfectionStatus(@Nullable JsonObject object) {
            if (object == null) {
                return null;
            }

            return ImmutableInfectionStatus.builder()
                    .hasActiveInfection(bool(object, "hasActiveInfection"))
                    .description(string(object, "description"))
                    .build();
        }

        @Nullable
        private static ECG toECG(@Nullable JsonObject object) {
            if (object == null) {
                return null;
            }

            return ImmutableECG.builder()
                    .hasSigAberrationLatestECG(bool(object, "hasSigAberrationLatestECG"))
                    .aberrationDescription(string(object, "aberrationDescription"))
                    .qtcfValue(nullableInteger(object, "qtcfValue"))
                    .qtcfUnit(nullableString(object, "qtcfUnit"))
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
                        .month(nullableInteger(object, "month"))
                        .categories(toTreatmentCategories(array(object, "categories")))
                        .isSystemic(bool(object, "isSystemic"))
                        .chemoType(nullableString(object, "chemoType"))
                        .immunoType(nullableString(object, "immunoType"))
                        .targetedType(nullableString(object, "targetedType"))
                        .hormoneType(nullableString(object, "hormoneType"))
                        .radioType(nullableString(object, "radioType"))
                        .carTType(nullableString(object, "carTType"))
                        .transplantType(nullableString(object, "transplantType"))
                        .supportiveType(nullableString(object, "supportiveType"))
                        .trialAcronym(nullableString(object, "trialAcronym"))
                        .build());
            }
            return priorTumorTreatmentList;
        }

        @NotNull
        private static Set<TreatmentCategory> toTreatmentCategories(@NotNull JsonArray categoryArray) {
            Set<TreatmentCategory> categories = Sets.newHashSet();
            for (JsonElement element : categoryArray) {
                categories.add(TreatmentCategory.valueOf(element.getAsString()));
            }
            return categories;
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
                        .diagnosedMonth(nullableInteger(object, "diagnosedMonth"))
                        .treatmentHistory(string(object, "treatmentHistory"))
                        .lastTreatmentYear(nullableInteger(object, "lastTreatmentYear"))
                        .lastTreatmentMonth(nullableInteger(object, "lastTreatmentMonth"))
                        .isActive(bool(object, "isActive"))
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
                        .year(nullableInteger(object, "year"))
                        .doids(stringList(object, "doids"))
                        .category(string(object, "category"))
                        .build());
            }
            return priorOtherConditionList;
        }

        @NotNull
        private static List<PriorMolecularTest> toPriorMolecularTests(@NotNull JsonArray priorMolecularTests) {
            List<PriorMolecularTest> priorMolecularTestList = Lists.newArrayList();
            for (JsonElement element : priorMolecularTests) {
                JsonObject object = element.getAsJsonObject();
                priorMolecularTestList.add(ImmutablePriorMolecularTest.builder()
                        .test(string(object, "test"))
                        .item(string(object, "item"))
                        .measure(nullableString(object, "measure"))
                        .scoreText(nullableString(object, "scoreText"))
                        .scoreValue(nullableNumber(object, "scoreValue"))
                        .unit(nullableString(object, "unit"))
                        .build());
            }
            return priorMolecularTestList;
        }

        @NotNull
        private static List<Complication> toComplications(@NotNull JsonArray complications) {
            List<Complication> complicationList = Lists.newArrayList();
            for (JsonElement element : complications) {
                JsonObject object = element.getAsJsonObject();
                complicationList.add(ImmutableComplication.builder()
                        .name(string(object, "name"))
                        .year(nullableInteger(object, "year"))
                        .month(nullableInteger(object, "month"))
                        .build());
            }
            return complicationList;
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
                        .unit(LabUnit.valueOf(string(object, "unit")))
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
        private static List<Intolerance> toIntolerances(@NotNull JsonArray allergies) {
            List<Intolerance> intoleranceList = Lists.newArrayList();
            for (JsonElement element : allergies) {
                JsonObject object = element.getAsJsonObject();
                intoleranceList.add(ImmutableIntolerance.builder()
                        .name(string(object, "name"))
                        .doids(stringList(object, "doids"))
                        .category(string(object, "category"))
                        .type(string(object, "type"))
                        .clinicalStatus(string(object, "clinicalStatus"))
                        .verificationStatus(string(object, "verificationStatus"))
                        .criticality(string(object, "criticality"))
                        .build());
            }
            return intoleranceList;
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
        private static List<BodyWeight> toBodyWeights(@NotNull JsonArray bodyWeights) {
            List<BodyWeight> bodyWeightList = Lists.newArrayList();
            for (JsonElement element : bodyWeights) {
                JsonObject object = element.getAsJsonObject();
                bodyWeightList.add(ImmutableBodyWeight.builder()
                        .date(date(object, "date"))
                        .value(number(object, "value"))
                        .unit(string(object, "unit"))
                        .build());
            }
            return bodyWeightList;
        }

        @NotNull
        private static List<VitalFunction> toVitalFunctions(@NotNull JsonArray vitalFunctions) {
            List<VitalFunction> vitalFunctionList = Lists.newArrayList();
            for (JsonElement element : vitalFunctions) {
                JsonObject object = element.getAsJsonObject();
                vitalFunctionList.add(ImmutableVitalFunction.builder()
                        .date(date(object, "date"))
                        .category(VitalFunctionCategory.valueOf(string(object, "category")))
                        .subcategory(string(object, "subcategory"))
                        .value(number(object, "value"))
                        .unit(string(object, "unit"))
                        .build());
            }
            return vitalFunctionList;
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
                        .categories(stringList(object, "categories"))
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
