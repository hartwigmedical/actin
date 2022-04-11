package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.ACTIVATEDGENE;
import static com.hartwig.actin.database.Tables.AMPLIFIEDGENE;
import static com.hartwig.actin.database.Tables.FUSIONGENE;
import static com.hartwig.actin.database.Tables.INACTIVATEDGENE;
import static com.hartwig.actin.database.Tables.MOLECULAR;
import static com.hartwig.actin.database.Tables.MOLECULAREVIDENCE;
import static com.hartwig.actin.database.Tables.MUTATION;
import static com.hartwig.actin.database.Tables.WILDTYPEGENE;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

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
        context.delete(MUTATION).where(MUTATION.SAMPLEID.eq(sampleId)).execute();
        context.delete(ACTIVATEDGENE).where(ACTIVATEDGENE.SAMPLEID.eq(sampleId)).execute();
        context.delete(INACTIVATEDGENE).where(INACTIVATEDGENE.SAMPLEID.eq(sampleId)).execute();
        context.delete(AMPLIFIEDGENE).where(AMPLIFIEDGENE.SAMPLEID.eq(sampleId)).execute();
        context.delete(WILDTYPEGENE).where(WILDTYPEGENE.SAMPLEID.eq(sampleId)).execute();
        context.delete(FUSIONGENE).where(FUSIONGENE.SAMPLEID.eq(sampleId)).execute();
        context.delete(MOLECULAREVIDENCE).where(MOLECULAREVIDENCE.SAMPLEID.eq(sampleId)).execute();
    }

    public void writeMolecularRecord(@NotNull MolecularRecord record) {
        String sampleId = record.sampleId();
        writeMolecularDetails(sampleId, record);
        writeMutations(sampleId, record.mappedEvents().mutations());
        writeActivatedGenes(sampleId, record.mappedEvents().activatedGenes());
        writeInactivatedGenes(sampleId, record.mappedEvents().inactivatedGenes());
        writeAmplifiedGenes(sampleId, record.mappedEvents().amplifiedGenes());
        writeWildtypeGenes(sampleId, record.mappedEvents().wildtypeGenes());
        writeFusionGenes(sampleId, record.mappedEvents().fusions());
        writeMolecularEvidence(sampleId, record.evidence());
    }

    private void writeMolecularDetails(@NotNull String sampleId, @NotNull MolecularRecord record) {
        // TODO Fix hasReliableQuality
        context.insertInto(MOLECULAR,
                MOLECULAR.SAMPLEID,
                MOLECULAR.EXPERIMENTTYPE,
                MOLECULAR.EXPERIMENTDATE,
                MOLECULAR.HASRELIABLEQUALITY,
                MOLECULAR.ISMICROSATELLITEUNSTABLE,
                MOLECULAR.ISHOMOLOGOUSREPAIRDEFICIENT,
                MOLECULAR.TUMORMUTATIONALBURDEN,
                MOLECULAR.TUMORMUTATIONALLOAD)
                .values(sampleId,
                        record.type().toString(),
                        record.date(),
                        DataUtil.toByte(true),
                        DataUtil.toByte(record.characteristics().isMicrosatelliteUnstable()),
                        DataUtil.toByte(record.characteristics().isHomologousRepairDeficient()),
                        record.characteristics().tumorMutationalBurden(),
                        record.characteristics().tumorMutationalLoad())
                .execute();
    }

    private void writeMutations(@NotNull String sampleId, @NotNull Set<GeneMutation> mutations) {
        for (GeneMutation mutation : mutations) {
            context.insertInto(MUTATION, MUTATION.SAMPLEID, MUTATION.GENE, MUTATION.MUTATION_)
                    .values(sampleId, mutation.gene(), mutation.mutation())
                    .execute();
        }
    }

    private void writeActivatedGenes(@NotNull String sampleId, @NotNull Set<String> activatedGenes) {
        for (String activatedGene : activatedGenes) {
            context.insertInto(ACTIVATEDGENE, ACTIVATEDGENE.SAMPLEID, ACTIVATEDGENE.GENE).values(sampleId, activatedGene).execute();
        }
    }

    private void writeInactivatedGenes(@NotNull String sampleId, @NotNull Set<InactivatedGene> inactivatedGenes) {
        for (InactivatedGene inactivatedGene : inactivatedGenes) {
            context.insertInto(INACTIVATEDGENE, INACTIVATEDGENE.SAMPLEID, INACTIVATEDGENE.GENE, INACTIVATEDGENE.HASBEENDELETED)
                    .values(sampleId, inactivatedGene.gene(), DataUtil.toByte(inactivatedGene.hasBeenDeleted()))
                    .execute();
        }
    }

    private void writeAmplifiedGenes(@NotNull String sampleId, @NotNull Set<String> amplifiedGenes) {
        for (String amplifiedGene : amplifiedGenes) {
            context.insertInto(AMPLIFIEDGENE, AMPLIFIEDGENE.SAMPLEID, AMPLIFIEDGENE.GENE).values(sampleId, amplifiedGene).execute();
        }
    }

    private void writeWildtypeGenes(@NotNull String sampleId, @NotNull Set<String> wildtypeGenes) {
        for (String wildtypeGene : wildtypeGenes) {
            context.insertInto(WILDTYPEGENE, WILDTYPEGENE.SAMPLEID, WILDTYPEGENE.GENE).values(sampleId, wildtypeGene).execute();
        }
    }

    private void writeFusionGenes(@NotNull String sampleId, @NotNull Set<FusionGene> fusions) {
        for (FusionGene fusionGene : fusions) {
            context.insertInto(FUSIONGENE, FUSIONGENE.SAMPLEID, FUSIONGENE.FIVEGENE, FUSIONGENE.THREEGENE).
                    values(sampleId, fusionGene.fiveGene(), fusionGene.threeGene()).execute();
        }
    }

    private void writeMolecularEvidence(@NotNull String sampleId, @NotNull MolecularEvidence evidence) {
        writeEvidenceForTypeAndSource(sampleId, evidence.actinTrials(), "Experimental", true, "ACTIN");
        writeEvidenceForTypeAndSource(sampleId, evidence.externalTrials(), "Experimental", true, evidence.externalTrialSource());
        writeEvidenceForTypeAndSource(sampleId, evidence.approvedResponsiveEvidence(), "Approved", true, evidence.evidenceSource());
        writeEvidenceForTypeAndSource(sampleId, evidence.experimentalResponsiveEvidence(), "Experimental", true, evidence.evidenceSource());
        writeEvidenceForTypeAndSource(sampleId, evidence.otherResponsiveEvidence(), "Other", true, evidence.evidenceSource());
        writeEvidenceForTypeAndSource(sampleId, evidence.resistanceEvidence(), "Resistance", false, evidence.evidenceSource());
    }

    private void writeEvidenceForTypeAndSource(@NotNull String sampleId, @NotNull Iterable<EvidenceEntry> evidences,
            @NotNull String type, boolean isResponsive, @NotNull String source) {
        for (EvidenceEntry evidence : evidences) {
            context.insertInto(MOLECULAREVIDENCE,
                    MOLECULAREVIDENCE.SAMPLEID,
                    MOLECULAREVIDENCE.TYPE,
                    MOLECULAREVIDENCE.EVENT,
                    MOLECULAREVIDENCE.TREATMENT,
                    MOLECULAREVIDENCE.ISRESPONSIVE,
                    MOLECULAREVIDENCE.SOURCE)
                    .values(sampleId, type, evidence.event(), evidence.treatment(), DataUtil.toByte(isResponsive), source)
                    .execute();
        }
    }
}
