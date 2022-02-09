package com.hartwig.actin.report.pdf.tables;

import java.util.List;
import java.util.Map;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummarizer;
import com.hartwig.actin.algo.interpretation.TreatmentMatchSummary;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class TrialMatchGenerator implements TableGenerator {

    @NotNull
    private final TreatmentMatchSummary treatmentMatchSummary;
    private final float col1Width;
    private final float col2Width;
    private final float col3Width;
    private final float col4Width;

    @NotNull
    public static TrialMatchGenerator fromTreatmentMatch(@NotNull TreatmentMatch treatmentMatch, float contentWidth) {
        float col1Width = contentWidth / 4;
        float col2Width = contentWidth / 4;
        float col3Width = contentWidth / 4;
        float col4Width = contentWidth - (col1Width + col2Width + col3Width);

        return new TrialMatchGenerator(TreatmentMatchSummarizer.summarize(treatmentMatch), col1Width, col2Width, col3Width, col4Width);
    }

    private TrialMatchGenerator(@NotNull final TreatmentMatchSummary treatmentMatchSummary, final float col1Width, final float col2Width,
            final float col3Width, final float col4Width) {
        this.treatmentMatchSummary = treatmentMatchSummary;
        this.col1Width = col1Width;
        this.col2Width = col2Width;
        this.col3Width = col3Width;
        this.col4Width = col4Width;
    }

    @NotNull
    @Override
    public String title() {
        return "Trials and cohorts considered potentially eligible (" + eligibleCohortCount(treatmentMatchSummary) + ")";
    }

    private static int eligibleCohortCount(@NotNull TreatmentMatchSummary treatmentMatchSummary) {
        int eligibleCount = 0;
        for (Map.Entry<TrialIdentification, List<CohortMetadata>> entry : treatmentMatchSummary.eligibleTrialMap().entrySet()) {
            List<CohortMetadata> eligibleCohorts = entry.getValue();
            eligibleCount += Math.max(1, eligibleCohorts.size());
        }
        return eligibleCount;
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { col1Width, col2Width, col3Width, col4Width });

        table.addHeaderCell(Cells.createHeader("Trial ID"));
        table.addHeaderCell(Cells.createHeader("Acronym"));
        table.addHeaderCell(Cells.createHeader("Cohort"));
        table.addHeaderCell(Cells.createHeader("Cohort open?"));

        for (Map.Entry<TrialIdentification, List<CohortMetadata>> entry : treatmentMatchSummary.eligibleTrialMap().entrySet()) {
            String trialId = entry.getKey().trialId();
            String acronym = entry.getKey().acronym();
            List<CohortMetadata> eligibleCohorts = entry.getValue();
            if (eligibleCohorts.isEmpty()) {
                table.addCell(Cells.createContent(trialId));
                table.addCell(Cells.createContent(trialId));
                table.addCell(Cells.createEmpty());
                table.addCell(Cells.createContentYesNo("Yes"));
            } else {
                for (CohortMetadata cohort : eligibleCohorts) {
                    table.addCell(Cells.createContent(trialId));
                    table.addCell(Cells.createContent(trialId));
                    table.addCell(Cells.createContent(cohort.description()));
                    table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(cohort.open())));
                }
            }
        }

        return Tables.makeWrapping(table);
    }
}
