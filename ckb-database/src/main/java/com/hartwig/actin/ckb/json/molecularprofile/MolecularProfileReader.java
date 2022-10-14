package com.hartwig.actin.ckb.json.molecularprofile;

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
import com.hartwig.actin.ckb.json.common.ImmutableTreatmentApproachInfo;
import com.hartwig.actin.ckb.json.common.ImmutableVariantInfo;
import com.hartwig.actin.ckb.json.common.IndicationInfo;
import com.hartwig.actin.ckb.json.common.MolecularProfileInfo;
import com.hartwig.actin.ckb.json.common.ReferenceInfo;
import com.hartwig.actin.ckb.json.common.TherapyInfo;
import com.hartwig.actin.ckb.json.common.TreatmentApproachInfo;
import com.hartwig.actin.ckb.json.common.VariantInfo;
import com.hartwig.actin.ckb.util.DateConverter;
import com.hartwig.actin.util.json.Json;
import com.hartwig.actin.util.json.JsonDatamodelChecker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularProfileReader extends CkbJsonDirectoryReader<JsonMolecularProfile> {

    public MolecularProfileReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonMolecularProfile read(@NotNull final JsonObject object) {
        JsonDatamodelChecker molecularProfileChecker = MolecularProfileDatamodelChecker.molecularProfileObjectChecker();
        molecularProfileChecker.check(object);

        return ImmutableJsonMolecularProfile.builder()
                .id(Json.integer(object, "id"))
                .profileName(Json.string(object, "profileName"))
                .geneVariants(extractGeneVariants(object.getAsJsonArray("geneVariants")))
                .treatmentApproaches(extractProfileTreatmentApproaches(object.getAsJsonArray("profileTreatmentApproaches")))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .updateDate(DateConverter.toDate(Json.string(object, "updateDate")))
                .complexMolecularProfileEvidence(extractComplexMolecularProfileEvidence(object.getAsJsonObject(
                        "complexMolecularProfileEvidence")))
                .treatmentApproachEvidence(extractTreatmentApproachEvidence(object.getAsJsonObject("treatmentApproachEvidence")))
                .variantAssociatedClinicalTrials(extractVariantAssociatedClinicalTrials(object.getAsJsonArray(
                        "variantAssociatedClinicalTrials")))
                .variantLevelEvidence(extractVariantLevelEvidence(object.getAsJsonObject("variantLevelEvidence")))
                .extendedEvidence(extractExtendedEvidence(object.getAsJsonObject("extendedEvidence")))
                .build();
    }

    @NotNull
    private static List<VariantInfo> extractGeneVariants(@NotNull JsonArray jsonArray) {
        List<VariantInfo> geneVariants = Lists.newArrayList();
        JsonDatamodelChecker geneVariantChecker = MolecularProfileDatamodelChecker.geneVariantObjectChecker();

        for (JsonElement geneVariant : jsonArray) {
            JsonObject geneVariantJsonObject = geneVariant.getAsJsonObject();
            geneVariantChecker.check(geneVariantJsonObject);

            geneVariants.add(ImmutableVariantInfo.builder()
                    .id(Json.integer(geneVariantJsonObject, "id"))
                    .fullName(Json.string(geneVariantJsonObject, "fullName"))
                    .impact(Json.nullableString(geneVariantJsonObject, "impact"))
                    .proteinEffect(Json.nullableString(geneVariantJsonObject, "proteinEffect"))
                    .build());
        }
        return geneVariants;
    }

    @NotNull
    private static List<TreatmentApproachInfo> extractProfileTreatmentApproaches(@NotNull JsonArray jsonArray) {
        List<TreatmentApproachInfo> profileTreatmentApproaches = Lists.newArrayList();
        JsonDatamodelChecker profileTreatmentApproachChecker = MolecularProfileDatamodelChecker.profileTreatmentApproachObjectChecker();

        for (JsonElement profileTreatmentApproach : jsonArray) {
            JsonObject profileTreatmentApproachJsonObject = profileTreatmentApproach.getAsJsonObject();
            profileTreatmentApproachChecker.check(profileTreatmentApproachJsonObject);

            profileTreatmentApproaches.add(ImmutableTreatmentApproachInfo.builder()
                    .id(Json.integer(profileTreatmentApproachJsonObject, "id"))
                    .name(Json.string(profileTreatmentApproachJsonObject, "name"))
                    .profileName(Json.string(profileTreatmentApproachJsonObject, "profileName"))
                    .build());
        }
        return profileTreatmentApproaches;
    }

    @NotNull
    private static JsonMolecularProfileExtendedEvidence extractComplexMolecularProfileEvidence(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker complexMolecularProfileEvidenceChecker =
                MolecularProfileDatamodelChecker.complexMolecularProfileEvidenceChecker();
        complexMolecularProfileEvidenceChecker.check(jsonObject);

        return ImmutableJsonMolecularProfileExtendedEvidence.builder()
                .totalCount(Json.integer(jsonObject, "totalCount"))
                .evidences(extractComplexMolecularProfileEvidenceList(jsonObject.getAsJsonArray("complexMolecularProfileEvidence")))
                .build();
    }

    @NotNull
    private static List<EvidenceInfo> extractComplexMolecularProfileEvidenceList(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> complexMolecularProfileEvidenceList = Lists.newArrayList();
        JsonDatamodelChecker complexMolecularProfileEvidenceListChecker =
                MolecularProfileDatamodelChecker.complexMolecularProfileEvidenceListChecker();

        for (JsonElement complexMolecularProfileEvidence : jsonArray) {
            JsonObject complexMolecularProfileEvidenceJsonObject = complexMolecularProfileEvidence.getAsJsonObject();
            complexMolecularProfileEvidenceListChecker.check(complexMolecularProfileEvidenceJsonObject);

            complexMolecularProfileEvidenceList.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(complexMolecularProfileEvidenceJsonObject, "id"))
                    .approvalStatus(Json.string(complexMolecularProfileEvidenceJsonObject, "approvalStatus"))
                    .evidenceType(Json.string(complexMolecularProfileEvidenceJsonObject, "evidenceType"))
                    .efficacyEvidence(Json.string(complexMolecularProfileEvidenceJsonObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfile(complexMolecularProfileEvidenceJsonObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapy(complexMolecularProfileEvidenceJsonObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(complexMolecularProfileEvidenceJsonObject.getAsJsonObject("indication")))
                    .responseType(Json.string(complexMolecularProfileEvidenceJsonObject, "responseType"))
                    .references(extractReferences(complexMolecularProfileEvidenceJsonObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(complexMolecularProfileEvidenceJsonObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(complexMolecularProfileEvidenceJsonObject, "ampCapAscoInferredTier"))
                    .treatmentApproaches(extractRelevantTreatmentApproaches(complexMolecularProfileEvidenceJsonObject.getAsJsonArray(
                            "relevantTreatmentApproaches")))
                    .build());
        }
        return complexMolecularProfileEvidenceList;
    }

    @NotNull
    private static MolecularProfileInfo extractMolecularProfile(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker molecularProfileChecker = MolecularProfileDatamodelChecker.molecularProfileChecker();
        molecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker therapyChecker = MolecularProfileDatamodelChecker.therapyChecker();
        therapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndication(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker indicationChecker = MolecularProfileDatamodelChecker.indicationChecker();
        indicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static List<ReferenceInfo> extractReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker referenceChecker = MolecularProfileDatamodelChecker.referenceChecker();

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
    private static List<TreatmentApproachInfo> extractRelevantTreatmentApproaches(@NotNull JsonArray jsonArray) {
        List<TreatmentApproachInfo> relevantTreatmentApproaches = Lists.newArrayList();
        JsonDatamodelChecker relevantTreatmentApproachChecker = MolecularProfileDatamodelChecker.relevantTreatmentApproachChecker();

        for (JsonElement relevantTreatmentApproach : jsonArray) {
            JsonObject relevantTreatmentApproachJsonObject = relevantTreatmentApproach.getAsJsonObject();
            relevantTreatmentApproachChecker.check(relevantTreatmentApproachJsonObject);

            relevantTreatmentApproaches.add(ImmutableTreatmentApproachInfo.builder()
                    .id(Json.integer(relevantTreatmentApproachJsonObject, "id"))
                    .name(Json.string(relevantTreatmentApproachJsonObject, "name"))
                    .profileName(Json.string(relevantTreatmentApproachJsonObject, "profileName"))
                    .build());
        }
        return relevantTreatmentApproaches;
    }

    @NotNull
    private static JsonMolecularProfileExtendedEvidence extractTreatmentApproachEvidence(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker treatmentApproachEvidenceChecker = MolecularProfileDatamodelChecker.treatmentApproachEvidenceChecker();
        treatmentApproachEvidenceChecker.check(jsonObject);

        return ImmutableJsonMolecularProfileExtendedEvidence.builder()
                .totalCount(Json.integer(jsonObject, "totalCount"))
                .evidences(extractTreatmentApproachEvidenceList(jsonObject.getAsJsonArray("treatmentApproachEvidence")))
                .build();
    }

    @NotNull
    private static List<EvidenceInfo> extractTreatmentApproachEvidenceList(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> treatmentApproachEvidenceList = Lists.newArrayList();
        JsonDatamodelChecker treatmentApproachEvidenceListChecker = MolecularProfileDatamodelChecker.treatmentApproachEvidenceListChecker();

        for (JsonElement treatmentApproachEvidence : jsonArray) {
            JsonObject treatmentApproachEvidenceJsonObject = treatmentApproachEvidence.getAsJsonObject();
            treatmentApproachEvidenceListChecker.check(treatmentApproachEvidenceJsonObject);

            treatmentApproachEvidenceList.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(treatmentApproachEvidenceJsonObject, "id"))
                    .approvalStatus(Json.string(treatmentApproachEvidenceJsonObject, "approvalStatus"))
                    .evidenceType(Json.string(treatmentApproachEvidenceJsonObject, "evidenceType"))
                    .efficacyEvidence(Json.string(treatmentApproachEvidenceJsonObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfile(treatmentApproachEvidenceJsonObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapy(treatmentApproachEvidenceJsonObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(treatmentApproachEvidenceJsonObject.getAsJsonObject("indication")))
                    .responseType(Json.string(treatmentApproachEvidenceJsonObject, "responseType"))
                    .references(extractReferences(treatmentApproachEvidenceJsonObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(treatmentApproachEvidenceJsonObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(treatmentApproachEvidenceJsonObject, "ampCapAscoInferredTier"))
                    .treatmentApproaches(extractRelevantTreatmentApproaches(treatmentApproachEvidenceJsonObject.getAsJsonArray(
                            "relevantTreatmentApproaches")))
                    .build());
        }
        return treatmentApproachEvidenceList;
    }

    @NotNull
    private static List<ClinicalTrialInfo> extractVariantAssociatedClinicalTrials(@NotNull JsonArray jsonArray) {
        List<ClinicalTrialInfo> variantAssociatedClinicalTrials = Lists.newArrayList();
        JsonDatamodelChecker variantAssociatedClinicalTrialChecker =
                MolecularProfileDatamodelChecker.variantAssociatedClinicalTrialChecker();

        for (JsonElement variantAssociatedClinicalTrial : jsonArray) {
            JsonObject variantAssociatedClinicalTrialJsonObject = variantAssociatedClinicalTrial.getAsJsonObject();
            variantAssociatedClinicalTrialChecker.check(variantAssociatedClinicalTrialJsonObject);

            variantAssociatedClinicalTrials.add(ImmutableClinicalTrialInfo.builder()
                    .nctId(Json.string(variantAssociatedClinicalTrialJsonObject, "nctId"))
                    .title(Json.string(variantAssociatedClinicalTrialJsonObject, "title"))
                    .phase(Json.nullableString(variantAssociatedClinicalTrialJsonObject, "phase"))
                    .recruitment(Json.string(variantAssociatedClinicalTrialJsonObject, "recruitment"))
                    .therapies(extractTherapyList(variantAssociatedClinicalTrialJsonObject.getAsJsonArray("therapies")))
                    .build());
        }
        return variantAssociatedClinicalTrials;
    }

    @NotNull
    private static List<TherapyInfo> extractTherapyList(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker therapyChecker = MolecularProfileDatamodelChecker.therapyChecker();

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
    private static JsonMolecularProfileExtendedEvidence extractVariantLevelEvidence(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker variantLevelEvidenceChecker = MolecularProfileDatamodelChecker.variantLevelEvidenceChecker();
        variantLevelEvidenceChecker.check(jsonObject);

        return ImmutableJsonMolecularProfileExtendedEvidence.builder()
                .totalCount(Json.integer(jsonObject, "totalCount"))
                .evidences(extractVariantLevelEvidenceList(jsonObject.getAsJsonArray("variantLevelEvidences")))
                .build();
    }

    @NotNull
    private static List<EvidenceInfo> extractVariantLevelEvidenceList(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> variantLevelEvidenceList = Lists.newArrayList();
        JsonDatamodelChecker variantLevelEvidenceListChecker = MolecularProfileDatamodelChecker.variantLevelEvidenceListChecker();

        for (JsonElement variantLevelEvidence : jsonArray) {
            JsonObject variantLevelEvidenceJsonObject = variantLevelEvidence.getAsJsonObject();
            variantLevelEvidenceListChecker.check(variantLevelEvidenceJsonObject);

            variantLevelEvidenceList.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(variantLevelEvidenceJsonObject, "id"))
                    .approvalStatus(Json.string(variantLevelEvidenceJsonObject, "approvalStatus"))
                    .evidenceType(Json.string(variantLevelEvidenceJsonObject, "evidenceType"))
                    .efficacyEvidence(Json.string(variantLevelEvidenceJsonObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfile(variantLevelEvidenceJsonObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapy(variantLevelEvidenceJsonObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(variantLevelEvidenceJsonObject.getAsJsonObject("indication")))
                    .responseType(Json.string(variantLevelEvidenceJsonObject, "responseType"))
                    .references(extractReferences(variantLevelEvidenceJsonObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(variantLevelEvidenceJsonObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(variantLevelEvidenceJsonObject, "ampCapAscoInferredTier"))
                    .treatmentApproaches(extractRelevantTreatmentApproaches(variantLevelEvidenceJsonObject.getAsJsonArray(
                            "relevantTreatmentApproaches")))
                    .build());
        }
        return variantLevelEvidenceList;
    }

    @NotNull
    private static JsonMolecularProfileExtendedEvidence extractExtendedEvidence(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker extendedEvidenceChecker = MolecularProfileDatamodelChecker.extendedEvidenceChecker();
        extendedEvidenceChecker.check(jsonObject);

        return ImmutableJsonMolecularProfileExtendedEvidence.builder()
                .totalCount(Json.integer(jsonObject, "totalCount"))
                .evidences(extractExtendedEvidenceList(jsonObject.getAsJsonArray("extendedEvidence")))
                .build();
    }

    @NotNull
    private static List<EvidenceInfo> extractExtendedEvidenceList(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> extendedEvidenceList = Lists.newArrayList();
        JsonDatamodelChecker extendedEvidenceListChecker = MolecularProfileDatamodelChecker.extendedEvidenceListChecker();

        for (JsonElement extendedEvidence : jsonArray) {
            JsonObject extendedEvidenceJsonObject = extendedEvidence.getAsJsonObject();
            extendedEvidenceListChecker.check(extendedEvidenceJsonObject);

            extendedEvidenceList.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(extendedEvidenceJsonObject, "id"))
                    .approvalStatus(Json.string(extendedEvidenceJsonObject, "approvalStatus"))
                    .evidenceType(Json.string(extendedEvidenceJsonObject, "evidenceType"))
                    .efficacyEvidence(Json.string(extendedEvidenceJsonObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfile(extendedEvidenceJsonObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapy(extendedEvidenceJsonObject.getAsJsonObject("therapy")))
                    .indication(extractIndication(extendedEvidenceJsonObject.getAsJsonObject("indication")))
                    .responseType(Json.string(extendedEvidenceJsonObject, "responseType"))
                    .references(extractReferences(extendedEvidenceJsonObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(extendedEvidenceJsonObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(extendedEvidenceJsonObject, "ampCapAscoInferredTier"))

                    .build());
        }
        return extendedEvidenceList;
    }
}
