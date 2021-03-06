package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.report.interpretation.ClonalityInterpreter;
import com.hartwig.actin.report.interpretation.MolecularDriverEntry;
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularDriversGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord molecular;
    private final float width;

    public MolecularDriversGenerator(@NotNull final MolecularRecord molecular, final float width) {
        this.molecular = molecular;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "Drivers";
    }

    @NotNull
    @Override
    public Table contents() {
        float colWidth = width / 8;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth * 2, colWidth, colWidth, colWidth, colWidth, colWidth);

        table.addHeaderCell(Cells.createHeader("Type"));
        table.addHeaderCell(Cells.createHeader("Driver"));
        table.addHeaderCell(Cells.createHeader("Driver likelihood"));
        table.addHeaderCell(Cells.createHeader("Trials in " + molecular.evidence().actinSource()));
        table.addHeaderCell(Cells.createHeader("Trials in " + molecular.evidence().externalTrialSource()));
        table.addHeaderCell(Cells.createHeader("Best evidence in " + molecular.evidence().evidenceSource()));
        table.addHeaderCell(Cells.createHeader("Resistance in " + molecular.evidence().evidenceSource()));

        Set<MolecularDriverEntry> entries = MolecularDriverEntryFactory.create(molecular.drivers(), molecular.evidence());

        for (MolecularDriverEntry entry : entries) {
            table.addCell(Cells.createContent(entry.driverType()));
            table.addCell(Cells.createContent(entry.driver()));
            table.addCell(Cells.createContent(entry.driverLikelihood().display()));
            table.addCell(Cells.createContent(concat(entry.actinTrials())));
            table.addCell(Cells.createContent(concat(entry.externalTrials())));
            table.addCell(Cells.createContent(nullToEmpty(entry.bestResponsiveEvidence())));
            table.addCell(Cells.createContent(nullToEmpty(entry.bestResistanceEvidence())));
        }

        if (hasPotentiallySubclonalVariants(molecular.drivers().variants())) {
            String note = "* Variant has > " + Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF) + " likelihood of being sub-clonal";
            table.addCell(Cells.createSpanningSubNote(note, table));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String concat(@NotNull Set<String> treatments) {
        if (treatments.isEmpty()) {
            return Strings.EMPTY;
        }
        StringJoiner joiner = Formats.commaJoiner();
        for (String treatment : treatments) {
            joiner.add(treatment);
        }
        return joiner.toString();
    }

    @NotNull
    private static String nullToEmpty(@Nullable String string) {
        return string != null ? string : Strings.EMPTY;
    }

    private static boolean hasPotentiallySubclonalVariants(@NotNull Set<Variant> variants) {
        for (Variant variant : variants) {
            if (ClonalityInterpreter.isPotentiallySubclonal(variant)) {
                return true;
            }
        }

        return false;
    }
}
