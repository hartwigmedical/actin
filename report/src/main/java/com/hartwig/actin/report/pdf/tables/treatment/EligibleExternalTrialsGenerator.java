package com.hartwig.actin.report.pdf.tables.treatment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
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
    private final Set<EvidenceEntry> evidenceForExternalTrials;
    private final float keyWidth;
    private final float valueWidth;

    public EligibleExternalTrialsGenerator(@NotNull final String source, @NotNull final Set<EvidenceEntry> evidenceForExternalTrials,
            final float keyWidth, final float valueWidth) {
        this.source = source;
        this.evidenceForExternalTrials = evidenceForExternalTrials;
        this.keyWidth = keyWidth;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return source + " trials potentially eligible based on molecular results";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(keyWidth, valueWidth);

        table.addHeaderCell(Cells.createHeader("Event"));
        table.addHeaderCell(Cells.createHeader("Trials"));

        Map<String, List<String>> treatmentsPerEvent = toTreatmentMapPerEvent(evidenceForExternalTrials);
        Set<String> events = Sets.newTreeSet(treatmentsPerEvent.keySet());
        for (String event : events) {
            table.addCell(Cells.createContent(event));

            StringJoiner joiner = Formats.commaJoiner();
            for (String treatment : treatmentsPerEvent.get(event)) {
                joiner.add(treatment);
            }
            table.addCell(Cells.createContent(joiner.toString()));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static Map<String, List<String>> toTreatmentMapPerEvent(@NotNull Iterable<EvidenceEntry> evidences) {
        Map<String, List<String>> treatmentsPerEvent = Maps.newHashMap();
        for (EvidenceEntry evidence : evidences) {
            List<String> treatments = treatmentsPerEvent.get(evidence.event());
            if (treatments == null) {
                treatments = Lists.newArrayList();
            }
            treatments.add(evidence.treatment());
            treatmentsPerEvent.put(evidence.event(), treatments);
        }
        return treatmentsPerEvent;
    }
}
