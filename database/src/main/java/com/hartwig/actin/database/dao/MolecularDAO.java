package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.COPYNUMBER;
import static com.hartwig.actin.database.Tables.COPYNUMBEREVIDENCE;
import static com.hartwig.actin.database.Tables.DISRUPTION;
import static com.hartwig.actin.database.Tables.DISRUPTIONEVIDENCE;
import static com.hartwig.actin.database.Tables.FUSION;
import static com.hartwig.actin.database.Tables.FUSIONEVIDENCE;
import static com.hartwig.actin.database.Tables.HLAALLELE;
import static com.hartwig.actin.database.Tables.HOMOLOGOUSREPAIREVIDENCE;
import static com.hartwig.actin.database.Tables.HOMOZYGOUSDISRUPTION;
import static com.hartwig.actin.database.Tables.HOMOZYGOUSDISRUPTIONEVIDENCE;
import static com.hartwig.actin.database.Tables.MICROSATELLITEEVIDENCE;
import static com.hartwig.actin.database.Tables.MOLECULAR;
import static com.hartwig.actin.database.Tables.PHARMACO;
import static com.hartwig.actin.database.Tables.TUMORMUTATIONALBURDENEVIDENCE;
import static com.hartwig.actin.database.Tables.TUMORMUTATIONALLOADEVIDENCE;
import static com.hartwig.actin.database.Tables.VARIANT;
import static com.hartwig.actin.database.Tables.VARIANTEVIDENCE;
import static com.hartwig.actin.database.Tables.VARIANTOTHERIMPACT;
import static com.hartwig.actin.database.Tables.VIRUS;
import static com.hartwig.actin.database.Tables.VIRUSEVIDENCE;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.database.tables.records.CopynumberevidenceRecord;
import com.hartwig.actin.database.tables.records.DisruptionevidenceRecord;
import com.hartwig.actin.database.tables.records.FusionevidenceRecord;
import com.hartwig.actin.database.tables.records.HomologousrepairevidenceRecord;
import com.hartwig.actin.database.tables.records.HomozygousdisruptionevidenceRecord;
import com.hartwig.actin.database.tables.records.MicrosatelliteevidenceRecord;
import com.hartwig.actin.database.tables.records.TumormutationalburdenevidenceRecord;
import com.hartwig.actin.database.tables.records.TumormutationalloadevidenceRecord;
import com.hartwig.actin.database.tables.records.VariantevidenceRecord;
import com.hartwig.actin.database.tables.records.VirusevidenceRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;

class MolecularDAO {

    @NotNull
    private final DSLContext context;

    public MolecularDAO(@NotNull final DSLContext context) {
        this.context = context;
    }

    public void clear(@NotNull MolecularRecord record) {
        String sampleId = record.sampleId();

        Result<Record1<Integer>> molecularResults =
                context.select(MOLECULAR.ID).from(MOLECULAR).where(MOLECULAR.SAMPLEID.eq(sampleId)).fetch();
        for (Record molecularResult : molecularResults) {
            int molecularId = molecularResult.getValue(MOLECULAR.ID);
            context.delete(MICROSATELLITEEVIDENCE).where(MICROSATELLITEEVIDENCE.MOLECULARID.eq(molecularId)).execute();
            context.delete(HOMOLOGOUSREPAIREVIDENCE).where(HOMOLOGOUSREPAIREVIDENCE.MOLECULARID.eq(molecularId)).execute();
            context.delete(TUMORMUTATIONALBURDENEVIDENCE).where(TUMORMUTATIONALBURDENEVIDENCE.MOLECULARID.eq(molecularId)).execute();
            context.delete(TUMORMUTATIONALLOADEVIDENCE).where(TUMORMUTATIONALLOADEVIDENCE.MOLECULARID.eq(molecularId)).execute();
        }

        Result<Record1<Integer>> variantResults = context.select(VARIANT.ID).from(VARIANT).where(VARIANT.SAMPLEID.eq(sampleId)).fetch();
        for (Record variantResult : variantResults) {
            int variantId = variantResult.getValue(VARIANT.ID);
            context.delete(VARIANTOTHERIMPACT).where(VARIANTOTHERIMPACT.VARIANTID.eq(variantId)).execute();
            context.delete(VARIANTEVIDENCE).where(VARIANTEVIDENCE.VARIANTID.eq(variantId)).execute();
        }

        context.delete(VARIANT).where(VARIANT.SAMPLEID.eq(sampleId)).execute();

        Result<Record1<Integer>> copyNumberResults =
                context.select(COPYNUMBER.ID).from(COPYNUMBER).where(COPYNUMBER.SAMPLEID.eq(sampleId)).fetch();
        for (Record copyNumberResult : copyNumberResults) {
            int copyNumberId = copyNumberResult.getValue(COPYNUMBER.ID);
            context.delete(COPYNUMBEREVIDENCE).where(COPYNUMBEREVIDENCE.COPYNUMBERID.eq(copyNumberId)).execute();
        }

        context.delete(COPYNUMBER).where(COPYNUMBER.SAMPLEID.eq(sampleId)).execute();

        Result<Record1<Integer>> homozygousDisruptionResults = context.select(HOMOZYGOUSDISRUPTION.ID)
                .from(HOMOZYGOUSDISRUPTION)
                .where(HOMOZYGOUSDISRUPTION.SAMPLEID.eq(sampleId))
                .fetch();
        for (Record homozygousDisruptionResult : homozygousDisruptionResults) {
            int homozygousDisruptionId = homozygousDisruptionResult.getValue(HOMOZYGOUSDISRUPTION.ID);
            context.delete(HOMOZYGOUSDISRUPTIONEVIDENCE)
                    .where(HOMOZYGOUSDISRUPTIONEVIDENCE.HOMOZYGOUSDISRUPTIONID.eq(homozygousDisruptionId))
                    .execute();
        }

        context.delete(HOMOZYGOUSDISRUPTION).where(HOMOZYGOUSDISRUPTION.SAMPLEID.eq(sampleId)).execute();

        Result<Record1<Integer>> disruptionResults =
                context.select(DISRUPTION.ID).from(DISRUPTION).where(DISRUPTION.SAMPLEID.eq(sampleId)).fetch();
        for (Record disruptionResult : disruptionResults) {
            int disruptionId = disruptionResult.getValue(DISRUPTION.ID);
            context.delete(DISRUPTIONEVIDENCE).where(DISRUPTIONEVIDENCE.DISRUPTIONID.eq(disruptionId)).execute();
        }

        context.delete(DISRUPTION).where(DISRUPTION.SAMPLEID.eq(sampleId)).execute();

        Result<Record1<Integer>> fusionResults = context.select(FUSION.ID).from(FUSION).where(FUSION.SAMPLEID.eq(sampleId)).fetch();
        for (Record fusionResult : fusionResults) {
            int fusionId = fusionResult.getValue(FUSION.ID);
            context.delete(FUSIONEVIDENCE).where(FUSIONEVIDENCE.FUSIONID.eq(fusionId)).execute();
        }

        context.delete(FUSION).where(FUSION.SAMPLEID.eq(sampleId)).execute();

        Result<Record1<Integer>> virusResults = context.select(VIRUS.ID).from(VIRUS).where(VIRUS.SAMPLEID.eq(sampleId)).fetch();
        for (Record virusResult : virusResults) {
            int virusId = virusResult.getValue(VIRUS.ID);
            context.delete(VIRUSEVIDENCE).where(VIRUSEVIDENCE.VIRUSID.eq(virusId)).execute();
        }

        context.delete(VIRUS).where(VIRUS.SAMPLEID.eq(sampleId)).execute();

        context.delete(HLAALLELE).where(HLAALLELE.SAMPLEID.eq(sampleId)).execute();
        context.delete(PHARMACO).where(PHARMACO.SAMPLEID.eq(sampleId)).execute();

        context.delete(MOLECULAR).where(MOLECULAR.SAMPLEID.eq(sampleId)).execute();
    }

    public void writeMolecularRecord(@NotNull MolecularRecord record) {
        writeMolecularDetails(record);

        String sampleId = record.sampleId();
        MolecularDrivers drivers = record.drivers();
        writeVariants(sampleId, drivers.variants());
        writeCopyNumbers(sampleId, drivers.copyNumbers());
        writeHomozygousDisruptions(sampleId, drivers.homozygousDisruptions());
        writeDisruptions(sampleId, drivers.disruptions());
        writeFusions(sampleId, drivers.fusions());
        writeViruses(sampleId, drivers.viruses());

        writeImmunology(sampleId, record.immunology());

        writePharmaco(sampleId, record.pharmaco());
    }

    private void writeMolecularDetails(@NotNull MolecularRecord record) {
        String sampleId = record.sampleId();
        PredictedTumorOrigin predictedTumorOrigin = record.characteristics().predictedTumorOrigin();

        int molecularId = context.insertInto(MOLECULAR,
                        MOLECULAR.PATIENTID,
                        MOLECULAR.SAMPLEID,
                        MOLECULAR.EXPERIMENTTYPE,
                        MOLECULAR.REFGENOMEVERSION,
                        MOLECULAR.EXPERIMENTDATE,
                        MOLECULAR.EVIDENCESOURCE,
                        MOLECULAR.EXTERNALTRIALSOURCE,
                        MOLECULAR.CONTAINSTUMORCELLS,
                        MOLECULAR.HASSUFFICIENTQUALITY,
                        MOLECULAR.PURITY,
                        MOLECULAR.PLOIDY,
                        MOLECULAR.PREDICTEDTUMORTYPE,
                        MOLECULAR.PREDICTEDTUMORLIKELIHOOD,
                        MOLECULAR.ISMICROSATELLITEUNSTABLE,
                        MOLECULAR.ISHOMOLOGOUSREPAIRDEFICIENT,
                        MOLECULAR.TUMORMUTATIONALBURDEN,
                        MOLECULAR.HASHIGHTUMORMUTATIONALBURDEN,
                        MOLECULAR.TUMORMUTATIONALLOAD,
                        MOLECULAR.HASHIGHTUMORMUTATIONALLOAD)
                .values(record.patientId(),
                        sampleId,
                        record.type().toString(),
                        record.refGenomeVersion().toString(),
                        record.date(),
                        record.evidenceSource(),
                        record.externalTrialSource(),
                        DataUtil.toByte(record.containsTumorCells()),
                        DataUtil.toByte(record.hasSufficientQualityAndPurity()),
                        record.characteristics().purity(),
                        record.characteristics().ploidy(),
                        predictedTumorOrigin != null ? predictedTumorOrigin.tumorType() : null,
                        predictedTumorOrigin != null ? predictedTumorOrigin.likelihood() : null,
                        DataUtil.toByte(record.characteristics().isMicrosatelliteUnstable()),
                        DataUtil.toByte(record.characteristics().isHomologousRepairDeficient()),
                        record.characteristics().tumorMutationalBurden(),
                        DataUtil.toByte(record.characteristics().hasHighTumorMutationalBurden()),
                        record.characteristics().tumorMutationalLoad(),
                        DataUtil.toByte(record.characteristics().hasHighTumorMutationalLoad()))
                .returning(MOLECULAR.ID)
                .fetchOne()
                .getValue(MOLECULAR.ID);

        writeMicrosatelliteEvidence(molecularId, record.characteristics().microsatelliteEvidence());
        writeHomologousRepairEvidence(molecularId, record.characteristics().homologousRepairEvidence());
        writeTumorMutationalBurdenEvidence(molecularId, record.characteristics().tumorMutationalBurdenEvidence());
        writeTumorMutationalLoadEvidence(molecularId, record.characteristics().tumorMutationalLoadEvidence());
    }

    private void writeMicrosatelliteEvidence(int molecularId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        EvidenceInserter<MicrosatelliteevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(MICROSATELLITEEVIDENCE,
                MICROSATELLITEEVIDENCE.MOLECULARID,
                MICROSATELLITEEVIDENCE.TREATMENT,
                MICROSATELLITEEVIDENCE.TYPE));

        writeEvidence(inserter, molecularId, evidence);
        inserter.execute();
    }

    private void writeHomologousRepairEvidence(int molecularId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        EvidenceInserter<HomologousrepairevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(HOMOLOGOUSREPAIREVIDENCE,
                HOMOLOGOUSREPAIREVIDENCE.MOLECULARID,
                HOMOLOGOUSREPAIREVIDENCE.TREATMENT,
                HOMOLOGOUSREPAIREVIDENCE.TYPE));

        writeEvidence(inserter, molecularId, evidence);
        inserter.execute();
    }

    private void writeTumorMutationalBurdenEvidence(int molecularId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        EvidenceInserter<TumormutationalburdenevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(
                TUMORMUTATIONALBURDENEVIDENCE,
                TUMORMUTATIONALBURDENEVIDENCE.MOLECULARID,
                TUMORMUTATIONALBURDENEVIDENCE.TREATMENT,
                TUMORMUTATIONALBURDENEVIDENCE.TYPE));

        writeEvidence(inserter, molecularId, evidence);
        inserter.execute();
    }

    private void writeTumorMutationalLoadEvidence(int molecularId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        EvidenceInserter<TumormutationalloadevidenceRecord> inserter =
                new EvidenceInserter<>(context.insertInto(TUMORMUTATIONALLOADEVIDENCE,
                        TUMORMUTATIONALLOADEVIDENCE.MOLECULARID,
                        TUMORMUTATIONALLOADEVIDENCE.TREATMENT,
                        TUMORMUTATIONALLOADEVIDENCE.TYPE));

        writeEvidence(inserter, molecularId, evidence);
        inserter.execute();
    }

    private void writeVariants(@NotNull String sampleId, @NotNull Set<Variant> variants) {
        for (Variant variant : variants) {
            int variantId = context.insertInto(VARIANT,
                            VARIANT.SAMPLEID,
                            VARIANT.ISREPORTABLE,
                            VARIANT.EVENT,
                            VARIANT.DRIVERLIKELIHOOD,
                            VARIANT.GENE,
                            VARIANT.GENEROLE,
                            VARIANT.PROTEINEFFECT,
                            VARIANT.ISASSOCIATEDWITHDRUGRESISTANCE,
                            VARIANT.TYPE,
                            VARIANT.VARIANTCOPYNUMBER,
                            VARIANT.TOTALCOPYNUMBER,
                            VARIANT.ISBIALLELIC,
                            VARIANT.ISHOTSPOT,
                            VARIANT.CLONALLIKELIHOOD,
                            VARIANT.PHASEGROUPS,
                            VARIANT.CANONICALTRANSCRIPTID,
                            VARIANT.CANONICALHGVSCODINGIMPACT,
                            VARIANT.CANONICALHGVSPROTEINIMPACT,
                            VARIANT.CANONICALAFFECTEDCODON,
                            VARIANT.CANONICALAFFECTEDEXON,
                            VARIANT.CANONICALISSPLICEREGION,
                            VARIANT.CANONICALEFFECTS,
                            VARIANT.CANONICALCODINGEFFECT)
                    .values(sampleId,
                            DataUtil.toByte(variant.isReportable()),
                            variant.event(),
                            driverLikelihood(variant),
                            variant.gene(),
                            variant.geneRole().toString(),
                            variant.proteinEffect().toString(),
                            DataUtil.toByte(variant.isAssociatedWithDrugResistance()),
                            variant.type().toString(),
                            variant.variantCopyNumber(),
                            variant.totalCopyNumber(),
                            DataUtil.toByte(variant.isBiallelic()),
                            DataUtil.toByte(variant.isHotspot()),
                            variant.clonalLikelihood(),
                            DataUtil.concat(integersToStrings(variant.phaseGroups())),
                            variant.canonicalImpact().transcriptId(),
                            variant.canonicalImpact().hgvsCodingImpact(),
                            variant.canonicalImpact().hgvsProteinImpact(),
                            variant.canonicalImpact().affectedCodon(),
                            variant.canonicalImpact().affectedExon(),
                            DataUtil.toByte(variant.canonicalImpact().isSpliceRegion()),
                            DataUtil.concat(effectsToStrings(variant.canonicalImpact().effects())),
                            DataUtil.nullableToString(variant.canonicalImpact().codingEffect()))
                    .returning(VARIANT.ID)
                    .fetchOne()
                    .getValue(VARIANT.ID);
            writeVariantEvidence(variantId, variant.evidence());
        }
    }

    @NotNull
    private static Set<String> effectsToStrings(@NotNull Set<VariantEffect> effects) {
        Set<String> strings = Sets.newHashSet();
        for (VariantEffect effect : effects) {
            strings.add(effect.toString());
        }
        return strings;
    }

    private void writeVariantEvidence(int variantId, @NotNull ActionableEvidence evidence) {
        EvidenceInserter<VariantevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(VARIANTEVIDENCE,
                VARIANTEVIDENCE.VARIANTID,
                VARIANTEVIDENCE.TREATMENT,
                VARIANTEVIDENCE.TYPE));

        writeEvidence(inserter, variantId, evidence);
        inserter.execute();
    }

    private void writeCopyNumbers(@NotNull String sampleId, @NotNull Set<CopyNumber> copyNumbers) {
        for (CopyNumber copyNumber : copyNumbers) {
            int copyNumberId = context.insertInto(COPYNUMBER,
                            COPYNUMBER.SAMPLEID,
                            COPYNUMBER.ISREPORTABLE,
                            COPYNUMBER.EVENT,
                            COPYNUMBER.DRIVERLIKELIHOOD,
                            COPYNUMBER.GENE,
                            COPYNUMBER.GENEROLE,
                            COPYNUMBER.PROTEINEFFECT,
                            COPYNUMBER.ISASSOCIATEDWITHDRUGRESISTANCE,
                            COPYNUMBER.TYPE,
                            COPYNUMBER.MINCOPIES,
                            COPYNUMBER.MAXCOPIES)
                    .values(sampleId,
                            DataUtil.toByte(copyNumber.isReportable()),
                            copyNumber.event(),
                            driverLikelihood(copyNumber),
                            copyNumber.gene(),
                            copyNumber.geneRole().toString(),
                            copyNumber.proteinEffect().toString(),
                            DataUtil.toByte(copyNumber.isAssociatedWithDrugResistance()),
                            copyNumber.type().toString(),
                            copyNumber.minCopies(),
                            copyNumber.maxCopies())
                    .returning(COPYNUMBER.ID)
                    .fetchOne()
                    .getValue(COPYNUMBER.ID);
            writeCopyNumberEvidence(copyNumberId, copyNumber.evidence());
        }
    }

    private void writeCopyNumberEvidence(int copyNumberId, @NotNull ActionableEvidence evidence) {
        EvidenceInserter<CopynumberevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(COPYNUMBEREVIDENCE,
                COPYNUMBEREVIDENCE.COPYNUMBERID,
                COPYNUMBEREVIDENCE.TREATMENT,
                COPYNUMBEREVIDENCE.TYPE));

        writeEvidence(inserter, copyNumberId, evidence);
        inserter.execute();
    }

    private void writeHomozygousDisruptions(@NotNull String sampleId, @NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        for (HomozygousDisruption homozygousDisruption : homozygousDisruptions) {
            int homozygousDisruptionId = context.insertInto(HOMOZYGOUSDISRUPTION,
                            HOMOZYGOUSDISRUPTION.SAMPLEID,
                            HOMOZYGOUSDISRUPTION.ISREPORTABLE,
                            HOMOZYGOUSDISRUPTION.EVENT,
                            HOMOZYGOUSDISRUPTION.DRIVERLIKELIHOOD,
                            HOMOZYGOUSDISRUPTION.GENE,
                            HOMOZYGOUSDISRUPTION.GENEROLE,
                            HOMOZYGOUSDISRUPTION.PROTEINEFFECT,
                            HOMOZYGOUSDISRUPTION.ISASSOCIATEDWITHDRUGRESISTANCE)
                    .values(sampleId,
                            DataUtil.toByte(homozygousDisruption.isReportable()),
                            homozygousDisruption.event(),
                            driverLikelihood(homozygousDisruption),
                            homozygousDisruption.gene(),
                            homozygousDisruption.geneRole().toString(),
                            homozygousDisruption.proteinEffect().toString(),
                            DataUtil.toByte(homozygousDisruption.isAssociatedWithDrugResistance()))
                    .returning(HOMOZYGOUSDISRUPTION.ID)
                    .fetchOne()
                    .getValue(HOMOZYGOUSDISRUPTION.ID);
            writeHomozygousDisruptionEvidence(homozygousDisruptionId, homozygousDisruption.evidence());
        }
    }

    private void writeHomozygousDisruptionEvidence(int homozygousDisruptionId, @NotNull ActionableEvidence evidence) {
        EvidenceInserter<HomozygousdisruptionevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(
                HOMOZYGOUSDISRUPTIONEVIDENCE,
                HOMOZYGOUSDISRUPTIONEVIDENCE.HOMOZYGOUSDISRUPTIONID,
                HOMOZYGOUSDISRUPTIONEVIDENCE.TREATMENT,
                HOMOZYGOUSDISRUPTIONEVIDENCE.TYPE));

        writeEvidence(inserter, homozygousDisruptionId, evidence);
        inserter.execute();
    }

    private void writeDisruptions(@NotNull String sampleId, @NotNull Set<Disruption> disruptions) {
        for (Disruption disruption : disruptions) {
            int disruptionId = context.insertInto(DISRUPTION,
                            DISRUPTION.SAMPLEID,
                            DISRUPTION.ISREPORTABLE,
                            DISRUPTION.EVENT,
                            DISRUPTION.DRIVERLIKELIHOOD,
                            DISRUPTION.GENE,
                            DISRUPTION.GENEROLE,
                            DISRUPTION.PROTEINEFFECT,
                            DISRUPTION.ISASSOCIATEDWITHDRUGRESISTANCE,
                            DISRUPTION.TYPE,
                            DISRUPTION.JUNCTIONCOPYNUMBER,
                            DISRUPTION.UNDISRUPTEDCOPYNUMBER,
                            DISRUPTION.REGIONTYPE,
                            DISRUPTION.CODINGCONTEXT,
                            DISRUPTION.CLUSTERGROUP)
                    .values(sampleId,
                            DataUtil.toByte(disruption.isReportable()),
                            disruption.event(),
                            driverLikelihood(disruption),
                            disruption.gene(),
                            disruption.geneRole().toString(),
                            disruption.proteinEffect().toString(),
                            DataUtil.toByte(disruption.isAssociatedWithDrugResistance()),
                            disruption.type().toString(),
                            disruption.junctionCopyNumber(),
                            disruption.undisruptedCopyNumber(),
                            disruption.regionType().toString(),
                            disruption.codingContext().toString(),
                            disruption.clusterGroup())
                    .returning(DISRUPTION.ID)
                    .fetchOne()
                    .getValue(DISRUPTION.ID);
            writeDisruptionEvidence(disruptionId, disruption.evidence());
        }
    }

    private void writeDisruptionEvidence(int disruptionId, @NotNull ActionableEvidence evidence) {
        EvidenceInserter<DisruptionevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(DISRUPTIONEVIDENCE,
                DISRUPTIONEVIDENCE.DISRUPTIONID,
                DISRUPTIONEVIDENCE.TREATMENT,
                DISRUPTIONEVIDENCE.TYPE));

        writeEvidence(inserter, disruptionId, evidence);
        inserter.execute();
    }

    private void writeFusions(@NotNull String sampleId, @NotNull Set<Fusion> fusions) {
        for (Fusion fusion : fusions) {
            int fusionId = context.insertInto(FUSION,
                            FUSION.SAMPLEID,
                            FUSION.ISREPORTABLE,
                            FUSION.EVENT,
                            FUSION.DRIVERLIKELIHOOD,
                            FUSION.GENESTART,
                            FUSION.GENETRANSCRIPTSTART,
                            FUSION.FUSEDEXONUP,
                            FUSION.GENEEND,
                            FUSION.GENETRANSCRIPTEND,
                            FUSION.FUSEDEXONDOWN,
                            FUSION.DRIVERTYPE,
                            FUSION.PROTEINEFFECT,
                            FUSION.ISASSOCIATEDWITHDRUGRESISTANCE)
                    .values(sampleId,
                            DataUtil.toByte(fusion.isReportable()),
                            fusion.event(),
                            driverLikelihood(fusion),
                            fusion.geneStart(),
                            fusion.geneTranscriptStart(),
                            fusion.fusedExonUp(),
                            fusion.geneEnd(),
                            fusion.geneTranscriptEnd(),
                            fusion.fusedExonDown(),
                            fusion.driverType().toString(),
                            fusion.proteinEffect().toString(),
                            DataUtil.toByte(fusion.isAssociatedWithDrugResistance()))
                    .returning(FUSION.ID)
                    .fetchOne()
                    .getValue(FUSION.ID);
            writeFusionEvidence(fusionId, fusion.evidence());
        }
    }

    private void writeFusionEvidence(int fusionId, @NotNull ActionableEvidence evidence) {
        EvidenceInserter<FusionevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(FUSIONEVIDENCE,
                FUSIONEVIDENCE.FUSIONID,
                FUSIONEVIDENCE.TREATMENT,
                FUSIONEVIDENCE.TYPE));

        writeEvidence(inserter, fusionId, evidence);
        inserter.execute();
    }

    private void writeViruses(@NotNull String sampleId, @NotNull Set<Virus> viruses) {
        for (Virus virus : viruses) {
            int virusId = context.insertInto(VIRUS,
                            VIRUS.SAMPLEID,
                            VIRUS.ISREPORTABLE,
                            VIRUS.EVENT,
                            VIRUS.DRIVERLIKELIHOOD,
                            VIRUS.NAME,
                            VIRUS.TYPE,
                            VIRUS.ISRELIABLE,
                            VIRUS.INTEGRATIONS)
                    .values(sampleId,
                            DataUtil.toByte(virus.isReportable()),
                            virus.event(),
                            driverLikelihood(virus),
                            virus.name(),
                            virus.type().toString(),
                            DataUtil.toByte(virus.isReliable()),
                            virus.integrations())
                    .returning(VIRUS.ID)
                    .fetchOne()
                    .getValue(VIRUS.ID);
            writeVirusEvidence(virusId, virus.evidence());
        }
    }

    private void writeVirusEvidence(int virusId,@NotNull ActionableEvidence evidence) {
        EvidenceInserter<VirusevidenceRecord> inserter = new EvidenceInserter<>(context.insertInto(VIRUSEVIDENCE,
                VIRUSEVIDENCE.VIRUSID,
                VIRUSEVIDENCE.TREATMENT,
                VIRUSEVIDENCE.TYPE));

        writeEvidence(inserter, virusId, evidence);
        inserter.execute();
    }

    @Nullable
    private static String driverLikelihood(@NotNull Driver driver) {
        return driver.driverLikelihood() != null ? driver.driverLikelihood().toString() : null;
    }

    @Nullable
    private static Set<String> integersToStrings(@Nullable Set<Integer> integers) {
        if (integers == null) {
            return null;
        }

        Set<String> strings = Sets.newHashSet();
        for (Integer integer : integers) {
            strings.add(String.valueOf(integer));
        }
        return strings;
    }

    private void writeImmunology(@NotNull String sampleId, @NotNull MolecularImmunology immunology) {
        for (HlaAllele hlaAllele : immunology.hlaAlleles()) {
            context.insertInto(HLAALLELE,
                            HLAALLELE.SAMPLEID,
                            HLAALLELE.ISRELIABLE,
                            HLAALLELE.NAME,
                            HLAALLELE.TUMORCOPYNUMBER,
                            HLAALLELE.HASSOMATICMUTATIONS)
                    .values(sampleId,
                            DataUtil.toByte(immunology.isReliable()),
                            hlaAllele.name(),
                            hlaAllele.tumorCopyNumber(),
                            DataUtil.toByte(hlaAllele.hasSomaticMutations()))
                    .execute();
        }
    }

    private void writePharmaco(@NotNull String sampleId, @NotNull Set<PharmacoEntry> pharmaco) {
        for (PharmacoEntry entry : pharmaco) {
            for (Haplotype haplotype : entry.haplotypes()) {
                context.insertInto(PHARMACO, PHARMACO.SAMPLEID, PHARMACO.GENE, PHARMACO.HAPLOTYPE, PHARMACO.HAPLOTYPEFUNCTION)
                        .values(sampleId, entry.gene(), haplotype.name(), haplotype.function())
                        .execute();
            }
        }
    }

    private static <T extends Record> void writeEvidence(@NotNull EvidenceInserter<T> inserter, int topicId,
            @NotNull ActionableEvidence evidence) {
        writeTreatments(inserter, topicId, evidence.approvedTreatments(), "Approved");
        writeTreatments(inserter, topicId, evidence.externalEligibleTrials(), "Trial");
        writeTreatments(inserter, topicId, evidence.onLabelExperimentalTreatments(), "On-label experimental");
        writeTreatments(inserter, topicId, evidence.offLabelExperimentalTreatments(), "Off-label experimental");
        writeTreatments(inserter, topicId, evidence.preClinicalTreatments(), "Pre-clinical");
        writeTreatments(inserter, topicId, evidence.knownResistantTreatments(), "Known resistant");
        writeTreatments(inserter, topicId, evidence.suspectResistantTreatments(), "Suspect resistant");
    }

    private static <T extends Record> void writeTreatments(@NotNull EvidenceInserter<T> inserter, int topicId,
            @NotNull Set<String> treatments, @NotNull String type) {
        for (String treatment : treatments) {
            inserter.write(topicId, treatment, type);
        }
    }
}
