package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class EligibleExternalTrialsGenerator implements TableGenerator {

    @NotNull
    private final String source;
    @NotNull
    private final Multimap<String, String> externalTrialsPerEvent;
    private final float keyWidth;
    private final float valueWidth;

    public EligibleExternalTrialsGenerator(@NotNull final String source, @NotNull final Multimap<String, String> externalTrialsPerEvent,
            final float keyWidth, final float valueWidth) {
        this.source = source;
        this.externalTrialsPerEvent = externalTrialsPerEvent;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return String.format("%s trials potentially eligible based on molecular results (%d)", source, externalTrialsPerEvent.size());
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addHeaderCell(Cells.createHeader("Event"));
        table.addHeaderCell(Cells.createHeader("Trials"));

        Set<String> events = Sets.newTreeSet(externalTrialsPerEvent.keySet());
        for (String event : events) {
            table.addCell(Cells.createContent(event));

            StringJoiner joiner = Formats.commaJoiner();
            for (String trial : externalTrialsPerEvent.get(event)) {
                joiner.add(trial);
            }
            table.addCell(Cells.createContent(joiner.toString()));
        }

        return Tables.makeWrapping(table);
    }
}
