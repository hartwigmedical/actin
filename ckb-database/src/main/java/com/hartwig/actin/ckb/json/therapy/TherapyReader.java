package com.hartwig.actin.ckb.json.therapy;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.ClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.DescriptionInfo;
import com.hartwig.actin.ckb.json.common.DrugInfo;
import com.hartwig.actin.ckb.json.common.EvidenceInfo;
import com.hartwig.actin.ckb.json.common.GlobalApprovalStatusInfo;
import com.hartwig.actin.ckb.json.common.ImmutableClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDescriptionInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDrugInfo;
import com.hartwig.actin.ckb.json.common.ImmutableEvidenceInfo;
import com.hartwig.actin.ckb.json.common.ImmutableGlobalApprovalStatusInfo;
import com.hartwig.actin.ckb.json.common.ImmutableIndicationInfo;
import com.hartwig.actin.ckb.json.common.ImmutableMolecularProfileInfo;
import com.hartwig.actin.ckb.json.common.ImmutableReferenceInfo;
import com.hartwig.actin.ckb.json.common.ImmutableTherapyInfo;
import com.hartwig.actin.ckb.json.common.IndicationInfo;
import com.hartwig.actin.ckb.json.common.MolecularProfileInfo;
import com.hartwig.actin.ckb.json.common.ReferenceInfo;
import com.hartwig.actin.ckb.json.common.TherapyInfo;
import com.hartwig.actin.ckb.util.DateConverter;
import com.hartwig.actin.util.json.Json;
import com.hartwig.actin.util.json.JsonDatamodelChecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TherapyReader extends CkbJsonDirectoryReader<JsonTherapy> {
    private static final Logger LOGGER = LogManager.getLogger(TherapyReader.class);

    public TherapyReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonTherapy read(@NotNull final JsonObject object) {
        JsonDatamodelChecker therapyObjectChecker = TherapyDatamodelChecker.therapyObjectChecker();
        therapyObjectChecker.check(object);

        return ImmutableJsonTherapy.builder()
                .id(Json.integer(object, "id"))
                .therapyName(Json.string(object, "therapyName"))
                .synonyms(Json.nullableStringList(object, "synonyms"))
                .descriptions(extractDescriptions(object.getAsJsonArray("therapyDescriptions")))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .updateDate(DateConverter.toDate(Json.nullableString(object, "updateDate")))
                .evidence(extractEvidence(object.getAsJsonArray("evidence")))
                .clinicalTrials(extractClinicalTrials(object.getAsJsonArray("clinicalTrials")))
                .drugs(extractDrugs(object.getAsJsonArray("drugs")))
                .globalApprovalStatuses(extractGlobalApprovalStatuses(object.getAsJsonArray("globalApprovalStatus")))
                .build();
    }

    @NotNull
    private static List<DescriptionInfo> extractDescriptions(@NotNull JsonArray jsonArray) {
        List<DescriptionInfo> descriptions = Lists.newArrayList();
        JsonDatamodelChecker descriptionChecker = TherapyDatamodelChecker.descriptionObjectChecker();

        for (JsonElement description : jsonArray) {
            JsonObject descriptionJsonObject = description.getAsJsonObject();
            descriptionChecker.check(descriptionJsonObject);

            descriptions.add(ImmutableDescriptionInfo.builder()
                    .description(Json.nullableString(descriptionJsonObject, "description"))
                    .references(extractReferences(descriptionJsonObject.getAsJsonArray("references")))
                    .build());
        }
        return descriptions;
    }

    @NotNull
    private static List<ReferenceInfo> extractReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker referenceChecker = TherapyDatamodelChecker.referenceObjectChecker();

        for (JsonElement reference : jsonArray) {
            JsonObject referenceJsonObject = reference.getAsJsonObject();
            referenceChecker.check(referenceJsonObject);

            references.add(ImmutableReferenceInfo.builder()
                    .id(Json.integer(referenceJsonObject, "id"))
                    .pubMedId(Json.nullableString(referenceJsonObject, "pubMedId"))
                    .title(Json.nullableString(referenceJsonObject, "title"))
                    .url(Json.nullableString(referenceJsonObject, "url"))
                    .build());
        }
        return references;
    }

    @NotNull
    private static List<EvidenceInfo> extractEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> evidences = Lists.newArrayList();
        JsonDatamodelChecker evidenceChecker = TherapyDatamodelChecker.evidenceObjectChecker();

        for (JsonElement evidence : jsonArray) {
            JsonObject evidenceJsonObject = evidence.getAsJsonObject();
            evidenceChecker.check(evidenceJsonObject);

            evidences.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(evidenceJsonObject, "id"))
                    .approvalStatus(Json.string(evidenceJsonObject, "approvalStatus"))
                    .evidenceType(Json.string(evidenceJsonObject, "evidenceType"))
                    .efficacyEvidence(Json.string(evidenceJsonObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfile(evidenceJsonObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapy(evidenceJsonObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(evidenceJsonObject.getAsJsonObject("indication")))
                    .responseType(Json.string(evidenceJsonObject, "responseType"))
                    .references(extractReferences(evidenceJsonObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(evidenceJsonObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(evidenceJsonObject, "ampCapAscoInferredTier"))
                    .build());
        }
        return evidences;
    }

    @NotNull
    private static MolecularProfileInfo extractMolecularProfile(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker molecularProfileChecker = TherapyDatamodelChecker.molecularProfileObjectChecker();
        molecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker therapyChecker = TherapyDatamodelChecker.therapyChecker();
        therapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndication(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker indicationChecker = TherapyDatamodelChecker.indicationChecker();
        indicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static List<ClinicalTrialInfo> extractClinicalTrials(@NotNull JsonArray jsonArray) {
        List<ClinicalTrialInfo> clinicalTrials = Lists.newArrayList();
        JsonDatamodelChecker clinicalTrialChecker = TherapyDatamodelChecker.clinicalTrialChecker();

        for (JsonElement clinicalTrial : jsonArray) {
            JsonObject clinicalTrialJsonObject = clinicalTrial.getAsJsonObject();
            clinicalTrialChecker.check(clinicalTrialJsonObject);

            clinicalTrials.add(ImmutableClinicalTrialInfo.builder()
                    .nctId(Json.string(clinicalTrialJsonObject, "nctId"))
                    .title(Json.string(clinicalTrialJsonObject, "title"))
                    .phase(Json.nullableString(clinicalTrialJsonObject, "phase"))
                    .recruitment(Json.string(clinicalTrialJsonObject, "recruitment"))
                    .therapies(extractTherapyList(clinicalTrialJsonObject.getAsJsonArray("therapies")))
                    .build());
        }
        return clinicalTrials;
    }

    @NotNull
    private static List<TherapyInfo> extractTherapyList(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker therapyChecker = TherapyDatamodelChecker.therapyChecker();

        for (JsonElement therapy : jsonArray) {
            JsonObject therapyJsonObject = therapy.getAsJsonObject();
            therapyChecker.check(therapyJsonObject);

            therapies.add(ImmutableTherapyInfo.builder()
                    .id(Json.integer(therapyJsonObject, "id"))
                    .therapyName(Json.string(therapyJsonObject, "therapyName"))
                    .synonyms(Json.nullableStringList(therapyJsonObject, "synonyms"))
                    .build());
        }
        return therapies;
    }

    @NotNull
    private static List<DrugInfo> extractDrugs(@NotNull JsonArray jsonArray) {
        List<DrugInfo> drugs = Lists.newArrayList();
        JsonDatamodelChecker drugChecker = TherapyDatamodelChecker.drugChecker();

        for (JsonElement drug : jsonArray) {
            JsonObject drugJsonObject = drug.getAsJsonObject();
            drugChecker.check(drugJsonObject);

            drugs.add(ImmutableDrugInfo.builder()
                    .id(Json.integer(drugJsonObject, "id"))
                    .drugName(Json.string(drugJsonObject, "drugName"))
                    .terms(Json.stringList(drugJsonObject, "terms"))
                    .build());
        }
        return drugs;
    }

    @NotNull
    private static List<GlobalApprovalStatusInfo> extractGlobalApprovalStatuses(@NotNull JsonArray jsonArray) {
        List<GlobalApprovalStatusInfo> globalApprovalStatuses = Lists.newArrayList();
        JsonDatamodelChecker globalApprovalStatusChecker = TherapyDatamodelChecker.globalApprovalStatusChecker();

        for (JsonElement globalApprovalStatus : jsonArray) {
            JsonObject globalApprovalStatusJsonObject = globalApprovalStatus.getAsJsonObject();
            globalApprovalStatusChecker.check(globalApprovalStatusJsonObject);

            globalApprovalStatuses.add(ImmutableGlobalApprovalStatusInfo.builder()
                    .id(Json.integer(globalApprovalStatusJsonObject, "id"))
                    .therapy(extractTherapy(globalApprovalStatusJsonObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(globalApprovalStatusJsonObject.getAsJsonObject("indication")))
                    .molecularProfile(extractMolecularProfile(globalApprovalStatusJsonObject.getAsJsonObject("molecularProfile")))
                    .approvalAuthority(Json.string(globalApprovalStatusJsonObject, "approvalAuthority"))
                    .approvalStatus(Json.string(globalApprovalStatusJsonObject, "approvalStatus"))
                    .build());
        }
        return globalApprovalStatuses;
    }
}