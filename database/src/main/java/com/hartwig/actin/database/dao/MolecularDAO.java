package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.AMPLIFICATION;
import static com.hartwig.actin.database.Tables.AMPLIFICATIONEVIDENCE;
import static com.hartwig.actin.database.Tables.DISRUPTION;
import static com.hartwig.actin.database.Tables.DISRUPTIONEVIDENCE;
import static com.hartwig.actin.database.Tables.FUSION;
import static com.hartwig.actin.database.Tables.FUSIONEVIDENCE;
import static com.hartwig.actin.database.Tables.HLAALLELE;
import static com.hartwig.actin.database.Tables.HOMOLOGOUSREPAIRDEFICIENCYEVIDENCE;
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
import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Effect;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.jetbrains.annotations.NotNull;
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
        context.delete(HOMOLOGOUSREPAIRDEFICIENCYEVIDENCE).where(HOMOLOGOUSREPAIRDEFICIENCYEVIDENCE.SAMPLEID.eq(sampleId)).execute();
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
        PredictedTumorOrigin predictedTumorOrigin = record.characteristics().predictedTumorOrigin();

        context.insertInto(MOLECULAR,
                        MOLECULAR.PATIENTID,
                        MOLECULAR.SAMPLEID,
                        MOLECULAR.EXPERIMENTTYPE,
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
                        record.sampleId(),
                        record.type().toString(),
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
    }

    private void writeVariants(@NotNull String sampleId, @NotNull Set<Variant> variants) {
        for (Variant variant : variants) {
            context.insertInto(VARIANT,
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
                            concatTranscriptEffects(variant.canonicalImpact().effects()),
                            DataUtil.nullableToString(variant.canonicalImpact().codingEffect()))
                    .execute();
        }
    }

    @NotNull
    private static String concatTranscriptEffects(@NotNull Iterable<Effect> effects) {
        StringJoiner joiner = new StringJoiner("&");
        for (Effect effect : effects) {
            joiner.add(effect.toString());
        }
        return joiner.toString();
    }

    private void writeAmplifications(@NotNull String sampleId, @NotNull Set<Amplification> amplifications) {
        for (Amplification amplification : amplifications) {
            context.insertInto(AMPLIFICATION,
                            AMPLIFICATION.SAMPLEID,
                            AMPLIFICATION.ISREPORTABLE,
                            AMPLIFICATION.DRIVERLIKELIHOOD,
                            AMPLIFICATION.GENE,
                            AMPLIFICATION.GENEROLE,
                            AMPLIFICATION.PROTEINEFFECT,
                            AMPLIFICATION.ISASSOCIATEDWITHDRUGRESISTANCE,
                            AMPLIFICATION.MINCOPIES,
                            AMPLIFICATION.MAXCOPIES)
                    .values(sampleId,
                            DataUtil.toByte(amplification.isReportable()),
                            amplification.driverLikelihood().toString(),
                            amplification.gene(),
                            amplification.geneRole().toString(),
                            amplification.proteinEffect().toString(),
                            DataUtil.toByte(amplification.isAssociatedWithDrugResistance()),
                            amplification.minCopies(),
                            amplification.maxCopies())
                    .execute();
        }
    }

    private void writeLosses(@NotNull String sampleId, @NotNull Set<Loss> losses) {
        for (Loss loss : losses) {
            context.insertInto(LOSS,
                            LOSS.SAMPLEID,
                            LOSS.ISREPORTABLE,
                            LOSS.EVENT,
                            LOSS.DRIVERLIKELIHOOD,
                            LOSS.GENE,
                            LOSS.GENEROLE,
                            LOSS.PROTEINEFFECT,
                            LOSS.ISASSOCIATEDWITHDRUGRESISTANCE,
                            LOSS.MINCOPIES,
                            LOSS.MAXCOPIES)
                    .values(sampleId,
                            DataUtil.toByte(loss.isReportable()),
                            loss.event(),
                            loss.driverLikelihood().toString(),
                            loss.gene(),
                            loss.geneRole().toString(),
                            loss.proteinEffect().toString(),
                            DataUtil.toByte(loss.isAssociatedWithDrugResistance()),
                            loss.minCopies(),
                            loss.maxCopies())
                    .execute();
        }
    }

    private void writeHomozygousDisruptions(@NotNull String sampleId, @NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        for (HomozygousDisruption homozygousDisruption : homozygousDisruptions) {
            context.insertInto(HOMOZYGOUSDISRUPTION,
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
                    .execute();
        }
    }

    private void writeDisruptions(@NotNull String sampleId, @NotNull Set<Disruption> disruptions) {
        for (Disruption disruption : disruptions) {
            context.insertInto(DISRUPTION,
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
                            DISRUPTION.CLUSTERGROUP,
                            DISRUPTION.DISRUPTEDRANGE)
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
                            disruption.clusterGroup(),
                            disruption.range())
                    .execute();
        }
    }

    private void writeFusions(@NotNull String sampleId, @NotNull Set<Fusion> fusions) {
        for (Fusion fusion : fusions) {
            context.insertInto(FUSION,
                            FUSION.SAMPLEID,
                            FUSION.ISREPORTABLE,
                            FUSION.EVENT,
                            FUSION.DRIVERLIKELIHOOD,
                            FUSION.GENESTART,
                            FUSION.GENETRANSCRIPTSTART,
                            FUSION.GENECONTEXTSTART,
                            FUSION.FUSEDEXONUP,
                            FUSION.GENEEND,
                            FUSION.GENETRANSCRIPTEND,
                            FUSION.GENECONTEXTEND,
                            FUSION.FUSEDEXONDOWN,
                            FUSION.PROTEINEFFECT,
                            FUSION.ISASSOCIATEDWITHDRUGRESISTANCE,
                            FUSION.DRIVERTYPE)
                    .values(sampleId,
                            DataUtil.toByte(fusion.isReportable()),
                            fusion.event(),
                            fusion.driverLikelihood().toString(),
                            fusion.geneStart(),
                            fusion.geneTranscriptStart(),
                            fusion.geneContextStart(),
                            fusion.fusedExonUp(),
                            fusion.geneEnd(),
                            fusion.geneTranscriptEnd(),
                            fusion.geneContextEnd(),
                            fusion.fusedExonDown(),
                            fusion.proteinEffect().toString(),
                            DataUtil.toByte(fusion.isAssociatedWithDrugResistance()),
                            fusion.driverType().toString())
                    .execute();
        }
    }

    private void writeViruses(@NotNull String sampleId, @NotNull Set<Virus> viruses) {
        for (Virus virus : viruses) {
            context.insertInto(VIRUS,
                            VIRUS.SAMPLEID,
                            VIRUS.ISREPORTABLE,
                            VIRUS.EVENT,
                            VIRUS.DRIVERLIKELIHOOD,
                            VIRUS.NAME,
                            VIRUS.INTERPRETATION,
                            VIRUS.INTEGRATIONS)
                    .values(sampleId,
                            DataUtil.toByte(virus.isReportable()),
                            virus.event(),
                            virus.driverLikelihood().toString(),
                            virus.name(),
                            virus.interpretation(),
                            virus.integrations())
                    .execute();
        }
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
}
