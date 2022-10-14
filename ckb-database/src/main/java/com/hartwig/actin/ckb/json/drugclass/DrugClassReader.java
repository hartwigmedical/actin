package com.hartwig.actin.ckb.json.drugclass;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.DrugInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDrugInfo;
import com.hartwig.actin.ckb.json.common.ImmutableTreatmentApproachInfo;
import com.hartwig.actin.ckb.json.common.TreatmentApproachInfo;
import com.hartwig.actin.ckb.util.DateConverter;
import com.hartwig.actin.util.json.Json;
import com.hartwig.actin.util.json.JsonDatamodelChecker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrugClassReader extends CkbJsonDirectoryReader<JsonDrugClass> {

    public DrugClassReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonDrugClass read(@NotNull final JsonObject object) {
        JsonDatamodelChecker drugsClassChecker = DrugClassDatamodelChecker.drugClassObjectChecker();
        drugsClassChecker.check(object);

        return ImmutableJsonDrugClass.builder()
                .id(Json.integer(object, "id"))
                .drugClass(Json.string(object, "drugClass"))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .drugs(extractDrugs(object.getAsJsonArray("drugs")))
                .treatmentApproaches(extractTreatmentApproaches(object.getAsJsonArray("treatmentApproaches")))
                .build();
    }

    @NotNull
    private static List<DrugInfo> extractDrugs(@NotNull JsonArray jsonArray) {
        List<DrugInfo> drugs = Lists.newArrayList();
        JsonDatamodelChecker drugChecker = DrugClassDatamodelChecker.drugObjectChecker();

        for (JsonElement drug : jsonArray) {
            JsonObject drugObject = drug.getAsJsonObject();
            drugChecker.check(drugObject);

            drugs.add(ImmutableDrugInfo.builder()
                    .id(Json.integer(drugObject, "id"))
                    .drugName(Json.string(drugObject, "drugName"))
                    .terms(Json.nullableStringList(drugObject, "terms"))
                    .build());
        }
        return drugs;
    }

    @NotNull
    private static List<TreatmentApproachInfo> extractTreatmentApproaches(@NotNull JsonArray jsonArray) {
        List<TreatmentApproachInfo> treatmentApproaches = Lists.newArrayList();
        JsonDatamodelChecker treatmentApproachChecker = DrugClassDatamodelChecker.treatmentApproachObjectChecker();

        for (JsonElement treatmentApproach : jsonArray) {
            JsonObject treatmentApproachObject = treatmentApproach.getAsJsonObject();
            treatmentApproachChecker.check(treatmentApproachObject);

            treatmentApproaches.add(ImmutableTreatmentApproachInfo.builder()
                    .id(Json.integer(treatmentApproachObject, "id"))
                    .name(Json.string(treatmentApproachObject, "name"))
                    .profileName(Json.string(treatmentApproachObject, "profileName"))
                    .build());
        }
        return treatmentApproaches;
    }
}
