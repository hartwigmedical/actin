package com.hartwig.actin.ckb.json.drug;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.ClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.DescriptionInfo;
import com.hartwig.actin.ckb.json.common.DrugClassInfo;
import com.hartwig.actin.ckb.json.common.EvidenceInfo;
import com.hartwig.actin.ckb.json.common.GlobalApprovalStatusInfo;
import com.hartwig.actin.ckb.json.common.ImmutableClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDescriptionInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDrugClassInfo;
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

public class DrugReader extends CkbJsonDirectoryReader<JsonDrug> {
    private static final Logger LOGGER = LogManager.getLogger(DrugReader.class);

    public DrugReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonDrug read(@NotNull final JsonObject object) {
        JsonDatamodelChecker drugChecker = DrugDatamodelChecker.drugObjectChecker();
        drugChecker.check(object);

        return ImmutableJsonDrug.builder()
                .id(Json.integer(object, "id"))
                .drugName(Json.string(object, "drugName"))
                .terms(Json.stringList(object, "terms"))
                .synonyms(Json.stringList(object, "synonyms"))
                .tradeName(Json.nullableString(object, "tradeName"))
                .descriptions(extractDescriptions(object.getAsJsonArray("drugDescriptions")))
                .drugClasses(extractDrugsClasses(object.getAsJsonArray("drugClasses")))
                .casRegistryNum(Json.nullableString(object, "casRegistryNum"))
                .ncitId(Json.nullableString(object, "ncitId"))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .clinicalTrials(extractClinicalTrials(object.getAsJsonArray("clinicalTrials")))
                .evidence(extractEvidence(object.getAsJsonArray("evidence")))
                .therapies(extractTherapies(object.getAsJsonArray("therapies")))
                .globalApprovalStatus(extractGlobalApprovalStatus(object.getAsJsonArray("globalApprovalStatus")))
                .build();
    }

    @NotNull
    private static List<DescriptionInfo> extractDescriptions(@NotNull JsonArray jsonArray) {
        List<DescriptionInfo> descriptions = Lists.newArrayList();
        JsonDatamodelChecker descriptionChecker = DrugDatamodelChecker.descriptionObjectChecker();
        for (JsonElement description : jsonArray) {
            JsonObject descriptionObject = description.getAsJsonObject();
            descriptionChecker.check(descriptionObject);

            descriptions.add(ImmutableDescriptionInfo.builder()
                    .description(Json.string(descriptionObject, "description"))
                    .references(extractDrugReferences(descriptionObject.getAsJsonArray("references")))
                    .build());
        }
        return descriptions;
    }

    @NotNull
    private static List<ReferenceInfo> extractDrugReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker referenceChecker = DrugDatamodelChecker.referenceObjectChecker();

        for (JsonElement reference : jsonArray) {
            JsonObject referenceObject = reference.getAsJsonObject();
            referenceChecker.check(referenceObject);

            references.add(ImmutableReferenceInfo.builder()
                    .id(Json.integer(referenceObject, "id"))
                    .pubMedId(Json.nullableString(referenceObject, "pubMedId"))
                    .title(Json.nullableString(referenceObject, "title"))
                    .url(Json.nullableString(referenceObject, "url"))
                    .authors(Json.nullableString(referenceObject, "authors"))
                    .journal(Json.nullableString(referenceObject, "journal"))
                    .volume(Json.nullableString(referenceObject, "volume"))
                    .issue(Json.nullableString(referenceObject, "issue"))
                    .date(Json.nullableString(referenceObject, "date"))
                    .abstractText(Json.nullableString(referenceObject, "abstractText"))
                    .year(Json.nullableString(referenceObject, "year"))
                    .build());
        }
        return references;
    }

    @NotNull
    private static List<DrugClassInfo> extractDrugsClasses(@NotNull JsonArray jsonArray) {
        List<DrugClassInfo> drugClasses = Lists.newArrayList();
        JsonDatamodelChecker drugClassChecker = DrugDatamodelChecker.drugClassObjectChecker();

        for (JsonElement drugClass : jsonArray) {
            JsonObject drugClassObject = drugClass.getAsJsonObject();
            drugClassChecker.check(drugClassObject);

            drugClasses.add(ImmutableDrugClassInfo.builder()
                    .id(Json.integer(drugClassObject, "id"))
                    .drugClass(Json.string(drugClassObject, "drugClass"))
                    .build());
        }
        return drugClasses;
    }

    @NotNull
    private static List<ClinicalTrialInfo> extractClinicalTrials(@NotNull JsonArray jsonArray) {
        List<ClinicalTrialInfo> clinicalTrials = Lists.newArrayList();
        JsonDatamodelChecker clinicalTrialChecker = DrugDatamodelChecker.clinicalTrialObjectChecker();

        for (JsonElement clinicalTrial : jsonArray) {
            JsonObject clinicalTrialObject = clinicalTrial.getAsJsonObject();
            clinicalTrialChecker.check(clinicalTrialObject);

            clinicalTrials.add(ImmutableClinicalTrialInfo.builder()
                    .nctId(Json.string(clinicalTrialObject, "nctId"))
                    .title(Json.string(clinicalTrialObject, "title"))
                    .phase(Json.nullableString(clinicalTrialObject, "phase"))
                    .recruitment(Json.string(clinicalTrialObject, "recruitment"))
                    .therapies(extractClinicalTrialTherapies(clinicalTrialObject.getAsJsonArray("therapies")))
                    .build());
        }
        return clinicalTrials;
    }

    @NotNull
    private static List<TherapyInfo> extractClinicalTrialTherapies(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker clinicalTrialTherapyChecker = DrugDatamodelChecker.clinicalTrialTherapyObjectChecker();

        for (JsonElement therapy : jsonArray) {
            JsonObject therapyObject = therapy.getAsJsonObject();
            clinicalTrialTherapyChecker.check(therapyObject);

            therapies.add(ImmutableTherapyInfo.builder()
                    .id(Json.integer(therapyObject, "id"))
                    .therapyName(Json.string(therapyObject, "therapyName"))
                    .synonyms(Json.nullableStringList(therapyObject, "synonyms"))
                    .build());
        }
        return therapies;
    }

    @NotNull
    private static List<EvidenceInfo> extractEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> evidences = Lists.newArrayList();
        JsonDatamodelChecker evidenceChecker = DrugDatamodelChecker.evidenceObjectChecker();

        for (JsonElement evidence : jsonArray) {
            JsonObject evidenceObject = evidence.getAsJsonObject();
            evidenceChecker.check(evidenceObject);

            evidences.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(evidenceObject, "id"))
                    .approvalStatus(Json.string(evidenceObject, "approvalStatus"))
                    .evidenceType(Json.string(evidenceObject, "evidenceType"))
                    .efficacyEvidence(Json.string(evidenceObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfile(evidenceObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapy(evidenceObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(evidenceObject.getAsJsonObject("indication")))
                    .responseType(Json.string(evidenceObject, "responseType"))
                    .references(extractEvidenceReferences(evidenceObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(evidenceObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(evidenceObject, "ampCapAscoInferredTier"))
                    .build());
        }
        return evidences;
    }

    @NotNull
    private static MolecularProfileInfo extractMolecularProfile(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker evidenceMolecularProfileChecker = DrugDatamodelChecker.evidenceMolecularProfileObjectChecker();
        evidenceMolecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker evidenceTherapyChecker = DrugDatamodelChecker.evidenceTherapyObjectChecker();
        evidenceTherapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndication(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker evidenceIndicationChecker = DrugDatamodelChecker.evidenceIndicationObjectChecker();
        evidenceIndicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static List<ReferenceInfo> extractEvidenceReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker evidenceReferenceChecker = DrugDatamodelChecker.evidenceReferenceObjectChecker();

        for (JsonElement reference : jsonArray) {
            JsonObject referenceObject = reference.getAsJsonObject();
            evidenceReferenceChecker.check(referenceObject);

            references.add(ImmutableReferenceInfo.builder()
                    .id(Json.integer(referenceObject, "id"))
                    .pubMedId(Json.nullableString(referenceObject, "pubMedId"))
                    .title(Json.nullableString(referenceObject, "title"))
                    .url(Json.nullableString(referenceObject, "url"))
                    .build());
        }
        return references;
    }

    @NotNull
    private static List<TherapyInfo> extractTherapies(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker therapyChecker = DrugDatamodelChecker.therapyObjectChecker();

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
    private static List<GlobalApprovalStatusInfo> extractGlobalApprovalStatus(@NotNull JsonArray jsonArray) {
        List<GlobalApprovalStatusInfo> globalApprovalStatuses = Lists.newArrayList();
        JsonDatamodelChecker drugGlobalApprovalStatusChecker = DrugDatamodelChecker.globalApprovalStatusObjectChecker();

        for (JsonElement globalApprovalStatus : jsonArray) {
            JsonObject globalStatusObject = globalApprovalStatus.getAsJsonObject();
            drugGlobalApprovalStatusChecker.check(globalStatusObject);

            globalApprovalStatuses.add(ImmutableGlobalApprovalStatusInfo.builder()
                    .id(Json.integer(globalStatusObject, "id"))
                    .therapy(extractTherapyGlobalApprovalStatus(globalStatusObject.getAsJsonObject("therapy")))
                    .indication(extractIndicationGlobalApprovalStatus(globalStatusObject.getAsJsonObject("indication")))
                    .molecularProfile(extractMolecularProfileGlobalApprovalStatus(globalStatusObject.getAsJsonObject("molecularProfile")))
                    .approvalAuthority(Json.string(globalStatusObject, "approvalAuthority"))
                    .approvalStatus(Json.string(globalStatusObject, "approvalStatus"))
                    .build());
        }
        return globalApprovalStatuses;
    }

    @NotNull
    private static TherapyInfo extractTherapyGlobalApprovalStatus(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker globalApprovalStatusTherapyChecker = DrugDatamodelChecker.globalApprovalStatusTherapyObjectChecker();
        globalApprovalStatusTherapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndicationGlobalApprovalStatus(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker globalApprovalStatusIndicationChecker = DrugDatamodelChecker.globalApprovalStatusIndicationObjectChecker();
        globalApprovalStatusIndicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static MolecularProfileInfo extractMolecularProfileGlobalApprovalStatus(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker globalApprovalStatusMolecularProfileChecker =
                DrugDatamodelChecker.globalApprovalStatusMolecularProfileObjectChecker();
        globalApprovalStatusMolecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }
}
