package com.hartwig.actin.report.pdf.tables.treatment;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvaluatedTrialComparator;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

import org.jetbrains.annotations.NotNull;

public class IneligibleActinTrialsGenerator implements TableGenerator {

    @NotNull
    private final Map<String, List<EvaluatedTrial>> trials;
    @NotNull
    private final String source;
    private final float trialColWidth;
    private final float cohortColWidth;
    private final float ineligibilityReasonColWith;
    private final boolean skipMatchingTrialDetails;

    @NotNull
    public static IneligibleActinTrialsGenerator fromEvaluatedTrials(@NotNull List<EvaluatedTrial> trials, float contentWidth,
            boolean skipMatchingTrialDetails) {
        Map<String, List<EvaluatedTrial>> ineligibleTrials = trials.stream()
                .filter(trial -> !trial.isPotentiallyEligible() && (trial.isOpen() || !skipMatchingTrialDetails))
                .collect(groupingBy(EvaluatedTrial::trialId));

        float trialColWidth = contentWidth / 9;
        float cohortColWidth = contentWidth / 2;
        float ineligibilityReasonColWidth = contentWidth - (trialColWidth + cohortColWidth);

        return new IneligibleActinTrialsGenerator(ineligibleTrials,
                TreatmentConstants.ACTIN_SOURCE,
                trialColWidth,
                cohortColWidth,
                ineligibilityReasonColWidth,
                skipMatchingTrialDetails);
    }

    private IneligibleActinTrialsGenerator(@NotNull final Map<String, List<EvaluatedTrial>> trials, @NotNull final String source,
            final float trialColWidth, final float cohortColWidth, final float ineligibilityReasonColWith,
            final boolean skipMatchingTrialDetails) {
        this.trials = trials;
        this.source = source;
        this.trialColWidth = trialColWidth;
        this.cohortColWidth = cohortColWidth;
        this.ineligibilityReasonColWith = ineligibilityReasonColWith;
        this.skipMatchingTrialDetails = skipMatchingTrialDetails;
    }

    @NotNull
    @Override
    public String title() {
        return String.format("%s trials and cohorts that are %sconsidered ineligible (%s)",
                source,
                skipMatchingTrialDetails ? "open but " : "",
                trials.size());
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth, ineligibilityReasonColWith);

        if (!trials.isEmpty()) {
            table.addHeaderCell(Cells.createHeader("Trial"));
            table.addHeaderCell(Cells.createHeader("Cohort"));
            table.addHeaderCell(Cells.createHeader("Ineligibility reasons"));
        }

        trials.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted(new EvaluatedTrialComparator())
                .map(EvaluatedTrial::trialId)
                .distinct()
                .map(trials::get)
                .forEach(cohortList -> {
                    EvaluatedTrial trial = cohortList.get(0);
                    if (trial != null) {
                        table.addCell(Cells.createContent(new Paragraph().addAll(Arrays.asList(new Text(trial.trialId()).addStyle(Styles.tableHighlightStyle()),
                                new Text(trial.acronym()).addStyle(Styles.tableContentStyle())))));

                        Table cohortSubTable = Tables.createFixedWidthCols(cohortColWidth);
                        Table ineligibilitySubTable = Tables.createFixedWidthCols(ineligibilityReasonColWith);

                        cohortList.stream().sorted(new EvaluatedTrialComparator()).forEach(cohort -> {
                            String cohortText = trial.cohort() == null ? "" : trial.cohort();
                            boolean noSlotsAvailable = trial.isOpen() && !trial.hasSlotsAvailable();
                            if (noSlotsAvailable) {
                                cohortText += " *";
                            }
                            cohortSubTable.addCell(Cells.createContentNoBorder(cohortText));

                            String ineligibilityText = cohort.fails().isEmpty() ? "?" : String.join(", ", cohort.fails());
                            ineligibilitySubTable.addCell(Cells.createContentNoBorder(ineligibilityText));
                        });

                        table.addCell(Cells.createContent(cohortSubTable));
                        table.addCell(Cells.createContent(ineligibilitySubTable));
                    }
                });

        if (trials.values().stream().flatMap(Collection::stream).anyMatch(trial -> trial.isOpen() && !trial.hasSlotsAvailable())) {
            table.addCell(Cells.createSpanningSubNote(" * Cohort currently has no slots available", table));
        }

        return Tables.makeWrapping(table);
    }
}
