package com.hartwig.actin.ckb.json.treatmentapproach;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.DrugClassInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDrugClassInfo;
import com.hartwig.actin.ckb.json.common.ImmutableReferenceInfo;
import com.hartwig.actin.ckb.json.common.ImmutableTherapyInfo;
import com.hartwig.actin.ckb.json.common.ReferenceInfo;
import com.hartwig.actin.ckb.json.common.TherapyInfo;
import com.hartwig.actin.ckb.util.DateConverter;
import com.hartwig.actin.util.json.Json;
import com.hartwig.actin.util.json.JsonDatamodelChecker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreatmentApproachReader extends CkbJsonDirectoryReader<JsonTreatmentApproach> {

    public TreatmentApproachReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonTreatmentApproach read(@NotNull final JsonObject object) {
        JsonDatamodelChecker treatmentApproachObjectChecker = TreatmentApproachDatamodelChecker.treatmentApproachObjectChecker();
        treatmentApproachObjectChecker.check(object);

        return ImmutableJsonTreatmentApproach.builder()
                .id(Json.integer(object, "id"))
                .name(Json.string(object, "name"))
                .profileName(Json.string(object, "profileName"))
                .drugClass(object.has("drugClass") && !object.get("drugClass").isJsonNull() ? extractDrugClass(object.getAsJsonObject(
                        "drugClass")) : null)
                .therapy(object.has("therapy") && !object.get("therapy").isJsonNull()
                        ? extractTherapy(object.getAsJsonObject("therapy"))
                        : null)
                .references(extractReferences(object.getAsJsonArray("references")))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .updateDate(DateConverter.toDate(Json.string(object, "updateDate")))
                .build();
    }

    @NotNull
    private static DrugClassInfo extractDrugClass(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker drugClassObjectChecker = TreatmentApproachDatamodelChecker.drugClassObjectChecker();
        drugClassObjectChecker.check(jsonObject);

        return ImmutableDrugClassInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .drugClass(Json.string(jsonObject, "drugClass"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker therapyObjectChecker = TreatmentApproachDatamodelChecker.therapyObjectChecker();
        therapyObjectChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static List<ReferenceInfo> extractReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker referenceObjectChecker = TreatmentApproachDatamodelChecker.referenceObjectChecker();

        for (JsonElement reference : jsonArray) {
            JsonObject referenceJsonObject = reference.getAsJsonObject();
            referenceObjectChecker.check(referenceJsonObject);

            references.add(ImmutableReferenceInfo.builder()
                    .id(Json.integer(referenceJsonObject, "id"))
                    .pubMedId(Json.nullableString(referenceJsonObject, "pubMedId"))
                    .title(Json.string(referenceJsonObject, "title"))
                    .url(Json.nullableString(referenceJsonObject, "url"))
                    .build());
        }
        return references;
    }
}
