package com.hartwig.actin.ckb.json.gene;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.ClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.DescriptionInfo;
import com.hartwig.actin.ckb.json.common.EvidenceInfo;
import com.hartwig.actin.ckb.json.common.ImmutableClinicalTrialInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDescriptionInfo;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneReader extends CkbJsonDirectoryReader<JsonGene> {
    private static final Logger LOGGER = LogManager.getLogger(GeneReader.class);

    public GeneReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonGene read(@NotNull final JsonObject object) {
        JsonDatamodelChecker geneChecker = GeneDatamodelChecker.geneObjectChecker();
        geneChecker.check(object);

        return ImmutableJsonGene.builder()
                .id(Json.integer(object, "id"))
                .geneSymbol(Json.string(object, "geneSymbol"))
                .terms(Json.stringList(object, "terms"))
                .entrezId(Json.nullableString(object, "entrezId"))
                .synonyms(Json.stringList(object, "synonyms"))
                .chromosome(Json.nullableString(object, "chromosome"))
                .mapLocation(Json.nullableString(object, "mapLocation"))
                .descriptions(extractDescriptions(object.getAsJsonArray("geneDescriptions")))
                .canonicalTranscript(Json.nullableString(object, "canonicalTranscript"))
                .geneRole(Json.string(object, "geneRole"))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .updateDate(DateConverter.toDate(Json.nullableString(object, "updateDate")))
                .clinicalTrials(extractClinicalTrials(object.getAsJsonArray("clinicalTrials")))
                .evidence(extractEvidence(object.getAsJsonArray("evidence")))
                .variants(extractVariants(object.getAsJsonArray("variants")))
                .molecularProfiles(extractMolecularProfiles(object.getAsJsonArray("molecularProfiles")))
                .categoryVariants(extractCategoryVariants(object.getAsJsonArray("categoryVariants")))
                .build();
    }

    @NotNull
    private static List<DescriptionInfo> extractDescriptions(@NotNull JsonArray jsonArray) {
        List<DescriptionInfo> descriptions = Lists.newArrayList();
        JsonDatamodelChecker descriptionChecker = GeneDatamodelChecker.descriptionObjectChecker();

        for (JsonElement description : jsonArray) {
            JsonObject descriptionJsonObject = description.getAsJsonObject();
            descriptionChecker.check(descriptionJsonObject);

            descriptions.add(ImmutableDescriptionInfo.builder()
                    .description(Json.string(descriptionJsonObject, "description"))
                    .references(extractReferences(descriptionJsonObject.getAsJsonArray("references")))
                    .build());
        }
        return descriptions;
    }

    @NotNull
    private static List<ReferenceInfo> extractReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker referenceChecker = GeneDatamodelChecker.referenceObjectChecker();

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
    private static List<ClinicalTrialInfo> extractClinicalTrials(@NotNull JsonArray jsonArray) {
        List<ClinicalTrialInfo> clinicalTrials = Lists.newArrayList();
        JsonDatamodelChecker clinicalTrialChecker = GeneDatamodelChecker.clinicalTrialObjectChecker();

        for (JsonElement clinicalTrial : jsonArray) {
            JsonObject clinicalTrialJsonObject = clinicalTrial.getAsJsonObject();
            clinicalTrialChecker.check(clinicalTrialJsonObject);

            clinicalTrials.add(ImmutableClinicalTrialInfo.builder()
                    .nctId(Json.string(clinicalTrialJsonObject, "nctId"))
                    .title(Json.string(clinicalTrialJsonObject, "title"))
                    .phase(Json.nullableString(clinicalTrialJsonObject, "phase"))
                    .recruitment(Json.string(clinicalTrialJsonObject, "recruitment"))
                    .therapies(extractTherapies(clinicalTrialJsonObject.getAsJsonArray("therapies")))
                    .build());
        }
        return clinicalTrials;
    }

    @NotNull
    private static List<TherapyInfo> extractTherapies(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker therapyChecker = GeneDatamodelChecker.therapyObjectChecker();

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
    private static List<EvidenceInfo> extractEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> evidences = Lists.newArrayList();
        JsonDatamodelChecker evidenceChecker = GeneDatamodelChecker.evidenceObjectChecker();

        for (JsonElement evidence : jsonArray) {
            JsonObject evidenceObject = evidence.getAsJsonObject();
            evidenceChecker.check(evidenceObject);

            evidences.add(ImmutableEvidenceInfo.builder()
                    .id(Json.integer(evidenceObject, "id"))
                    .approvalStatus(Json.string(evidenceObject, "approvalStatus"))
                    .evidenceType(Json.string(evidenceObject, "evidenceType"))
                    .efficacyEvidence(Json.string(evidenceObject, "efficacyEvidence"))
                    .molecularProfile(extractMolecularProfileObject(evidenceObject.getAsJsonObject("molecularProfile")))
                    .therapy(extractTherapyObject(evidenceObject.getAsJsonObject("therapy")))
                    .indication(extractIndicationObject(evidenceObject.getAsJsonObject("indication")))
                    .responseType(Json.string(evidenceObject, "responseType"))
                    .references(extractReferences(evidenceObject.getAsJsonArray("references")))
                    .ampCapAscoEvidenceLevel(Json.string(evidenceObject, "ampCapAscoEvidenceLevel"))
                    .ampCapAscoInferredTier(Json.string(evidenceObject, "ampCapAscoInferredTier"))
                    .build());
        }
        return evidences;
    }

    @NotNull
    private static MolecularProfileInfo extractMolecularProfileObject(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker molecularProfileChecker = GeneDatamodelChecker.molecularProfileObjectChecker();
        molecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapyObject(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker therapyChecker = GeneDatamodelChecker.therapyObjectChecker();
        therapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndicationObject(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker indicationChecker = GeneDatamodelChecker.indicationObjectChecker();
        indicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static List<VariantInfo> extractVariants(@NotNull JsonArray jsonArray) {
        List<VariantInfo> variants = Lists.newArrayList();
        JsonDatamodelChecker variantChecker = GeneDatamodelChecker.variantObjectChecker();

        for (JsonElement variant : jsonArray) {
            JsonObject variantObject = variant.getAsJsonObject();
            variantChecker.check(variantObject);

            variants.add(ImmutableVariantInfo.builder()
                    .id(Json.integer(variantObject, "id"))
                    .fullName(Json.string(variantObject, "fullName"))
                    .impact(Json.nullableString(variantObject, "impact"))
                    .proteinEffect(Json.nullableString(variantObject, "proteinEffect"))
                    .descriptions(extractVariantDescriptions(variantObject.getAsJsonArray("geneVariantDescriptions")))
                    .build());
        }
        return variants;
    }

    @NotNull
    private static List<DescriptionInfo> extractVariantDescriptions(@NotNull JsonArray jsonArray) {
        List<DescriptionInfo> variantDescriptions = Lists.newArrayList();
        JsonDatamodelChecker variantDescriptionChecker = GeneDatamodelChecker.variantDescriptionObjectChecker();

        for (JsonElement variantDescription : jsonArray) {
            JsonObject variantDescriptionObject = variantDescription.getAsJsonObject();
            variantDescriptionChecker.check(variantDescriptionObject);

            variantDescriptions.add(ImmutableDescriptionInfo.builder()
                    .description(Json.string(variantDescriptionObject, "description"))
                    .references(extractReferences(variantDescriptionObject.getAsJsonArray("references")))
                    .build());
        }
        return variantDescriptions;
    }

    @NotNull
    private static List<MolecularProfileInfo> extractMolecularProfiles(@NotNull JsonArray jsonArray) {
        List<MolecularProfileInfo> molecularProfiles = Lists.newArrayList();
        JsonDatamodelChecker molecularProfileChecker = GeneDatamodelChecker.molecularProfileObjectChecker();

        for (JsonElement molecularProfile : jsonArray) {
            JsonObject molecularProfileObject = molecularProfile.getAsJsonObject();
            molecularProfileChecker.check(molecularProfileObject);

            molecularProfiles.add(ImmutableMolecularProfileInfo.builder()
                    .id(Json.integer(molecularProfileObject, "id"))
                    .profileName(Json.string(molecularProfileObject, "profileName"))
                    .treatmentApproaches(extractProfileTreatmentApproaches(molecularProfileObject.getAsJsonArray(
                            "profileTreatmentApproaches")))
                    .build());
        }
        return molecularProfiles;
    }

    @NotNull
    private static List<TreatmentApproachInfo> extractProfileTreatmentApproaches(@NotNull JsonArray jsonArray) {
        List<TreatmentApproachInfo> profileTreatmentApproaches = Lists.newArrayList();
        JsonDatamodelChecker profileTreatmentApproachChecker = GeneDatamodelChecker.profileTreatmentApproachObjectChecker();

        for (JsonElement profileTreatmentApproach : jsonArray) {
            JsonObject profileTreatmentApproachObject = profileTreatmentApproach.getAsJsonObject();
            profileTreatmentApproachChecker.check(profileTreatmentApproachObject);

            profileTreatmentApproaches.add(ImmutableTreatmentApproachInfo.builder()
                    .id(Json.integer(profileTreatmentApproachObject, "id"))
                    .name(Json.string(profileTreatmentApproachObject, "name"))
                    .profileName(Json.string(profileTreatmentApproachObject, "profileName"))
                    .build());
        }
        return profileTreatmentApproaches;
    }

    @NotNull
    private static List<VariantInfo> extractCategoryVariants(@NotNull JsonArray jsonArray) {
        List<VariantInfo> categoryVariants = Lists.newArrayList();
        JsonDatamodelChecker categoryVariantObjectChecker = GeneDatamodelChecker.categoryVariantObjectChecker();

        for (JsonElement categoryVariant : jsonArray) {
            JsonObject categoryVariantObject = categoryVariant.getAsJsonObject();
            categoryVariantObjectChecker.check(categoryVariantObject);

            categoryVariants.add(ImmutableVariantInfo.builder()
                    .id(Json.integer(categoryVariantObject, "id"))
                    .fullName(Json.string(categoryVariantObject, "fullName"))
                    .impact(Json.nullableString(categoryVariantObject, "impact"))
                    .proteinEffect(Json.nullableString(categoryVariantObject, "proteinEffect"))
                    .descriptions(extractVariantDescriptions(categoryVariantObject.getAsJsonArray("geneVariantDescriptions")))
                    .build());
        }
        return categoryVariants;
    }
}
