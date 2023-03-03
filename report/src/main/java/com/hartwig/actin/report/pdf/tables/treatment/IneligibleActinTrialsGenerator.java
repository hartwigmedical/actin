package com.hartwig.actin.report.pdf.tables.treatment;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Table table = Tables.createFixedWidthCols(trialColWidth, cohortColWidth + ineligibilityReasonColWith);

        if (!trials.isEmpty()) {
            table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Trial")));

            Table headerSubTable = Tables.createFixedWidthCols(cohortColWidth, ineligibilityReasonColWith);
            headerSubTable.addHeaderCell(Cells.createHeader("Cohort"));
            headerSubTable.addHeaderCell(Cells.createHeader("Ineligibility reasons"));

            table.addHeaderCell(Cells.createContentNoBorder(headerSubTable));
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
                        table.addCell(Cells.createContent(Cells.createContentNoBorder(new Paragraph().addAll(Arrays.asList(
                                new Text(trial.trialId()).addStyle(Styles.tableHighlightStyle()),
                                new Text(trial.acronym()).addStyle(Styles.tableContentStyle())
                        )))));

                        Table trialSubTable = Tables.createFixedWidthCols(cohortColWidth, ineligibilityReasonColWith);

                        cohortList.stream().sorted(new EvaluatedTrialComparator()).forEach(cohort -> {
                            String cohortText = cohort.cohort() == null ? "" : cohort.cohort();
                            boolean noSlotsAvailable = cohort.isOpen() && !cohort.hasSlotsAvailable();
                            if (noSlotsAvailable) {
                                cohortText += " *";
                            }
                            String ineligibilityText = cohort.fails().isEmpty() ? "?" : String.join(", ", cohort.fails());

                            Stream.of(cohortText, ineligibilityText).map(text -> {
                                if (!cohort.isOpen()) {
                                    return Cells.createContentNoBorderDeemphasize(text);
                                } else {
                                    return Cells.createContentNoBorder(text);
                                }
                            }).forEach(trialSubTable::addCell);
                        });

                        table.addCell(Cells.createContent(trialSubTable));
                    }
                });

        List<EvaluatedTrial> allCohorts = trials.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        String subNote = "";
        if (allCohorts.stream().anyMatch(trial -> !trial.isOpen())) {
            subNote += " Cohorts shown in grey are closed.";
        }
        if (allCohorts.stream().anyMatch(trial -> trial.isOpen() && !trial.hasSlotsAvailable())) {
            subNote += " Cohorts with no slots available are indicated by an asterisk (*).";
        }
        if (!subNote.isEmpty()) {
            table.addCell(Cells.createSpanningSubNote(subNote, table));
        }

        return Tables.makeWrapping(table);
    }
}
