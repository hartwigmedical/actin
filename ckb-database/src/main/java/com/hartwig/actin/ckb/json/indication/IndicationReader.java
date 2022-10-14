package com.hartwig.actin.ckb.json.indication;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.ClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.EvidenceInfo;
import com.hartwig.actin.ckb.json.common.ImmutableClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.ImmutableEvidenceInfo;
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

public class IndicationReader extends CkbJsonDirectoryReader<JsonIndication> {
    private static final Logger LOGGER = LogManager.getLogger(IndicationReader.class);

    public IndicationReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonIndication read(@NotNull final JsonObject object) {
        JsonDatamodelChecker indicationChecker = IndicationDatamodelChecker.indicationObjectChecker();
        indicationChecker.check(object);

        return ImmutableJsonIndication.builder()
                .id(Json.integer(object, "id"))
                .name(Json.string(object, "name"))
                .source(Json.string(object, "source"))
                .definition(Json.nullableString(object, "definition"))
                .currentPreferredTerm(Json.nullableString(object, "currentPreferredTerm"))
                .lastUpdateDateFromDO(DateConverter.toDate(Json.nullableString(object, "lastUpdateDateFromDO")))
                .altIds(Json.stringList(object, "altIds"))
                .termId(Json.string(object, "termId"))
                .evidence(extractEvidence(object.getAsJsonArray("evidence")))
                .clinicalTrials(extractClinicalTrials(object.getAsJsonArray("clinicalTrials")))
                .build();
    }

    @NotNull
    private static List<EvidenceInfo> extractEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> evidences = Lists.newArrayList();
        JsonDatamodelChecker evidenceChecker = IndicationDatamodelChecker.evidenceObjectChecker();

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
        JsonDatamodelChecker evidenceMolecularProfileChecker = IndicationDatamodelChecker.evidenceMolecularProfileObjectChecker();
        evidenceMolecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker evidenceTherapyChecker = IndicationDatamodelChecker.evidenceTherapyObjectChecker();
        evidenceTherapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndication(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker evidenceIndicationChecker = IndicationDatamodelChecker.evidenceIndicationObjectChecker();
        evidenceIndicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static List<ReferenceInfo> extractReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker evidenceReferenceChecker = IndicationDatamodelChecker.evidenceReferenceObjectChecker();

        for (JsonElement reference : jsonArray) {
            JsonObject referenceJsonObject = reference.getAsJsonObject();
            evidenceReferenceChecker.check(referenceJsonObject);

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
    private static List<ClinicalTrialInfo> extractClinicalTrials(@NotNull JsonArray jsonArray) {
        List<ClinicalTrialInfo> clinicalTrials = Lists.newArrayList();
        JsonDatamodelChecker clinicalTrialChecker = IndicationDatamodelChecker.clinicalTrialObjectChecker();

        for (JsonElement clinicalTrial : jsonArray) {
            JsonObject clinicalTrialJsonObject = clinicalTrial.getAsJsonObject();
            clinicalTrialChecker.check(clinicalTrialJsonObject);

            String nctId = Json.string(clinicalTrialJsonObject, "nctId");
            String phase = Json.nullableString(clinicalTrialJsonObject, "phase");

            if (phase == null) {
                LOGGER.warn("phase of study '{}' is null in IndicationReader", nctId);
            }

            clinicalTrials.add(ImmutableClinicalTrialInfo.builder()
                    .nctId(nctId)
                    .title(Json.string(clinicalTrialJsonObject, "title"))
                    .phase(phase)
                    .recruitment(Json.string(clinicalTrialJsonObject, "recruitment"))
                    .therapies(extractTherapies(clinicalTrialJsonObject.getAsJsonArray("therapies")))
                    .build());
        }
        return clinicalTrials;
    }

    @NotNull
    private static List<TherapyInfo> extractTherapies(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker clinicalTrialTherapyChecker = IndicationDatamodelChecker.evidenceTherapyObjectChecker();

        for (JsonElement therapy : jsonArray) {
            JsonObject therapyJsonObject = therapy.getAsJsonObject();
            clinicalTrialTherapyChecker.check(therapyJsonObject);

            therapies.add(ImmutableTherapyInfo.builder()
                    .id(Json.integer(therapyJsonObject, "id"))
                    .therapyName(Json.string(therapyJsonObject, "therapyName"))
                    .synonyms(Json.nullableStringList(therapyJsonObject, "synonyms"))
                    .build());
        }
        return therapies;
    }
}