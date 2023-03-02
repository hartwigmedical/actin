package com.hartwig.actin.report.pdf.chapters;

import static com.hartwig.actin.report.pdf.ReportWriter.STANDARD_KEY_WIDTH;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.LabResultsGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class ClinicalDetailsChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public ClinicalDetailsChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Clinical Details";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull Document document) {
        addChapterTitle(document);
        addClinicalDetails(document);
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addClinicalDetails(final Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());

        float keyWidth = STANDARD_KEY_WIDTH;
        float valueWidth = contentWidth() - keyWidth - 10;
        List<TableGenerator> generators = Lists.newArrayList(new PatientClinicalHistoryGenerator(report.clinical(), keyWidth, valueWidth),
                new PatientCurrentDetailsGenerator(report.clinical(), keyWidth, valueWidth),
                new TumorDetailsGenerator(report.clinical(), keyWidth, valueWidth),
                LabResultsGenerator.fromRecord(report.clinical(), keyWidth, valueWidth),
                new MedicationGenerator(report.clinical().medications(), contentWidth()));

        if (!report.clinical().bloodTransfusions().isEmpty()) {
            generators.add(new BloodTransfusionGenerator(report.clinical().bloodTransfusions(), contentWidth()));
        }

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
