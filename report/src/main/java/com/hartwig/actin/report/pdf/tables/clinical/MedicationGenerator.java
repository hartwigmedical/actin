package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class MedicationGenerator implements TableGenerator {

    @NotNull
    private final List<Medication> medications;
    private final float totalWidth;

    public MedicationGenerator(@NotNull final List<Medication> medications, final float totalWidth) {
        this.medications = medications;
        this.totalWidth = totalWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Medication details";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(1, 1, 1, 1, 1, 1, 1).setWidth(totalWidth);

        table.addHeaderCell(Cells.createHeader("Medication"));
        table.addHeaderCell(Cells.createHeader("Categories"));
        table.addHeaderCell(Cells.createHeader("Start date"));
        table.addHeaderCell(Cells.createHeader("Stop date"));
        table.addHeaderCell(Cells.createHeader("Active?"));
        table.addHeaderCell(Cells.createHeader("Dosage"));
        table.addHeaderCell(Cells.createHeader("Frequency"));

        for (Medication medication : medications) {
            table.addCell(Cells.createContent(medication.name()));
            table.addCell(Cells.createContent(concat(medication.categories())));
            table.addCell(Cells.createContent(Formats.date(medication.startDate(), Strings.EMPTY)));
            table.addCell(Cells.createContent(Formats.date(medication.stopDate(), Strings.EMPTY)));
            table.addCell(Cells.createContent(Formats.yesNoUnknown(medication.active())));
            table.addCell(Cells.createContent(dosage(medication)));
            table.addCell(Cells.createContent(frequency(medication)));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String dosage(@NotNull  Medication medication) {
        String dosageMin = medication.dosageMin() != null ? Formats.number(medication.dosageMin()) : "?";
        String dosageMax = medication.dosageMax() != null ? Formats.number(medication.dosageMax()) : "?";

        String result = dosageMin.equals(dosageMax) ? dosageMin : dosageMin + " - " + dosageMax;
        if (medication.dosageUnit() != null) {
            result += (" " + medication.dosageUnit());
        }
        return result;
    }

    @NotNull
    private static String frequency(@NotNull  Medication medication) {
        String result = medication.frequency() != null ? Formats.number(medication.frequency()) : "?";
        if (medication.frequencyUnit() != null) {
            result += (" / " + medication.frequencyUnit());
        }
        return result;
    }

    @NotNull
    private static String concat(@NotNull Set<String> categories) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String category : categories) {
            joiner.add(category);
        }
        return Formats.valueOrDefault(joiner.toString(), "None");
    }
}
