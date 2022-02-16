package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.ACTIVATEDGENE;
import static com.hartwig.actin.database.Tables.AMPLIFIEDGENE;
import static com.hartwig.actin.database.Tables.FUSIONGENE;
import static com.hartwig.actin.database.Tables.INACTIVATEDGENE;
import static com.hartwig.actin.database.Tables.MOLECULAR;
import static com.hartwig.actin.database.Tables.MOLECULAREVIDENCE;
import static com.hartwig.actin.database.Tables.MUTATION;
import static com.hartwig.actin.database.Tables.WILDTYPEGENE;

import java.util.List;
import java.util.Set;

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
        writeMutations(sampleId, record.mutations());
        writeActivatedGenes(sampleId, record.activatedGenes());
        writeInactivatedGenes(sampleId, record.inactivatedGenes());
        writeAmplifiedGenes(sampleId, record.amplifiedGenes());
        writeWildtypeGenes(sampleId, record.wildtypeGenes());
        writeFusionGenes(sampleId, record.fusions());
        writeMolecularEvidence(sampleId, record);
    }

    private void writeMolecularDetails(@NotNull String sampleId, @NotNull MolecularRecord record) {
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
                        DataUtil.toByte(record.hasReliableQuality()),
                        DataUtil.toByte(record.isMicrosatelliteUnstable()),
                        DataUtil.toByte(record.isHomologousRepairDeficient()),
                        record.tumorMutationalBurden(),
                        record.tumorMutationalLoad())
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

    private void writeMolecularEvidence(@NotNull String sampleId, @NotNull MolecularRecord record) {
        writeEvidenceForTypeAndSource(sampleId, record.actinTrials(), "Experimental", true, "ACTIN");
        writeEvidenceForTypeAndSource(sampleId, record.externalTrials(), "Experimental", true, record.externalTrialSource());
        writeEvidenceForTypeAndSource(sampleId, record.approvedResponsiveEvidence(), "Approved", true, record.evidenceSource());
        writeEvidenceForTypeAndSource(sampleId, record.experimentalResponsiveEvidence(), "Experimental", true, record.evidenceSource());
        writeEvidenceForTypeAndSource(sampleId, record.otherResponsiveEvidence(), "Other", true, record.evidenceSource());
        writeEvidenceForTypeAndSource(sampleId, record.resistanceEvidence(), "Resistance", false, record.evidenceSource());
    }

    private void writeEvidenceForTypeAndSource(@NotNull String sampleId, @NotNull List<MolecularEvidence> evidences, @NotNull String type,
            boolean isResponsive, @NotNull String source) {
        for (MolecularEvidence evidence : evidences) {
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
