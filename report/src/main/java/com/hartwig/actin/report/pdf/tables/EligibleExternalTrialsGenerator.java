package com.hartwig.actin.report.pdf.tables;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

// TODO Remove duplication with RecentMolecularResultsGenerator
public class EligibleExternalTrialsGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord molecular;
    private final float keyWidth;
    private final float valueWidth;

    public EligibleExternalTrialsGenerator(@NotNull final MolecularRecord molecular, final float keyWidth, final float valueWidth) {
        this.molecular = molecular;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Additional " + molecular.externalTrialSource() + " trials eligible based on molecular results";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addHeaderCell(Cells.createHeader("Event"));
        table.addHeaderCell(Cells.createHeader("Trial"));

        for (MolecularEvidence evidence : additionalExternalTrialEvidence(molecular)) {
            table.addCell(Cells.createContent(evidence.event()));
            table.addCell(Cells.createContent(evidence.treatment()));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static Set<MolecularEvidence> additionalExternalTrialEvidence(@NotNull MolecularRecord molecular) {
        Set<MolecularEvidence> filtered = Sets.newHashSet();
        Set<String> eventsToSkip = eventsWithApprovedOrActinEvidence(molecular);
        for (MolecularEvidence evidence : molecular.externalTrials()) {
            if (!eventsToSkip.contains(evidence.event())) {
                filtered.add(evidence);
            }
        }
        return filtered;
    }

    @NotNull
    private static Set<String> eventsWithApprovedOrActinEvidence(@NotNull MolecularRecord molecular) {
        Set<String> events = Sets.newHashSet();
        for (MolecularEvidence evidence : molecular.approvedResponsiveEvidence()) {
            events.add(evidence.event());
        }

        for (MolecularEvidence evidence : molecular.actinTrials()) {
            events.add(evidence.event());
        }

        return events;
    }
}
