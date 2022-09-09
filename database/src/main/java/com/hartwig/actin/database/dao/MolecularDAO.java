package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.ACTINTRIALEVIDENCE;
import static com.hartwig.actin.database.Tables.AMPLIFICATION;
import static com.hartwig.actin.database.Tables.DISRUPTION;
import static com.hartwig.actin.database.Tables.EXTERNALTRIALEVIDENCE;
import static com.hartwig.actin.database.Tables.FUSION;
import static com.hartwig.actin.database.Tables.HLAALLELE;
import static com.hartwig.actin.database.Tables.HOMOZYGOUSDISRUPTION;
import static com.hartwig.actin.database.Tables.LOSS;
import static com.hartwig.actin.database.Tables.MOLECULAR;
import static com.hartwig.actin.database.Tables.PHARMACO;
import static com.hartwig.actin.database.Tables.TREATMENTEVIDENCE;
import static com.hartwig.actin.database.Tables.VARIANT;
import static com.hartwig.actin.database.Tables.VIRUS;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

class MolecularDAO {

    @NotNull
    private final DSLContext context;

    public MolecularDAO(@NotNull final DSLContext context) {
        this.context = context;
    }

    public void clear(@NotNull MolecularRecord record) {
        String sampleId = record.sampleId();

        context.delete(MOLECULAR).where(MOLECULAR.SAMPLEID.eq(sampleId)).execute();
        context.delete(VARIANT).where(VARIANT.SAMPLEID.eq(sampleId)).execute();
        context.delete(AMPLIFICATION).where(AMPLIFICATION.SAMPLEID.eq(sampleId)).execute();
        context.delete(LOSS).where(LOSS.SAMPLEID.eq(sampleId)).execute();
        context.delete(HOMOZYGOUSDISRUPTION).where(HOMOZYGOUSDISRUPTION.SAMPLEID.eq(sampleId)).execute();
        context.delete(DISRUPTION).where(DISRUPTION.SAMPLEID.eq(sampleId)).execute();
        context.delete(FUSION).where(FUSION.SAMPLEID.eq(sampleId)).execute();
        context.delete(VIRUS).where(VIRUS.SAMPLEID.eq(sampleId)).execute();
        context.delete(HLAALLELE).where(HLAALLELE.SAMPLEID.eq(sampleId)).execute();
        context.delete(PHARMACO).where(PHARMACO.SAMPLEID.eq(sampleId)).execute();
        context.delete(ACTINTRIALEVIDENCE).where(ACTINTRIALEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(EXTERNALTRIALEVIDENCE).where(EXTERNALTRIALEVIDENCE.SAMPLEID.eq(sampleId)).execute();
        context.delete(TREATMENTEVIDENCE).where(TREATMENTEVIDENCE.SAMPLEID.eq(sampleId)).execute();
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

        MolecularEvidence evidence = record.evidence();
        writeActinTrialEvidence(sampleId, evidence.actinSource(), evidence.actinTrials());
        writeExternalTrialEvidence(sampleId, evidence.externalTrialSource(), evidence.externalTrials());
        writeTreatmentEvidence(sampleId, evidence);
    }

    private void writeMolecularDetails(@NotNull MolecularRecord record) {
        PredictedTumorOrigin predictedTumorOrigin = record.characteristics().predictedTumorOrigin();

        context.insertInto(MOLECULAR,
                        MOLECULAR.PATIENTID,
                        MOLECULAR.SAMPLEID,
                        MOLECULAR.EXPERIMENTTYPE,
                        MOLECULAR.EXPERIMENTDATE,
                        MOLECULAR.CONTAINSTUMORCELLS,
                        MOLECULAR.HASSUFFICIENTQUALITY,
                        MOLECULAR.PURITY,
                        MOLECULAR.PREDICTEDTUMORTYPE,
                        MOLECULAR.PREDICTEDTUMORLIKELIHOOD,
                        MOLECULAR.ISMICROSATELLITEUNSTABLE,
                        MOLECULAR.ISHOMOLOGOUSREPAIRDEFICIENT,
                        MOLECULAR.TUMORMUTATIONALBURDEN,
                        MOLECULAR.TUMORMUTATIONALLOAD)
                .values(record.patientId(),
                        record.sampleId(),
                        record.type().toString(),
                        record.date(),
                        DataUtil.toByte(record.containsTumorCells()),
                        DataUtil.toByte(record.hasSufficientQuality()),
                        record.characteristics().purity(),
                        predictedTumorOrigin != null ? predictedTumorOrigin.tumorType() : null,
                        predictedTumorOrigin != null ? predictedTumorOrigin.likelihood() : null,
                        DataUtil.toByte(record.characteristics().isMicrosatelliteUnstable()),
                        DataUtil.toByte(record.characteristics().isHomologousRepairDeficient()),
                        record.characteristics().tumorMutationalBurden(),
                        record.characteristics().tumorMutationalLoad())
                .execute();
    }

    private void writeVariants(@NotNull String sampleId, @NotNull Set<Variant> variants) {
        for (Variant variant : variants) {
            context.insertInto(VARIANT,
                            VARIANT.SAMPLEID,
                            VARIANT.EVENT,
                            VARIANT.DRIVERLIKELIHOOD,
                            VARIANT.GENE,
                            VARIANT.IMPACT,
                            VARIANT.VARIANTCOPYNUMBER,
                            VARIANT.TOTALCOPYNUMBER,
                            VARIANT.DRIVERTYPE,
                            VARIANT.CLONALLIKELIHOOD)
                    .values(sampleId,
                            variant.event(),
                            variant.driverLikelihood().toString(),
                            variant.gene(),
                            variant.impact(),
                            variant.variantCopyNumber(),
                            variant.totalCopyNumber(),
                            variant.driverType().toString(),
                            variant.clonalLikelihood())
                    .execute();
        }
    }

    private void writeAmplifications(@NotNull String sampleId, @NotNull Set<Amplification> amplifications) {
        for (Amplification amplification : amplifications) {
            context.insertInto(AMPLIFICATION,
                            AMPLIFICATION.SAMPLEID,
                            AMPLIFICATION.EVENT,
                            AMPLIFICATION.DRIVERLIKELIHOOD,
                            AMPLIFICATION.GENE,
                            AMPLIFICATION.ISPARTIAL,
                            AMPLIFICATION.COPIES)
                    .values(sampleId,
                            amplification.event(),
                            amplification.driverLikelihood().toString(),
                            amplification.gene(),
                            DataUtil.toByte(amplification.isPartial()),
                            amplification.copies())
                    .execute();
        }
    }

    private void writeLosses(@NotNull String sampleId, @NotNull Set<Loss> losses) {
        for (Loss loss : losses) {
            context.insertInto(LOSS, LOSS.SAMPLEID, LOSS.EVENT, LOSS.DRIVERLIKELIHOOD, LOSS.GENE, LOSS.ISPARTIAL)
                    .values(sampleId, loss.event(), loss.driverLikelihood().toString(), loss.gene(), DataUtil.toByte(loss.isPartial()))
                    .execute();
        }
    }

    private void writeHomozygousDisruptions(@NotNull String sampleId, @NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        for (HomozygousDisruption homozygousDisruption : homozygousDisruptions) {
            context.insertInto(HOMOZYGOUSDISRUPTION,
                            HOMOZYGOUSDISRUPTION.SAMPLEID,
                            HOMOZYGOUSDISRUPTION.EVENT,
                            HOMOZYGOUSDISRUPTION.DRIVERLIKELIHOOD,
                            HOMOZYGOUSDISRUPTION.GENE)
                    .values(sampleId,
                            homozygousDisruption.event(),
                            homozygousDisruption.driverLikelihood().toString(),
                            homozygousDisruption.gene())
                    .execute();
        }
    }

    private void writeDisruptions(@NotNull String sampleId, @NotNull Set<Disruption> disruptions) {
        for (Disruption disruption : disruptions) {
            context.insertInto(DISRUPTION,
                            DISRUPTION.SAMPLEID,
                            DISRUPTION.EVENT,
                            DISRUPTION.DRIVERLIKELIHOOD,
                            DISRUPTION.GENE,
                            DISRUPTION.TYPE,
                            DISRUPTION.JUNCTIONCOPYNUMBER,
                            DISRUPTION.UNDISRUPTEDCOPYNUMBER,
                            DISRUPTION.DISRUPTEDRANGE)
                    .values(sampleId,
                            disruption.event(),
                            disruption.driverLikelihood().toString(),
                            disruption.gene(),
                            disruption.type(),
                            disruption.junctionCopyNumber(),
                            disruption.undisruptedCopyNumber(),
                            disruption.range())
                    .execute();
        }
    }

    private void writeFusions(@NotNull String sampleId, @NotNull Set<Fusion> fusions) {
        for (Fusion fusion : fusions) {
            context.insertInto(FUSION,
                            FUSION.SAMPLEID,
                            FUSION.EVENT,
                            FUSION.DRIVERLIKELIHOOD,
                            FUSION.FIVEGENE,
                            FUSION.THREEGENE,
                            FUSION.DETAILS,
                            FUSION.DRIVERTYPE)
                    .values(sampleId,
                            fusion.event(),
                            fusion.driverLikelihood().toString(),
                            fusion.fiveGene(),
                            fusion.threeGene(),
                            fusion.details(),
                            fusion.driverType().toString())
                    .execute();
        }
    }

    private void writeViruses(@NotNull String sampleId, @NotNull Set<Virus> viruses) {
        for (Virus virus : viruses) {
            context.insertInto(VIRUS, VIRUS.SAMPLEID, VIRUS.EVENT, VIRUS.DRIVERLIKELIHOOD, VIRUS.NAME, VIRUS.INTEGRATIONS)
                    .values(sampleId, virus.event(), virus.driverLikelihood().toString(), virus.name(), virus.integrations())
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

    private void writeActinTrialEvidence(@NotNull String sampleId, @NotNull String actinSource,
            @NotNull Set<ActinTrialEvidence> actinTrials) {
        for (ActinTrialEvidence evidence : actinTrials) {
            context.insertInto(ACTINTRIALEVIDENCE,
                            ACTINTRIALEVIDENCE.SAMPLEID,
                            ACTINTRIALEVIDENCE.SOURCE,
                            ACTINTRIALEVIDENCE.EVENT,
                            ACTINTRIALEVIDENCE.TRIALACRONYM,
                            ACTINTRIALEVIDENCE.COHORTCODE,
                            ACTINTRIALEVIDENCE.ISINCLUSIONCRITERION,
                            ACTINTRIALEVIDENCE.TYPE,
                            ACTINTRIALEVIDENCE.GENE,
                            ACTINTRIALEVIDENCE.MUTATION)
                    .values(sampleId,
                            actinSource,
                            evidence.event(),
                            evidence.trialAcronym(),
                            evidence.cohortId(),
                            DataUtil.toByte(evidence.isInclusionCriterion()),
                            evidence.type().toString(),
                            evidence.gene(),
                            evidence.mutation())
                    .execute();
        }
    }

    private void writeExternalTrialEvidence(@NotNull String sampleId, @NotNull String externalTrialSource,
            @NotNull Set<ExternalTrialEvidence> externalTrials) {
        for (ExternalTrialEvidence evidence : externalTrials) {
            context.insertInto(EXTERNALTRIALEVIDENCE,
                    EXTERNALTRIALEVIDENCE.SAMPLEID,
                    EXTERNALTRIALEVIDENCE.SOURCE,
                    EXTERNALTRIALEVIDENCE.EVENT,
                    EXTERNALTRIALEVIDENCE.TRIAL).values(sampleId, externalTrialSource, evidence.event(), evidence.trial()).execute();
        }
    }

    private void writeTreatmentEvidence(@NotNull String sampleId, @NotNull MolecularEvidence evidence) {
        String evidenceSource = evidence.evidenceSource();
        writeEvidenceForType(sampleId, evidenceSource, "Approved", true, evidence.approvedEvidence());
        writeEvidenceForType(sampleId, evidenceSource, "Experimental (on-label)", true, evidence.onLabelExperimentalEvidence());
        writeEvidenceForType(sampleId, evidenceSource, "Experimental (off-label)", true, evidence.offLabelExperimentalEvidence());
        writeEvidenceForType(sampleId, evidenceSource, "Pre-clinical", true, evidence.preClinicalEvidence());
        writeEvidenceForType(sampleId, evidenceSource, "Resistance (known)", false, evidence.knownResistanceEvidence());
        writeEvidenceForType(sampleId, evidenceSource, "Resistance (suspected)", false, evidence.suspectResistanceEvidence());
    }

    private void writeEvidenceForType(@NotNull String sampleId, @NotNull String source, @NotNull String type, boolean isResponsive,
            @NotNull Iterable<TreatmentEvidence> evidences) {
        for (TreatmentEvidence evidence : evidences) {
            context.insertInto(TREATMENTEVIDENCE,
                            TREATMENTEVIDENCE.SAMPLEID,
                            TREATMENTEVIDENCE.SOURCE,
                            TREATMENTEVIDENCE.TYPE,
                            TREATMENTEVIDENCE.EVENT,
                            TREATMENTEVIDENCE.TREATMENT,
                            TREATMENTEVIDENCE.ISRESPONSIVE)
                    .values(sampleId, source, type, evidence.event(), evidence.treatment(), DataUtil.toByte(isResponsive))
                    .execute();
        }
    }
}
