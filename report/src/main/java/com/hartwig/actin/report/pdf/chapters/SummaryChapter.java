package com.hartwig.actin.report.pdf.chapters;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.datamodel.ActinRecord;
import com.hartwig.actin.report.pdf.tables.LaboratoryTableGenerator;
import com.hartwig.actin.report.pdf.tables.MolecularResultsTableGenerator;
import com.hartwig.actin.report.pdf.tables.PatientClinicalHistoryTableGenerator;
import com.hartwig.actin.report.pdf.tables.PatientCurrentDetailsTableGenerator;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;

import org.jetbrains.annotations.NotNull;

public class SummaryChapter implements ReportChapter {

    @NotNull
    private final ActinRecord record;

    public SummaryChapter(@NotNull final ActinRecord record) {
        this.record = record;
    }

    @NotNull
    @Override
    public String name() {
        return "Summary";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull Document document) {
        addPatientDetails(document);
        addChapterTitle(document);
        addSummaryTable(document);
    }

    private void addPatientDetails(@NotNull Document document) {
        Paragraph patientDetailsLine = new Paragraph();
        patientDetailsLine.add(new Text("Sample ID: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(record.sampleId()).addStyle(Styles.valueStyle()));
        patientDetailsLine.add(new Text(" | Gender: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(record.clinical().patient().gender().display()).addStyle(Styles.valueStyle()));
        patientDetailsLine.add(new Text(" | Birth year: ").addStyle(Styles.labelStyle()));
        patientDetailsLine.add(new Text(String.valueOf(record.clinical().patient().birthYear())).addStyle(Styles.valueStyle()));

        document.add(patientDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addSummaryTable(@NotNull Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());

        float keyWidth = 170;
        float valueWidth = contentWidth() - keyWidth - 10;
        List<TableGenerator> generators =
                Lists.newArrayList(new PatientClinicalHistoryTableGenerator(record.clinical(), keyWidth, valueWidth),
                        new PatientCurrentDetailsTableGenerator(record.clinical(), keyWidth, valueWidth),
                        new LaboratoryTableGenerator(record.clinical(), keyWidth, valueWidth),
                        new MolecularResultsTableGenerator(record.molecular(), keyWidth, valueWidth));

        for (int i = 0; i < generators.size(); i++) {
            addSubTableToMain(table, generators.get(i));
            if (i < generators.size() - 1) {
                table.addCell(Cells.createEmpty());
            }
        }

        document.add(table);
    }

    private static void addSubTableToMain(@NotNull Table mainTable, @NotNull TableGenerator subTableGenerator) {
        mainTable.addCell(Cells.createTitle(subTableGenerator.title()));
        mainTable.addCell(Cells.create(subTableGenerator.contents()));
    }
}
