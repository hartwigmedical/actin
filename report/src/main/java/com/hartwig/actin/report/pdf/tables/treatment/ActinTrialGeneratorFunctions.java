package com.hartwig.actin.report.pdf.tables.treatment;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvaluatedTrialComparator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

public final class ActinTrialGeneratorFunctions {

    public static Stream<List<EvaluatedTrial>> streamSortedCohorts(List<EvaluatedTrial> trials) {
        trials.sort(new EvaluatedTrialComparator());
        Map<String, List<EvaluatedTrial>> cohortsByTrialId = trials.stream().collect(groupingBy(EvaluatedTrial::trialId));

        return trials.stream().map(EvaluatedTrial::trialId).distinct().map(cohortsByTrialId::get);
    }

    public static String createCohortString(EvaluatedTrial cohort) {
        String cohortText = cohort.cohort() == null ? "" : cohort.cohort();
        if (cohort.isOpen() && !cohort.hasSlotsAvailable()) {
            cohortText += " *";
        }
        return cohortText;
    }

    public static void addContentStreamToTable(Stream<String> cellContent, boolean deemphasizeContent, Table table) {
        cellContent.map(text -> {
            if (deemphasizeContent) {
                return Cells.createContentNoBorderDeemphasize(text);
            } else {
                return Cells.createContentNoBorder(text);
            }
        }).forEach(table::addCell);
    }

    public static void insertTrialRow(List<EvaluatedTrial> cohortList, Table table, Table trialSubTable) {
        EvaluatedTrial trial = cohortList.get(0);
        if (trial != null) {
            table.addCell(Cells.createContent(Cells.createContentNoBorder(new Paragraph().addAll(Arrays.asList(new Text(trial.trialId()).addStyle(
                    Styles.tableHighlightStyle()), new Text(trial.acronym()).addStyle(Styles.tableContentStyle()))))));
            table.addCell(Cells.createContent(trialSubTable));
        }
    }
}
