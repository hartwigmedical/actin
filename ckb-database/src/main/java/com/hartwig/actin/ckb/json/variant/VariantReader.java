package com.hartwig.actin.ckb.json.variant;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.DescriptionInfo;
import com.hartwig.actin.ckb.json.common.EvidenceInfo;
import com.hartwig.actin.ckb.json.common.GeneInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDescriptionInfo;
import com.hartwig.actin.ckb.json.common.ImmutableEvidenceInfo;
import com.hartwig.actin.ckb.json.common.ImmutableGeneInfo;
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

public class VariantReader extends CkbJsonDirectoryReader<JsonVariant> {

    public VariantReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonVariant read(@NotNull final JsonObject object) {
        JsonDatamodelChecker variantObjectChecker = VariantDatamodelChecker.variantObjectChecker();
        variantObjectChecker.check(object);

        return ImmutableJsonVariant.builder()
                .id(Json.integer(object, "id"))
                .fullName(Json.string(object, "fullName"))
                .impact(Json.nullableString(object, "impact"))
                .proteinEffect(Json.nullableString(object, "proteinEffect"))
                .descriptions(extractGeneDescriptions(object.getAsJsonArray("geneVariantDescriptions")))
                .type(Json.nullableString(object, "type"))
                .gene(extractGene(object.getAsJsonObject("gene")))
                .variant(Json.string(object, "variant"))
                .createDate(DateConverter.toDate(Json.string(object, "createDate")))
                .updateDate(DateConverter.toDate(Json.string(object, "updateDate")))
                .referenceTranscriptCoordinate(extractReferenceTranscriptCoordinate(Json.nullableObject(object,
                        "referenceTranscriptCoordinates")))
                .partnerGenes(extractPartnerGenes(object.getAsJsonArray("partnerGenes")))
                .categoryVariantPaths(extractCategoryVariantPaths(object.getAsJsonArray("categoryVariantPaths")))
                .evidence(extractEvidence(object.getAsJsonArray("evidence")))
                .extendedEvidence(extractExtendedEvidence(object.getAsJsonArray("extendedEvidence")))
                .molecularProfiles(extractMolecularProfiles(object.getAsJsonArray("molecularProfiles")))
                .allTranscriptCoordinates(extractAllTranscriptCoordinates(object.getAsJsonArray("allTranscriptCoordinates")))
                .memberVariants(extractMemberVariants(object.getAsJsonArray("memberVariants")))
                .build();
    }

    @NotNull
    private static List<DescriptionInfo> extractGeneDescriptions(@NotNull JsonArray jsonArray) {
        List<DescriptionInfo> geneVariantDescriptions = Lists.newArrayList();
        JsonDatamodelChecker geneVariantDescriptionObjectChecker = VariantDatamodelChecker.geneVariantDescriptionObjectChecker();

        for (JsonElement geneVariantDescription : jsonArray) {
            JsonObject geneVariantDescriptionJsonObject = geneVariantDescription.getAsJsonObject();
            geneVariantDescriptionObjectChecker.check(geneVariantDescriptionJsonObject);

            geneVariantDescriptions.add(ImmutableDescriptionInfo.builder()
                    .description(Json.string(geneVariantDescriptionJsonObject, "description"))
                    .references(extractReferences(geneVariantDescriptionJsonObject.getAsJsonArray("references")))
                    .build());
        }
        return geneVariantDescriptions;
    }

    @NotNull
    private static List<ReferenceInfo> extractReferences(@NotNull JsonArray jsonArray) {
        List<ReferenceInfo> references = Lists.newArrayList();
        JsonDatamodelChecker referenceObjectChecker = VariantDatamodelChecker.referenceObjectChecker();

        for (JsonElement reference : jsonArray) {
            JsonObject referenceJsonObject = reference.getAsJsonObject();
            referenceObjectChecker.check(referenceJsonObject);

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
    private static GeneInfo extractGene(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker geneObjectChecker = VariantDatamodelChecker.geneObjectChecker();
        geneObjectChecker.check(jsonObject);

        return ImmutableGeneInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .geneSymbol(Json.string(jsonObject, "geneSymbol"))
                .terms(Json.stringList(jsonObject, "terms"))
                .build();
    }

    @Nullable
    private static JsonTranscriptCoordinate extractReferenceTranscriptCoordinate(@Nullable JsonObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        JsonDatamodelChecker referenceTranscriptCoordinateObjectChecker =
                VariantDatamodelChecker.referenceTranscriptCoordinateObjectChecker();
        referenceTranscriptCoordinateObjectChecker.check(jsonObject);

        return ImmutableJsonTranscriptCoordinate.builder()
                .id(Json.integer(jsonObject, "id"))
                .transcript(Json.string(jsonObject, "transcript"))
                .gDNA(Json.string(jsonObject, "gDna"))
                .cDNA(Json.string(jsonObject, "cDna"))
                .protein(Json.string(jsonObject, "protein"))
                .sourceDB(Json.string(jsonObject, "sourceDb"))
                .refGenomeBuild(Json.string(jsonObject, "refGenomeBuild"))
                .build();
    }

    @NotNull
    private static List<JsonVariantPartnerGene> extractPartnerGenes(@NotNull JsonArray jsonArray) {
        List<JsonVariantPartnerGene> partnerGenes = Lists.newArrayList();
        JsonDatamodelChecker partnerGeneObjectChecker = VariantDatamodelChecker.partnerGeneObjectChecker();

        for (JsonElement partnerGene : jsonArray) {
            JsonObject partnerGenePathJsonObject = partnerGene.getAsJsonObject();
            partnerGeneObjectChecker.check(partnerGenePathJsonObject);

            partnerGenes.add(ImmutableJsonVariantPartnerGene.builder()
                    .gene(extractGene(partnerGenePathJsonObject.getAsJsonObject("gene")))
                    .build());
        }

        return partnerGenes;
    }

    @NotNull
    private static List<JsonCategoryVariantPath> extractCategoryVariantPaths(@NotNull JsonArray jsonArray) {
        List<JsonCategoryVariantPath> categoryVariantPaths = Lists.newArrayList();
        JsonDatamodelChecker categoryVariantPathObjectChecker = VariantDatamodelChecker.categoryVariantPathObjectChecker();

        for (JsonElement categoryVariantPath : jsonArray) {
            JsonObject categoryVariantPathJsonObject = categoryVariantPath.getAsJsonObject();
            categoryVariantPathObjectChecker.check(categoryVariantPathJsonObject);

            categoryVariantPaths.add(ImmutableJsonCategoryVariantPath.builder()
                    .variantPath(Json.string(categoryVariantPathJsonObject, "variantPath"))
                    .variants(extractVariants(categoryVariantPathJsonObject.getAsJsonArray("variants")))
                    .build());
        }

        return categoryVariantPaths;
    }

    @NotNull
    private static List<VariantInfo> extractVariants(@NotNull JsonArray jsonArray) {
        List<VariantInfo> variants = Lists.newArrayList();
        JsonDatamodelChecker variantObjectChecker = VariantDatamodelChecker.variantVariantObjectChecker();

        for (JsonElement variant : jsonArray) {
            JsonObject variantJsonObject = variant.getAsJsonObject();
            variantObjectChecker.check(variantJsonObject);

            variants.add(ImmutableVariantInfo.builder()
                    .id(Json.integer(variantJsonObject, "id"))
                    .fullName(Json.string(variantJsonObject, "fullName"))
                    .impact(Json.string(variantJsonObject, "impact"))
                    .proteinEffect(Json.string(variantJsonObject, "proteinEffect"))
                    .build());
        }
        return variants;
    }

    @NotNull
    private static List<EvidenceInfo> extractEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> evidences = Lists.newArrayList();
        JsonDatamodelChecker evidenceChecker = VariantDatamodelChecker.evidenceObjectChecker();

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
        JsonDatamodelChecker molecularProfileChecker = VariantDatamodelChecker.molecularProfileObjectChecker();
        molecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker therapyChecker = VariantDatamodelChecker.therapyChecker();
        therapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndication(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker indicationChecker = VariantDatamodelChecker.indicationChecker();
        indicationChecker.check(jsonObject);

        return ImmutableIndicationInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .name(Json.string(jsonObject, "name"))
                .source(Json.string(jsonObject, "source"))
                .build();
    }

    @NotNull
    private static List<EvidenceInfo> extractExtendedEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> extendedEvidences = Lists.newArrayList();
        JsonDatamodelChecker extendedEvidenceChecker = VariantDatamodelChecker.extendedEvidenceObjectChecker();

        for (JsonElement extendedEvidence : jsonArray) {
            JsonObject extendedEvidenceJsonObject = extendedEvidence.getAsJsonObject();
            extendedEvidenceChecker.check(extendedEvidenceJsonObject);

            extendedEvidences.add(ImmutableEvidenceInfo.builder()
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
        return extendedEvidences;
    }

    @NotNull
    private static List<MolecularProfileInfo> extractMolecularProfiles(@NotNull JsonArray jsonArray) {
        List<MolecularProfileInfo> molecularProfiles = Lists.newArrayList();
        JsonDatamodelChecker molecularProfileChecker = VariantDatamodelChecker.molecularProfileObjectChecker();

        for (JsonElement molecularProfile : jsonArray) {
            JsonObject molecularProfileJsonObject = molecularProfile.getAsJsonObject();
            molecularProfileChecker.check(molecularProfileJsonObject);
            molecularProfiles.add(ImmutableMolecularProfileInfo.builder()
                    .id(Json.integer(molecularProfileJsonObject, "id"))
                    .profileName(Json.string(molecularProfileJsonObject, "profileName"))
                    .treatmentApproaches(extractProfileTreatmentApproaches(molecularProfileJsonObject.getAsJsonArray(
                            "profileTreatmentApproaches")))
                    .build());
        }

        return molecularProfiles;
    }

    @NotNull
    private static List<TreatmentApproachInfo> extractProfileTreatmentApproaches(@NotNull JsonArray jsonArray) {
        List<TreatmentApproachInfo> profileTreatmentApproaches = Lists.newArrayList();
        JsonDatamodelChecker profileTreatmentApproachChecker = VariantDatamodelChecker.profileTreatmentApproachObjectChecker();

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
    private static List<JsonTranscriptCoordinate> extractAllTranscriptCoordinates(@NotNull JsonArray jsonArray) {
        List<JsonTranscriptCoordinate> allTranscriptCoordinates = Lists.newArrayList();
        JsonDatamodelChecker allTranscriptCoordinateChecker = VariantDatamodelChecker.allTranscriptCoordinateObjectChecker();

        for (JsonElement allTranscriptCoordinate : jsonArray) {
            JsonObject allTranscriptCoordinatesJsonObject = allTranscriptCoordinate.getAsJsonObject();
            allTranscriptCoordinateChecker.check(allTranscriptCoordinatesJsonObject);

            allTranscriptCoordinates.add(ImmutableJsonTranscriptCoordinate.builder()
                    .id(Json.integer(allTranscriptCoordinatesJsonObject, "id"))
                    .transcript(Json.string(allTranscriptCoordinatesJsonObject, "transcript"))
                    .gDNA(Json.string(allTranscriptCoordinatesJsonObject, "gDna"))
                    .cDNA(Json.string(allTranscriptCoordinatesJsonObject, "cDna"))
                    .protein(Json.string(allTranscriptCoordinatesJsonObject, "protein"))
                    .sourceDB(Json.string(allTranscriptCoordinatesJsonObject, "sourceDb"))
                    .refGenomeBuild(Json.string(allTranscriptCoordinatesJsonObject, "refGenomeBuild"))
                    .build());
        }

        return allTranscriptCoordinates;
    }

    @NotNull
    private static List<VariantInfo> extractMemberVariants(@NotNull JsonArray jsonArray) {
        List<VariantInfo> memberVariants = Lists.newArrayList();
        JsonDatamodelChecker memberVariantChecker = VariantDatamodelChecker.memberVariantObjectChecker();

        for (JsonElement memberVariant : jsonArray) {
            JsonObject memberVariantObject = memberVariant.getAsJsonObject();
            memberVariantChecker.check(memberVariantObject);

            memberVariants.add(ImmutableVariantInfo.builder()
                    .id(Json.integer(memberVariantObject, "id"))
                    .fullName(Json.string(memberVariantObject, "fullName"))
                    .impact(Json.string(memberVariantObject, "impact"))
                    .proteinEffect(Json.string(memberVariantObject, "proteinEffect"))
                    .descriptions(extractGeneDescriptions(memberVariantObject.getAsJsonArray("geneVariantDescriptions")))
                    .build());
        }
        return memberVariants;
    }
}
