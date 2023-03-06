package com.hartwig.actin.report.pdf.chapters;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.tables.treatment.EligibleActinTrialsGenerator;
import com.hartwig.actin.report.pdf.tables.treatment.IneligibleActinTrialsGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class TrialMatchingChapter implements ReportChapter {

    @NotNull
    private final Report report;
    private final boolean skipTrialMatchingDetails;

    public TrialMatchingChapter(@NotNull final Report report, final boolean skipTrialMatchingDetails) {
        this.report = report;
        this.skipTrialMatchingDetails = skipTrialMatchingDetails;
    }

    @NotNull
    @Override
    public String name() {
        return "Trial Matching Summary";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull Document document) {
        addChapterTitle(document);
        addTrialMatchingOverview(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addTrialMatchingOverview(@NotNull Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());

        List<EvaluatedCohort> trials = EvaluatedCohortFactory.create(report.treatmentMatch());
        List<TableGenerator> generators = Lists.newArrayList(
                EligibleActinTrialsGenerator.forClosedTrials(trials, contentWidth(), skipTrialMatchingDetails),
                IneligibleActinTrialsGenerator.fromEvaluatedTrials(trials, contentWidth(), skipTrialMatchingDetails)
        );

        for (int i = 0; i < generators.size(); i++) {
            TableGenerator generator = generators.get(i);
            table.addCell(Cells.createTitle(generator.title()));
            table.addCell(Cells.create(generator.contents()));
            if (i < generators.size() - 1) {
                table.addCell(Cells.createEmpty());
            }
        }

        document.add(table);
    }
}
