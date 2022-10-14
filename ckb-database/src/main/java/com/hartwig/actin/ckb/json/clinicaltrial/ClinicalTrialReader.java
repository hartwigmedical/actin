package com.hartwig.actin.ckb.json.clinicaltrial;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.ImmutableIndicationInfo;
import com.hartwig.actin.ckb.json.common.ImmutableMolecularProfileInfo;
import com.hartwig.actin.ckb.json.common.ImmutableTherapyInfo;
import com.hartwig.actin.ckb.json.common.IndicationInfo;
import com.hartwig.actin.ckb.json.common.MolecularProfileInfo;
import com.hartwig.actin.ckb.json.common.TherapyInfo;
import com.hartwig.actin.ckb.util.DateConverter;
import com.hartwig.actin.util.json.Json;
import com.hartwig.actin.util.json.JsonDatamodelChecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClinicalTrialReader extends CkbJsonDirectoryReader<JsonClinicalTrial> {
    private static final Logger LOGGER = LogManager.getLogger(ClinicalTrialReader.class);

    public ClinicalTrialReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonClinicalTrial read(@NotNull final JsonObject object) {
        JsonDatamodelChecker clinicalTrialChecker = ClinicalTrialDatamodelChecker.clinicalTrialObjectChecker();
        clinicalTrialChecker.check(object);

        String nctId = Json.string(object, "nctId");
        String phase = Json.nullableString(object, "phase");

        if (phase == null) {
            LOGGER.warn("phase of study '{}' is null in ClinicalTrialReader", nctId);
        }

        return ImmutableJsonClinicalTrial.builder()
                .nctId(nctId)
                .title(Json.string(object, "title"))
                .phase(phase)
                .recruitment(Json.string(object, "recruitment"))
                .therapies(extractTherapies(object.getAsJsonArray("therapies")))
                .ageGroups(Json.stringList(object, "ageGroups"))
                .gender(Json.nullableString(object, "gender"))
                .variantRequirements(Json.string(object, "variantRequirements"))
                .sponsors(Json.nullableString(object, "sponsors"))
                .updateDate(DateConverter.toDate(Json.string(object, "updateDate")))
                .indications(extractIndications(object.getAsJsonArray("indications")))
                .variantRequirementDetails(extractVariantRequirementDetails(object.getAsJsonArray("variantRequirementDetails")))
                .locations(extractLocations(object.getAsJsonArray("clinicalTrialLocations")))
                .coveredCountries(Json.stringList(object, "coveredCountries"))
                .build();
    }

    @NotNull
    private static List<TherapyInfo> extractTherapies(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker therapyChecker = ClinicalTrialDatamodelChecker.therapyObjectChecker();

        for (JsonElement therapy : jsonArray) {
            JsonObject therapyObject = therapy.getAsJsonObject();
            therapyChecker.check(therapyObject);

            therapies.add(ImmutableTherapyInfo.builder()
                    .id(Json.integer(therapyObject, "id"))
                    .therapyName(Json.string(therapyObject, "therapyName"))
                    .synonyms(Json.nullableStringList(therapyObject, "synonyms"))
                    .build());
        }
        return therapies;
    }

    @NotNull
    private static List<IndicationInfo> extractIndications(@NotNull JsonArray jsonArray) {
        List<IndicationInfo> indications = Lists.newArrayList();
        JsonDatamodelChecker indicationChecker = ClinicalTrialDatamodelChecker.indicationObjectChecker();

        for (JsonElement indication : jsonArray) {
            JsonObject indicationObject = indication.getAsJsonObject();
            indicationChecker.check(indicationObject);

            indications.add(ImmutableIndicationInfo.builder()
                    .id(Json.integer(indicationObject, "id"))
                    .name(Json.string(indicationObject, "name"))
                    .source(Json.string(indicationObject, "source"))
                    .build());
        }
        return indications;
    }

    @NotNull
    private static MolecularProfileInfo extractMolecularProfile(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker molecularProfileChecker = ClinicalTrialDatamodelChecker.molecularProfileObjectChecker();
        molecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static List<JsonVariantRequirementDetail> extractVariantRequirementDetails(@NotNull JsonArray jsonArray) {
        List<JsonVariantRequirementDetail> variantRequirementDetails = Lists.newArrayList();
        JsonDatamodelChecker variantRequirementDetailChecker =
                ClinicalTrialDatamodelChecker.variantRequirementDetailObjectChecker();

        for (JsonElement variantRequirementDetail : jsonArray) {
            JsonObject variantRequirementDetailObject = variantRequirementDetail.getAsJsonObject();
            variantRequirementDetailChecker.check(variantRequirementDetailObject);

            variantRequirementDetails.add(ImmutableJsonVariantRequirementDetail.builder()
                    .molecularProfile(extractMolecularProfile(variantRequirementDetailObject.getAsJsonObject("molecularProfile")))
                    .requirementType(Json.string(variantRequirementDetailObject, "requirementType"))
                    .build());
        }
        return variantRequirementDetails;
    }

    @NotNull
    private static List<JsonLocation> extractLocations(@NotNull JsonArray jsonArray) {
        List<JsonLocation> locations = Lists.newArrayList();
        JsonDatamodelChecker locationChecker = ClinicalTrialDatamodelChecker.locationObjectChecker();

        for (JsonElement location : jsonArray) {
            JsonObject locationObject = location.getAsJsonObject();
            locationChecker.check(locationObject);

            locations.add(ImmutableJsonLocation.builder()
                    .nctId(Json.string(locationObject, "nctId"))
                    .facility(Json.nullableString(locationObject, "facility"))
                    .city(Json.string(locationObject, "city"))
                    .country(Json.string(locationObject, "country"))
                    .status(Json.nullableString(locationObject, "status"))
                    .state(Json.nullableString(locationObject, "state"))
                    .zip(Json.nullableString(locationObject, "zip"))
                    .contacts(extractContacts(locationObject.getAsJsonArray("clinicalTrialContacts")))
                    .build());
        }
        return locations;
    }

    @NotNull
    private static List<JsonContact> extractContacts(@NotNull JsonArray jsonArray) {
        List<JsonContact> contacts = Lists.newArrayList();
        JsonDatamodelChecker contactChecker = ClinicalTrialDatamodelChecker.contactObjectChecker();

        for (JsonElement contact : jsonArray) {
            JsonObject contactObject = contact.getAsJsonObject();

            contactChecker.check(contactObject);
            contacts.add(ImmutableJsonContact.builder()
                    .name(Json.nullableString(contactObject, "name"))
                    .email(Json.nullableString(contactObject, "email"))
                    .phone(Json.nullableString(contactObject, "phone"))
                    .phoneExt(Json.nullableString(contactObject, "phoneExt"))
                    .role(Json.string(contactObject, "role"))
                    .build());
        }
        return contacts;
    }
}
