package com.hartwig.actin.report.pdf.chapters;

import static com.hartwig.actin.report.pdf.util.Formats.STANDARD_KEY_WIDTH;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.MolecularCharacteristicsGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.MolecularDriversGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.PredictedTumorOriginGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.PriorMolecularResultGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class MolecularDetailsChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public MolecularDetailsChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Molecular Details";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4.rotate();
    }

    @Override
    public void render(@NotNull final Document document) {
        addChapterTitle(document);
        addMolecularDetails(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addMolecularDetails(@NotNull Document document) {
        float keyWidth = STANDARD_KEY_WIDTH;
        PriorMolecularResultGenerator priorMolecularResultGenerator =
                new PriorMolecularResultGenerator(report.clinical(), keyWidth, contentWidth() - keyWidth - 10);
        Table priorMolecularResults = priorMolecularResultGenerator.contents().setBorder(Border.NO_BORDER);
        document.add(priorMolecularResults);

        Table table = Tables.createSingleColWithWidth(contentWidth());

        table.addCell(Cells.createEmpty());
        table.addCell(Cells.createTitle(String.format("%s (%s, %s)",
                report.molecular().type(),
                report.molecular().sampleId(),
                Formats.date(report.molecular().date()))));
        List<EvaluatedCohort> cohorts = EvaluatedCohortFactory.create(report.treatmentMatch());
        List<TableGenerator> generators = Lists.newArrayList(new MolecularCharacteristicsGenerator(report.molecular(), contentWidth()));
        if (report.molecular().containsTumorCells()) {
            generators.add(new PredictedTumorOriginGenerator(report.molecular(), contentWidth()));
            generators.add(new MolecularDriversGenerator(report.molecular(), cohorts, contentWidth()));
        }

        for (int i = 0; i < generators.size(); i++) {
            TableGenerator generator = generators.get(i);
            table.addCell(Cells.createSubTitle(generator.title()));
            table.addCell(Cells.create(generator.contents()));
            if (i < generators.size() - 1) {
                table.addCell(Cells.createEmpty());
            }
        }

        if (!report.molecular().containsTumorCells()) {
            table.addCell(Cells.createContent("No successful WGS could be performed on the submitted biopsy"));
        }

        document.add(table);
    }
}
