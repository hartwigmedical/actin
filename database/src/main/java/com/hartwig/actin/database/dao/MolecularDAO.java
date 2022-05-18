package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.ACTIVATEDGENE;
import static com.hartwig.actin.database.Tables.AMPLIFIEDGENE;
import static com.hartwig.actin.database.Tables.FUSEDGENE;
import static com.hartwig.actin.database.Tables.INACTIVATEDGENE;
import static com.hartwig.actin.database.Tables.MOLECULAR;
import static com.hartwig.actin.database.Tables.MOLECULAREVIDENCE;
import static com.hartwig.actin.database.Tables.MUTATION;
import static com.hartwig.actin.database.Tables.WILDTYPEGENE;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.interpretation.ActionableActinEvents;
import com.hartwig.actin.molecular.interpretation.GeneMutation;
import com.hartwig.actin.molecular.interpretation.MolecularInterpreter;

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
        context.delete(FUSEDGENE).where(FUSEDGENE.SAMPLEID.eq(sampleId)).execute();
        context.delete(MOLECULAREVIDENCE).where(MOLECULAREVIDENCE.SAMPLEID.eq(sampleId)).execute();
    }

    public void writeMolecularRecord(@NotNull MolecularRecord record) {
        String sampleId = record.sampleId();
        writeMolecularDetails(sampleId, record);

        ActionableActinEvents actionableActinEvents = MolecularInterpreter.extractActionableEvents(record);
        writeMutations(sampleId, actionableActinEvents.mutations());
        writeActivatedGenes(sampleId, actionableActinEvents.activatedGenes());
        writeInactivatedGenes(sampleId, actionableActinEvents.inactivatedGenes());
        writeAmplifiedGenes(sampleId, actionableActinEvents.amplifiedGenes());
        writeWildtypeGenes(sampleId, actionableActinEvents.wildtypeGenes());
        writeFusedGenes(sampleId, actionableActinEvents.fusedGenes());
        writeMolecularEvidence(sampleId, record.evidence());
    }

    private void writeMolecularDetails(@NotNull String sampleId, @NotNull MolecularRecord record) {
        context.insertInto(MOLECULAR,
                MOLECULAR.SAMPLEID,
                MOLECULAR.EXPERIMENTTYPE,
                MOLECULAR.EXPERIMENTDATE,
                MOLECULAR.HASRELIABLEQUALITY,
                MOLECULAR.PURITY,
                MOLECULAR.ISMICROSATELLITEUNSTABLE,
                MOLECULAR.ISHOMOLOGOUSREPAIRDEFICIENT,
                MOLECULAR.TUMORMUTATIONALBURDEN,
                MOLECULAR.TUMORMUTATIONALLOAD)
                .values(sampleId,
                        record.type().toString(),
                        record.date(),
                        DataUtil.toByte(record.hasReliableQuality()),
                        record.characteristics().purity(),
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

    private void writeInactivatedGenes(@NotNull String sampleId, @NotNull Set<String> inactivatedGenes) {
        for (String inactivatedGene : inactivatedGenes) {
            context.insertInto(INACTIVATEDGENE, INACTIVATEDGENE.SAMPLEID, INACTIVATEDGENE.GENE).values(sampleId, inactivatedGene).execute();
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

    private void writeFusedGenes(@NotNull String sampleId, @NotNull Set<String> fusedGenes) {
        for (String fusedGene : fusedGenes) {
            context.insertInto(FUSEDGENE, FUSEDGENE.SAMPLEID, FUSEDGENE.GENE).values(sampleId, fusedGene).execute();
        }
    }

    private void writeMolecularEvidence(@NotNull String sampleId, @NotNull MolecularEvidence evidence) {
        writeEvidenceForTypeAndSource(sampleId, evidence.actinTrials(), "Experimental", true, "ACTIN");
        writeEvidenceForTypeAndSource(sampleId, evidence.externalTrials(), "Experimental", true, evidence.externalTrialSource());

        String evidenceSource = evidence.evidenceSource();
        writeEvidenceForTypeAndSource(sampleId, evidence.approvedEvidence(), "Approved", true, evidenceSource);
        writeEvidenceForTypeAndSource(sampleId, evidence.onLabelExperimentalEvidence(), "Experimental (on-label)", true, evidenceSource);
        writeEvidenceForTypeAndSource(sampleId, evidence.offLabelExperimentalEvidence(), "Experimental (off-label)", true, evidenceSource);
        writeEvidenceForTypeAndSource(sampleId, evidence.preClinicalEvidence(), "Pre-clinical", true, evidenceSource);
        writeEvidenceForTypeAndSource(sampleId, evidence.knownResistanceEvidence(), "Resistance (known)", false, evidenceSource);
        writeEvidenceForTypeAndSource(sampleId, evidence.suspectResistanceEvidence(), "Resistance (suspected)", false, evidenceSource);
    }

    private void writeEvidenceForTypeAndSource(@NotNull String sampleId, @NotNull Iterable<EvidenceEntry> evidences, @NotNull String type,
            boolean isResponsive, @NotNull String source) {
        for (EvidenceEntry evidence : evidences) {
            context.insertInto(MOLECULAREVIDENCE,
                    MOLECULAREVIDENCE.SAMPLEID,
                    MOLECULAREVIDENCE.TYPE,
                    MOLECULAREVIDENCE.EVENT,
                    MOLECULAREVIDENCE.TREATMENT,
                    MOLECULAREVIDENCE.ISRESPONSIVE,
                    MOLECULAREVIDENCE.SOURCE)
                    .values(sampleId,
                            type,
                            evidence.event(),
                            evidence.treatment(),
                            DataUtil.toByte(isResponsive),
                            source)
                    .execute();
        }
    }
}
