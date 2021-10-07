package com.hartwig.actin.datamodel;

import static com.hartwig.actin.util.JsonFunctions.array;
import static com.hartwig.actin.util.JsonFunctions.date;
import static com.hartwig.actin.util.JsonFunctions.integer;
import static com.hartwig.actin.util.JsonFunctions.nullableBoolean;
import static com.hartwig.actin.util.JsonFunctions.nullableDate;
import static com.hartwig.actin.util.JsonFunctions.nullableInteger;
import static com.hartwig.actin.util.JsonFunctions.nullableString;
import static com.hartwig.actin.util.JsonFunctions.nullableStringList;
import static com.hartwig.actin.util.JsonFunctions.object;
import static com.hartwig.actin.util.JsonFunctions.string;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.clinical.ClinicalStatus;
import com.hartwig.actin.datamodel.clinical.ImmutableClinicalRecord;
import com.hartwig.actin.datamodel.clinical.ImmutableClinicalStatus;
import com.hartwig.actin.datamodel.clinical.ImmutablePatientDetails;
import com.hartwig.actin.datamodel.clinical.ImmutableTumorDetails;
import com.hartwig.actin.datamodel.clinical.PatientDetails;
import com.hartwig.actin.datamodel.clinical.Sex;
import com.hartwig.actin.datamodel.clinical.TumorDetails;
import com.hartwig.actin.datamodel.clinical.TumorStage;

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
        Gson gson = new GsonBuilder().registerTypeAdapter(ClinicalModel.class, new ClinicalModelCreator()).create();

        return gson.fromJson(new JsonReader(new FileReader(clinicalModelJson)), ClinicalModel.class);
    }

    private static class ClinicalModelCreator implements JsonDeserializer<ClinicalModel> {

        @Override
        public ClinicalModel deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            List<ClinicalRecord> records = Lists.newArrayList();

            for (JsonElement element : array(jsonElement.getAsJsonObject(), "records")) {
                records.add(toClinicalRecord(element.getAsJsonObject()));
            }

            return new ClinicalModel(records);
        }

        @NotNull
        private static ClinicalRecord toClinicalRecord(@NotNull JsonObject record) {
            return ImmutableClinicalRecord.builder()
                    .sampleId(string(record, "sampleId"))
                    .patient(toPatientDetails(object(record, "patient")))
                    .tumor(toTumorDetails(object(record, "tumor")))
                    .clinicalStatus(toClinicalStatus(object(record, "clinicalStatus")))
                    .build();

        }

        @NotNull
        private static PatientDetails toPatientDetails(@NotNull JsonObject patient) {
            return ImmutablePatientDetails.builder()
                    .sex(Sex.valueOf(string(patient, "sex")))
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
                    .hasMeasurableLesionRecist(nullableBoolean(tumor, "hasMeasurableLesionRecist"))
                    .hasBrainLesions(nullableBoolean(tumor, "hasBrainLesions"))
                    .hasActiveBrainLesions(nullableBoolean(tumor, "hasActiveBrainLesions"))
                    .hasSymptomaticBrainLesions(nullableBoolean(tumor, "hasSymptomaticBrainLesions"))
                    .hasCnsLesions(nullableBoolean(tumor, "hasCnsLesions"))
                    .hasActiveCnsLesions(nullableBoolean(tumor, "hasActiveCnsLesions"))
                    .hasSymptomaticCnsLesions(nullableBoolean(tumor, "hasSymptomaticCnsLesions"))
                    .hasBoneLesions(nullableBoolean(tumor, "hasBoneLesions"))
                    .hasLiverLesions(nullableBoolean(tumor, "hasLiverLesions"))
                    .hasOtherLesions(nullableBoolean(tumor, "hasOtherLesions"))
                    .otherLesions(nullableStringList(tumor, "otherLesions"))
                    .biopsyLocation(nullableString(tumor, "biopsyLocation"))
                    .build();
        }

        @NotNull
        private static ClinicalStatus toClinicalStatus(@NotNull JsonObject clinicalStatus) {
            return ImmutableClinicalStatus.builder()
                    .who(nullableInteger(clinicalStatus, "who"))
                    .hasActiveInfection(nullableBoolean(clinicalStatus, "hasActiveInfection"))
                    .hasSigAberrationLatestEcg(nullableBoolean(clinicalStatus, "hasSigAberrationLatestEcg"))
                    .ecgAberrationDescription(nullableString(clinicalStatus, "ecgAberrationDescription"))
                    .build();
        }
    }
}
