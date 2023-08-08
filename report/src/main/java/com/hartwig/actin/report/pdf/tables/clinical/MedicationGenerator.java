package com.hartwig.actin.report.pdf.tables.clinical;

import java.util.List;
import java.util.Optional;

import com.hartwig.actin.clinical.datamodel.Dosage;
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
        return "Active medication details";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(1, 1, 1, 1, 1, 1).setWidth(totalWidth);

        table.addHeaderCell(Cells.createHeader("Medication"));
        table.addHeaderCell(Cells.createHeader("Administration route"));
        table.addHeaderCell(Cells.createHeader("Start date"));
        table.addHeaderCell(Cells.createHeader("Stop date"));
        table.addHeaderCell(Cells.createHeader("Dosage"));
        table.addHeaderCell(Cells.createHeader("Frequency"));

        medications.stream()
                .distinct()
                .filter(medication -> Optional.ofNullable(medication.status())
                        .map(status -> status.display().equals("Active") || status.display().equals("Planned"))
                        .orElse(false))
                .forEach(medication -> {
                    table.addCell(Cells.createContent(medication.name()));
                    table.addCell(Cells.createContent(administrationRoute(medication)));
                    table.addCell(Cells.createContent(Formats.date(medication.startDate(), Strings.EMPTY)));
                    table.addCell(Cells.createContent(Formats.date(medication.stopDate(), Strings.EMPTY)));
                    table.addCell(Cells.createContent(dosage(medication)));
                    table.addCell(Cells.createContent(frequency(medication.dosage())));
                });

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String administrationRoute(@NotNull Medication medication) {
        return medication.administrationRoute() != null ? medication.administrationRoute() : "";
    }

    @NotNull
    private static String dosage(@NotNull Medication medication) {
        Dosage dosage = medication.dosage();

        String dosageMin = "";
        String dosageMax = "";
        if (medication.dosage().dosageMin() == 0.0) {
            dosageMin = "?";
            dosageMax = "?";
        } else if (dosage.dosageMin() != 0.0 || dosage.dosageMin() != null) {
            dosageMin = Formats.twoDigitNumber(dosage.dosageMin());
            dosageMax = Formats.twoDigitNumber(dosage.dosageMax());
        }

        String result = dosageMin.equals(dosageMax) ? dosageMin : dosageMin + " - " + dosageMax;
        Boolean ifNeeded = dosage.ifNeeded();
        if (ifNeeded != null && ifNeeded) {
            result = "if needed " + result;
        }

        if (dosage.dosageUnit() != null) {
            result += (" " + dosage.dosageUnit());
        }

        if (("cutaneous".equals(medication.administrationRoute()) || "intravenous".equals(medication.administrationRoute()))
                && dosage.dosageMin() == 0.0) {
            result = "";
        }

        return result;
    }

    @NotNull
    private static String frequency(@NotNull Dosage dosage) {
        String result = "";
        if (dosage.frequency() == 0.0) {
            result = "?";
        } else if (dosage.frequency() != 0.0 || dosage.frequency() != null) {
            result = Formats.twoDigitNumber(dosage.frequency());
        }

        if (dosage.periodBetweenUnit() != null) {
            result += (" / " + Formats.noDigitNumber(dosage.periodBetweenValue() + 1) + " " + dosage.periodBetweenUnit());
        } else if (dosage.frequencyUnit() != null) {
            result += (" / " + dosage.frequencyUnit());
        }

        return result;
    }
}
