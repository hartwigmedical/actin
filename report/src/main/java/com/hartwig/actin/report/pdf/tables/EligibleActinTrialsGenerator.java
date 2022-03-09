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

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class EligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final TreatmentMatchSummary treatmentMatchSummary;
    @NotNull
    private final String source;
    private final float trialIdColWidth;
    private final float trialAcronymColWidth;
    private final float cohortDescriptionColWidth;
    private final float cohortOpenColWidth;

    @NotNull
    public static EligibleActinTrialsGenerator fromTreatmentMatch(@NotNull TreatmentMatch treatmentMatch, @NotNull String source,
            float contentWidth) {
        float trialIdColWidth = contentWidth / 6;
        float trialAcronymColWidth = contentWidth / 6;
        float cohortOpenColWidth = contentWidth / 8;
        float cohortDescriptionColWidth = contentWidth - (trialIdColWidth + trialAcronymColWidth + cohortOpenColWidth);

        return new EligibleActinTrialsGenerator(TreatmentMatchSummarizer.summarize(treatmentMatch),
                source,
                trialIdColWidth,
                trialAcronymColWidth,
                cohortDescriptionColWidth,
                cohortOpenColWidth);
    }

    public EligibleActinTrialsGenerator(@NotNull final TreatmentMatchSummary treatmentMatchSummary, @NotNull final String source,
            final float trialIdColWidth, final float trialAcronymColWidth, final float cohortDescriptionColWidth,
            final float cohortOpenColWidth) {
        this.treatmentMatchSummary = treatmentMatchSummary;
        this.source = source;
        this.trialIdColWidth = trialIdColWidth;
        this.trialAcronymColWidth = trialAcronymColWidth;
        this.cohortDescriptionColWidth = cohortDescriptionColWidth;
        this.cohortOpenColWidth = cohortOpenColWidth;
    }

    @NotNull
    @Override
    public String title() {
        return source + " trials and cohorts considered potentially eligible (" + eligibleCohortCount(treatmentMatchSummary) + ")";
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
        Table table = Tables.createFixedWidthCols(trialIdColWidth, trialAcronymColWidth, cohortDescriptionColWidth, cohortOpenColWidth);

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
                table.addCell(Cells.createContent(acronym));
                table.addCell(Cells.createContent(Strings.EMPTY));
                table.addCell(Cells.createContentYesNo("Yes"));
            } else {
                for (CohortMetadata cohort : eligibleCohorts) {
                    table.addCell(Cells.createContent(trialId));
                    table.addCell(Cells.createContent(acronym));
                    table.addCell(Cells.createContent(cohort.description()));
                    table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(cohort.open())));
                }
            }
        }

        return Tables.makeWrapping(table);
    }
}
