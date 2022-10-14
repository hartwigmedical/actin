package com.hartwig.actin.ckb.json.reference;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hartwig.actin.ckb.json.CkbJsonDirectoryReader;
import com.hartwig.actin.ckb.json.common.DrugInfo;
import com.hartwig.actin.ckb.json.common.EvidenceInfo;
import com.hartwig.actin.ckb.json.common.GeneInfo;
import com.hartwig.actin.ckb.json.common.ImmutableDrugInfo;
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
import com.hartwig.actin.util.json.Json;
import com.hartwig.actin.util.json.JsonDatamodelChecker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReferenceReader extends CkbJsonDirectoryReader<JsonReference> {

    public ReferenceReader(@Nullable final Integer maxFilesToRead) {
        super(maxFilesToRead);
    }

    @NotNull
    @Override
    protected JsonReference read(@NotNull final JsonObject object) {
        JsonDatamodelChecker referenceChecker = ReferenceDatamodelChecker.referenceObjectChecker();
        referenceChecker.check(object);

        return ImmutableJsonReference.builder()
                .id(Json.integer(object, "id"))
                .pubMedId(Json.nullableString(object, "pubMedId"))
                .title(Json.nullableString(object, "title"))
                .url(Json.nullableString(object, "url"))
                .authors(Json.nullableString(object, "authors"))
                .journal(Json.nullableString(object, "journal"))
                .volume(Json.nullableString(object, "volume"))
                .issue(Json.nullableString(object, "issue"))
                .date(Json.nullableString(object, "date"))
                .abstractText(Json.nullableString(object, "abstractText"))
                .year(Json.nullableString(object, "year"))
                .drugs(extractDrugs(object.getAsJsonArray("drugs")))
                .genes(extractGenes(object.getAsJsonArray("genes")))
                .evidence(extractEvidence(object.getAsJsonArray("evidence")))
                .therapies(extractTherapies(object.getAsJsonArray("therapies")))
                .treatmentApproaches(extractTreatmentApproaches(object.getAsJsonArray("treatmentApproaches")))
                .variants(extractVariants(object.getAsJsonArray("variants")))
                .build();
    }

    @NotNull
    private static List<DrugInfo> extractDrugs(@NotNull JsonArray jsonArray) {
        List<DrugInfo> referenceDrugs = Lists.newArrayList();
        JsonDatamodelChecker drugChecker = ReferenceDatamodelChecker.drugObjectChecker();

        for (JsonElement drug : jsonArray) {
            JsonObject drugJsonObject = drug.getAsJsonObject();
            drugChecker.check(drugJsonObject);

            referenceDrugs.add(ImmutableDrugInfo.builder()
                    .id(Json.integer(drugJsonObject, "id"))
                    .drugName(Json.string(drugJsonObject, "drugName"))
                    .terms(Json.stringList(drugJsonObject, "terms"))
                    .build());
        }
        return referenceDrugs;
    }

    @NotNull
    private static List<GeneInfo> extractGenes(@NotNull JsonArray jsonArray) {
        List<GeneInfo> genes = Lists.newArrayList();
        JsonDatamodelChecker geneChecker = ReferenceDatamodelChecker.geneObjectChecker();

        for (JsonElement gene : jsonArray) {
            JsonObject geneJsonObject = gene.getAsJsonObject();
            geneChecker.check(geneJsonObject);

            genes.add(ImmutableGeneInfo.builder()
                    .id(Json.integer(geneJsonObject, "id"))
                    .geneSymbol(Json.string(geneJsonObject, "geneSymbol"))
                    .terms(Json.stringList(geneJsonObject, "terms"))
                    .build());
        }
        return genes;
    }

    @NotNull
    private static List<EvidenceInfo> extractEvidence(@NotNull JsonArray jsonArray) {
        List<EvidenceInfo> evidences = Lists.newArrayList();
        JsonDatamodelChecker evidenceChecker = ReferenceDatamodelChecker.evidenceObjectChecker();

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
        JsonDatamodelChecker molecularProfileChecker = ReferenceDatamodelChecker.molecularProfileObjectChecker();
        molecularProfileChecker.check(jsonObject);

        return ImmutableMolecularProfileInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .profileName(Json.string(jsonObject, "profileName"))
                .build();
    }

    @NotNull
    private static TherapyInfo extractTherapy(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker therapyChecker = ReferenceDatamodelChecker.therapyChecker();
        therapyChecker.check(jsonObject);

        return ImmutableTherapyInfo.builder()
                .id(Json.integer(jsonObject, "id"))
                .therapyName(Json.string(jsonObject, "therapyName"))
                .synonyms(Json.nullableStringList(jsonObject, "synonyms"))
                .build();
    }

    @NotNull
    private static IndicationInfo extractIndication(@NotNull JsonObject jsonObject) {
        JsonDatamodelChecker indicationChecker = ReferenceDatamodelChecker.indicationChecker();
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
        JsonDatamodelChecker referenceChecker = ReferenceDatamodelChecker.referenceChecker();

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
    private static List<TherapyInfo> extractTherapies(@NotNull JsonArray jsonArray) {
        List<TherapyInfo> therapies = Lists.newArrayList();
        JsonDatamodelChecker therapyChecker = ReferenceDatamodelChecker.therapyChecker();

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
    private static List<TreatmentApproachInfo> extractTreatmentApproaches(@NotNull JsonArray jsonArray) {
        List<TreatmentApproachInfo> treatmentApproaches = Lists.newArrayList();
        JsonDatamodelChecker treatmentApproachChecker = ReferenceDatamodelChecker.treatmentApproachObjectChecker();

        for (JsonElement treatmentApproach : jsonArray) {
            JsonObject treatmentApproachJsonObject = treatmentApproach.getAsJsonObject();
            treatmentApproachChecker.check(treatmentApproachJsonObject);

            treatmentApproaches.add(ImmutableTreatmentApproachInfo.builder()
                    .id(Json.integer(treatmentApproachJsonObject, "id"))
                    .name(Json.string(treatmentApproachJsonObject, "name"))
                    .profileName(Json.string(treatmentApproachJsonObject, "profileName"))
                    .build());
        }
        return treatmentApproaches;
    }

    @NotNull
    private static List<VariantInfo> extractVariants(@NotNull JsonArray jsonArray) {
        List<VariantInfo> variants = Lists.newArrayList();
        JsonDatamodelChecker variantChecker = ReferenceDatamodelChecker.variantObjectChecker();

        for (JsonElement variant : jsonArray) {
            JsonObject variantJsonObject = variant.getAsJsonObject();
            variantChecker.check(variantJsonObject);

            variants.add(ImmutableVariantInfo.builder()
                    .id(Json.integer(variantJsonObject, "id"))
                    .fullName(Json.string(variantJsonObject, "fullName"))
                    .impact(Json.nullableString(variantJsonObject, "impact"))
                    .proteinEffect(Json.nullableString(variantJsonObject, "proteinEffect"))
                    .build());
        }
        return variants;
    }
}
