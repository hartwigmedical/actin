package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.AMPLIFICATION;
import static com.hartwig.actin.database.Tables.AMPLIFICATIONEVIDENCE;
import static com.hartwig.actin.database.Tables.DISRUPTION;
import static com.hartwig.actin.database.Tables.DISRUPTIONEVIDENCE;
import static com.hartwig.actin.database.Tables.FUSION;
import static com.hartwig.actin.database.Tables.FUSIONEVIDENCE;
import static com.hartwig.actin.database.Tables.HLAALLELE;
import static com.hartwig.actin.database.Tables.HOMOLOGOUSREPAIREVIDENCE;
import static com.hartwig.actin.database.Tables.HOMOZYGOUSDISRUPTION;
import static com.hartwig.actin.database.Tables.HOMOZYGOUSDISRUPTIONEVIDENCE;
import static com.hartwig.actin.database.Tables.LOSS;
import static com.hartwig.actin.database.Tables.LOSSEVIDENCE;
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
import com.hartwig.actin.database.tables.records.AmplificationevidenceRecord;
import com.hartwig.actin.database.tables.records.DisruptionevidenceRecord;
import com.hartwig.actin.database.tables.records.FusionevidenceRecord;
import com.hartwig.actin.database.tables.records.HomologousrepairevidenceRecord;
import com.hartwig.actin.database.tables.records.HomozygousdisruptionevidenceRecord;
import com.hartwig.actin.database.tables.records.LossevidenceRecord;
import com.hartwig.actin.database.tables.records.MicrosatelliteevidenceRecord;
import com.hartwig.actin.database.tables.records.TumormutationalburdenevidenceRecord;
import com.hartwig.actin.database.tables.records.TumormutationalloadevidenceRecord;
import com.hartwig.actin.database.tables.records.VariantevidenceRecord;
import com.hartwig.actin.database.tables.records.VirusevidenceRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
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

        context.delete(MICROSATELLITEEVIDENCE).where(MICROSATELLITEEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(HOMOLOGOUSREPAIREVIDENCE).where(HOMOLOGOUSREPAIREVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(TUMORMUTATIONALBURDENEVIDENCE).where(TUMORMUTATIONALBURDENEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(TUMORMUTATIONALLOADEVIDENCE).where(TUMORMUTATIONALLOADEVIDENCE.SAMPLEID.eq(sampleId)).execute();

        Result<Record1<Integer>> variantResults = context.select(VARIANT.ID).from(VARIANT).where(VARIANT.SAMPLEID.eq(sampleId)).fetch();
        for (Record variantResult : variantResults) {
            int variantId = variantResult.getValue(VARIANT.ID);
            context.delete(VARIANTOTHERIMPACT).where(VARIANTOTHERIMPACT.VARIANTID.eq(variantId)).execute();
        }

        context.delete(VARIANTEVIDENCE).where(VARIANTEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(VARIANT).where(VARIANT.SAMPLEID.eq(sampleId)).execute();

        context.delete(AMPLIFICATIONEVIDENCE).where(AMPLIFICATIONEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(AMPLIFICATION).where(AMPLIFICATION.SAMPLEID.eq(sampleId)).execute();

        context.delete(LOSSEVIDENCE).where(LOSSEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(LOSS).where(LOSS.SAMPLEID.eq(sampleId)).execute();

        context.delete(HOMOZYGOUSDISRUPTIONEVIDENCE).where(HOMOZYGOUSDISRUPTIONEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(HOMOZYGOUSDISRUPTION).where(HOMOZYGOUSDISRUPTION.SAMPLEID.eq(sampleId)).execute();

        context.delete(DISRUPTIONEVIDENCE).where(DISRUPTIONEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(DISRUPTION).where(DISRUPTION.SAMPLEID.eq(sampleId)).execute();

        context.delete(FUSIONEVIDENCE).where(FUSIONEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(FUSION).where(FUSION.SAMPLEID.eq(sampleId)).execute();

        context.delete(VIRUSEVIDENCE).where(VIRUSEVIDENCE.SAMPLEID.eq(sampleId)).execute();
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
        writeAmplifications(sampleId, drivers.amplifications());
        writeLosses(sampleId, drivers.losses());
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

        context.insertInto(MOLECULAR,
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
                        DataUtil.toByte(record.hasSufficientQuality()),
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
                .execute();

        writeMicrosatelliteEvidence(sampleId, record.characteristics().microsatelliteEvidence());
        writeHomologousRepairEvidence(sampleId, record.characteristics().homologousRepairEvidence());
        writeTumorMutationalBurdenEvidence(sampleId, record.characteristics().tumorMutationalBurdenEvidence());
        writeTumorMutationalLoadEvidence(sampleId, record.characteristics().tumorMutationalLoadEvidence());
    }

    private void writeMicrosatelliteEvidence(@NotNull String sampleId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        CharacteristicsEvidenceInserter<MicrosatelliteevidenceRecord> inserter = new CharacteristicsEvidenceInserter<>(context.insertInto(
                MICROSATELLITEEVIDENCE,
                MICROSATELLITEEVIDENCE.SAMPLEID,
                MICROSATELLITEEVIDENCE.TREATMENT,
                MICROSATELLITEEVIDENCE.TYPE));

        writeCharacteristicsEvidence(inserter, sampleId, evidence);
        inserter.execute();
    }

    private void writeHomologousRepairEvidence(@NotNull String sampleId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        CharacteristicsEvidenceInserter<HomologousrepairevidenceRecord> inserter = new CharacteristicsEvidenceInserter<>(context.insertInto(
                HOMOLOGOUSREPAIREVIDENCE,
                HOMOLOGOUSREPAIREVIDENCE.SAMPLEID,
                HOMOLOGOUSREPAIREVIDENCE.TREATMENT,
                HOMOLOGOUSREPAIREVIDENCE.TYPE));

        writeCharacteristicsEvidence(inserter, sampleId, evidence);
        inserter.execute();
    }

    private void writeTumorMutationalBurdenEvidence(@NotNull String sampleId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        CharacteristicsEvidenceInserter<TumormutationalburdenevidenceRecord> inserter =
                new CharacteristicsEvidenceInserter<>(context.insertInto(TUMORMUTATIONALBURDENEVIDENCE,
                        TUMORMUTATIONALBURDENEVIDENCE.SAMPLEID,
                        TUMORMUTATIONALBURDENEVIDENCE.TREATMENT,
                        TUMORMUTATIONALBURDENEVIDENCE.TYPE));

        writeCharacteristicsEvidence(inserter, sampleId, evidence);
        inserter.execute();
    }

    private void writeTumorMutationalLoadEvidence(@NotNull String sampleId, @Nullable ActionableEvidence evidence) {
        if (evidence == null) {
            return;
        }

        CharacteristicsEvidenceInserter<TumormutationalloadevidenceRecord> inserter =
                new CharacteristicsEvidenceInserter<>(context.insertInto(TUMORMUTATIONALLOADEVIDENCE,
                        TUMORMUTATIONALLOADEVIDENCE.SAMPLEID,
                        TUMORMUTATIONALLOADEVIDENCE.TREATMENT,
                        TUMORMUTATIONALLOADEVIDENCE.TYPE));

        writeCharacteristicsEvidence(inserter, sampleId, evidence);
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
                            VARIANT.PHASEGROUP,
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
                            variant.driverLikelihood().toString(),
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
                            variant.phaseGroup(),
                            variant.canonicalImpact().transcriptId(),
                            variant.canonicalImpact().hgvsCodingImpact(),
                            variant.canonicalImpact().hgvsProteinImpact(),
                            variant.canonicalImpact().affectedCodon(),
                            variant.canonicalImpact().affectedExon(),
                            DataUtil.toByte(variant.canonicalImpact().isSpliceRegion()),
                            DataUtil.concat(toStrings(variant.canonicalImpact().effects())),
                            DataUtil.nullableToString(variant.canonicalImpact().codingEffect()))
                    .returning(VARIANT.ID)
                    .fetchOne()
                    .getValue(VARIANT.ID);
            writeVariantEvidence(variantId, sampleId, variant.evidence());
        }
    }

    @NotNull
    private static Set<String> toStrings(@NotNull Set<VariantEffect> effects) {
        Set<String> strings = Sets.newHashSet();
        for (VariantEffect effect : effects) {
            strings.add(effect.toString());
        }
        return strings;
    }

    private void writeVariantEvidence(int variantId, @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<VariantevidenceRecord> inserter = new DriverEvidenceInserter<>(context.insertInto(VARIANTEVIDENCE,
                VARIANTEVIDENCE.VARIANTID,
                VARIANTEVIDENCE.SAMPLEID,
                VARIANTEVIDENCE.TREATMENT,
                VARIANTEVIDENCE.TYPE));

        writeDriverEvidence(inserter, variantId, sampleId, evidence);
        inserter.execute();
    }

    private void writeAmplifications(@NotNull String sampleId, @NotNull Set<Amplification> amplifications) {
        for (Amplification amplification : amplifications) {
            int amplificationId = context.insertInto(AMPLIFICATION,
                            AMPLIFICATION.SAMPLEID,
                            AMPLIFICATION.ISREPORTABLE,
                            AMPLIFICATION.EVENT,
                            AMPLIFICATION.DRIVERLIKELIHOOD,
                            AMPLIFICATION.GENE,
                            AMPLIFICATION.GENEROLE,
                            AMPLIFICATION.PROTEINEFFECT,
                            AMPLIFICATION.ISASSOCIATEDWITHDRUGRESISTANCE,
                            AMPLIFICATION.MINCOPIES,
                            AMPLIFICATION.MAXCOPIES,
                            AMPLIFICATION.ISPARTIAL)
                    .values(sampleId,
                            DataUtil.toByte(amplification.isReportable()),
                            amplification.event(),
                            amplification.driverLikelihood().toString(),
                            amplification.gene(),
                            amplification.geneRole().toString(),
                            amplification.proteinEffect().toString(),
                            DataUtil.toByte(amplification.isAssociatedWithDrugResistance()),
                            amplification.minCopies(),
                            amplification.maxCopies(),
                            DataUtil.toByte(amplification.isPartial()))
                    .returning(AMPLIFICATION.ID)
                    .fetchOne()
                    .getValue(AMPLIFICATION.ID);
            writeAmplificationEvidence(amplificationId, sampleId, amplification.evidence());
        }
    }

    private void writeAmplificationEvidence(int amplificationId, @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<AmplificationevidenceRecord> inserter =
                new DriverEvidenceInserter<>(context.insertInto(AMPLIFICATIONEVIDENCE,
                        AMPLIFICATIONEVIDENCE.AMPLIFICATIONID,
                        AMPLIFICATIONEVIDENCE.SAMPLEID,
                        AMPLIFICATIONEVIDENCE.TREATMENT,
                        AMPLIFICATIONEVIDENCE.TYPE));

        writeDriverEvidence(inserter, amplificationId, sampleId, evidence);
        inserter.execute();
    }

    private void writeLosses(@NotNull String sampleId, @NotNull Set<Loss> losses) {
        for (Loss loss : losses) {
            int lossId = context.insertInto(LOSS,
                            LOSS.SAMPLEID,
                            LOSS.ISREPORTABLE,
                            LOSS.EVENT,
                            LOSS.DRIVERLIKELIHOOD,
                            LOSS.GENE,
                            LOSS.GENEROLE,
                            LOSS.PROTEINEFFECT,
                            LOSS.ISASSOCIATEDWITHDRUGRESISTANCE,
                            LOSS.MINCOPIES,
                            LOSS.MAXCOPIES,
                            LOSS.ISPARTIAL)
                    .values(sampleId,
                            DataUtil.toByte(loss.isReportable()),
                            loss.event(),
                            loss.driverLikelihood().toString(),
                            loss.gene(),
                            loss.geneRole().toString(),
                            loss.proteinEffect().toString(),
                            DataUtil.toByte(loss.isAssociatedWithDrugResistance()),
                            loss.minCopies(),
                            loss.maxCopies(),
                            DataUtil.toByte(loss.isPartial()))
                    .returning(LOSS.ID)
                    .fetchOne()
                    .getValue(LOSS.ID);
            writeLossEvidence(lossId, sampleId, loss.evidence());
        }
    }

    private void writeLossEvidence(int lossId, @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<LossevidenceRecord> inserter = new DriverEvidenceInserter<>(context.insertInto(LOSSEVIDENCE,
                LOSSEVIDENCE.LOSSID,
                LOSSEVIDENCE.SAMPLEID,
                LOSSEVIDENCE.TREATMENT,
                LOSSEVIDENCE.TYPE));

        writeDriverEvidence(inserter, lossId, sampleId, evidence);
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
                            homozygousDisruption.driverLikelihood().toString(),
                            homozygousDisruption.gene(),
                            homozygousDisruption.geneRole().toString(),
                            homozygousDisruption.proteinEffect().toString(),
                            DataUtil.toByte(homozygousDisruption.isAssociatedWithDrugResistance()))
                    .returning(HOMOZYGOUSDISRUPTION.ID)
                    .fetchOne()
                    .getValue(HOMOZYGOUSDISRUPTION.ID);
            writeHomozygousDisruptionEvidence(homozygousDisruptionId, sampleId, homozygousDisruption.evidence());
        }
    }

    private void writeHomozygousDisruptionEvidence(int homozygousDisruptionId, @NotNull String sampleId,
            @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<HomozygousdisruptionevidenceRecord> inserter = new DriverEvidenceInserter<>(context.insertInto(
                HOMOZYGOUSDISRUPTIONEVIDENCE,
                HOMOZYGOUSDISRUPTIONEVIDENCE.HOMOZYGOUSDISRUPTIONID,
                HOMOZYGOUSDISRUPTIONEVIDENCE.SAMPLEID,
                HOMOZYGOUSDISRUPTIONEVIDENCE.TREATMENT,
                HOMOZYGOUSDISRUPTIONEVIDENCE.TYPE));

        writeDriverEvidence(inserter, homozygousDisruptionId, sampleId, evidence);
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
                            disruption.driverLikelihood().toString(),
                            disruption.gene(),
                            disruption.geneRole().toString(),
                            disruption.proteinEffect().toString(),
                            DataUtil.toByte(disruption.isAssociatedWithDrugResistance()),
                            disruption.type(),
                            disruption.junctionCopyNumber(),
                            disruption.undisruptedCopyNumber(),
                            disruption.regionType().toString(),
                            disruption.codingContext().toString(),
                            disruption.clusterGroup())
                    .returning(DISRUPTION.ID)
                    .fetchOne()
                    .getValue(DISRUPTION.ID);
            writeDisruptionEvidence(disruptionId, sampleId, disruption.evidence());
        }
    }

    private void writeDisruptionEvidence(int disruptionId, @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<DisruptionevidenceRecord> inserter = new DriverEvidenceInserter<>(context.insertInto(DISRUPTIONEVIDENCE,
                DISRUPTIONEVIDENCE.DISRUPTIONID,
                DISRUPTIONEVIDENCE.SAMPLEID,
                DISRUPTIONEVIDENCE.TREATMENT,
                DISRUPTIONEVIDENCE.TYPE));

        writeDriverEvidence(inserter, disruptionId, sampleId, evidence);
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
                            fusion.driverLikelihood().toString(),
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
            writeFusionEvidence(fusionId, sampleId, fusion.evidence());
        }
    }

    private void writeFusionEvidence(int fusionId, @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<FusionevidenceRecord> inserter = new DriverEvidenceInserter<>(context.insertInto(FUSIONEVIDENCE,
                FUSIONEVIDENCE.FUSIONID,
                FUSIONEVIDENCE.SAMPLEID,
                FUSIONEVIDENCE.TREATMENT,
                FUSIONEVIDENCE.TYPE));

        writeDriverEvidence(inserter, fusionId, sampleId, evidence);
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
                            VIRUS.ISRELIABLE,
                            VIRUS.INTERPRETATION,
                            VIRUS.INTEGRATIONS)
                    .values(sampleId,
                            DataUtil.toByte(virus.isReportable()),
                            virus.event(),
                            virus.driverLikelihood().toString(),
                            virus.name(),
                            DataUtil.toByte(virus.isReliable()),
                            virus.interpretation(),
                            virus.integrations())
                    .returning(VIRUS.ID)
                    .fetchOne()
                    .getValue(VIRUS.ID);
            writeVirusEvidence(virusId, sampleId, virus.evidence());
        }
    }

    private void writeVirusEvidence(int virusId, @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        DriverEvidenceInserter<VirusevidenceRecord> inserter = new DriverEvidenceInserter<>(context.insertInto(VIRUSEVIDENCE,
                VIRUSEVIDENCE.VIRUSID,
                VIRUSEVIDENCE.SAMPLEID,
                VIRUSEVIDENCE.TREATMENT,
                VIRUSEVIDENCE.TYPE));

        writeDriverEvidence(inserter, virusId, sampleId, evidence);
        inserter.execute();
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

    private static <T extends Record> void writeCharacteristicsEvidence(@NotNull CharacteristicsEvidenceInserter<T> inserter,
            @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        writeCharacteristicsTreatments(inserter, sampleId, evidence.approvedTreatments(), "Approved");
        writeCharacteristicsTreatments(inserter, sampleId, evidence.externalEligibleTrials(), "Trial");
        writeCharacteristicsTreatments(inserter, sampleId, evidence.onLabelExperimentalTreatments(), "On-label experimental");
        writeCharacteristicsTreatments(inserter, sampleId, evidence.offLabelExperimentalTreatments(), "Off-label experimental");
        writeCharacteristicsTreatments(inserter, sampleId, evidence.preClinicalTreatments(), "Pre-clinical");
        writeCharacteristicsTreatments(inserter, sampleId, evidence.knownResistantTreatments(), "Known resistant");
        writeCharacteristicsTreatments(inserter, sampleId, evidence.suspectResistantTreatments(), "Suspect resistant");
    }

    private static <T extends Record> void writeCharacteristicsTreatments(@NotNull CharacteristicsEvidenceInserter<T> inserter,
            @NotNull String sampleId, @NotNull Set<String> treatments, @NotNull String type) {
        for (String treatment : treatments) {
            inserter.write(sampleId, treatment, type);
        }
    }

    private static <T extends Record> void writeDriverEvidence(@NotNull DriverEvidenceInserter<T> inserter, int topicId,
            @NotNull String sampleId, @NotNull ActionableEvidence evidence) {
        writeDriverTreatments(inserter, topicId, sampleId, evidence.approvedTreatments(), "Approved");
        writeDriverTreatments(inserter, topicId, sampleId, evidence.externalEligibleTrials(), "Trial");
        writeDriverTreatments(inserter, topicId, sampleId, evidence.onLabelExperimentalTreatments(), "On-label experimental");
        writeDriverTreatments(inserter, topicId, sampleId, evidence.offLabelExperimentalTreatments(), "Off-label experimental");
        writeDriverTreatments(inserter, topicId, sampleId, evidence.preClinicalTreatments(), "Pre-clinical");
        writeDriverTreatments(inserter, topicId, sampleId, evidence.knownResistantTreatments(), "Known resistant");
        writeDriverTreatments(inserter, topicId, sampleId, evidence.suspectResistantTreatments(), "Suspect resistant");
    }

    private static <T extends Record> void writeDriverTreatments(@NotNull DriverEvidenceInserter<T> inserter, int topicId,
            @NotNull String sampleId, @NotNull Set<String> treatments, @NotNull String type) {
        for (String treatment : treatments) {
            inserter.write(topicId, sampleId, treatment, type);
        }
    }
}
