package com.hartwig.actin.report.pdf.tables.treatment;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.EvaluatedCohortComparator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;

public final class ActinTrialGeneratorFunctions {

    public static Stream<List<EvaluatedCohort>> streamSortedCohorts(List<EvaluatedCohort> trials) {
        trials.sort(new EvaluatedCohortComparator());
        Map<String, List<EvaluatedCohort>> cohortsByTrialId = trials.stream().collect(groupingBy(EvaluatedCohort::trialId));

        return trials.stream().map(EvaluatedCohort::trialId).distinct().map(cohortsByTrialId::get);
    }

    public static String createCohortString(EvaluatedCohort cohort) {
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

    public static void insertTrialRow(List<EvaluatedCohort> cohortList, Table table, Table trialSubTable) {
        EvaluatedCohort trial = cohortList.get(0);
        if (trial != null) {
            table.addCell(Cells.createContent(Cells.createContentNoBorder(new Paragraph().addAll(Arrays.asList(
                    new Text(trial.trialId()).addStyle(Styles.tableHighlightStyle()),
                    new Text(trial.acronym()).addStyle(Styles.tableContentStyle())
            )))));
            if (trialSubTable.getNumberOfRows() > 2) {
                trialSubTable = Tables.makeWrapping(trialSubTable, false);
            } else {
                trialSubTable.setKeepTogether(true);
            }
            table.addCell(Cells.createContent(trialSubTable));
        }
    }
}
