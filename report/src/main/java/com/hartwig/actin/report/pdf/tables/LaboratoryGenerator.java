package com.hartwig.actin.report.pdf.tables;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpretationFactory;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaboratoryGenerator implements TableGenerator {

    @NotNull
    private final LabInterpretation labInterpretation;
    private final float key1Width;
    private final float key2Width;
    private final float valueWidth;

    @NotNull
    public static LaboratoryGenerator fromRecord(@NotNull ClinicalRecord record, float keyWidth, float valueWidth) {
        float key1Width = keyWidth / 3;
        float key2Width = keyWidth - key1Width;

        return new LaboratoryGenerator(LabInterpretationFactory.fromLabValues(record.labValues()), key1Width, key2Width, valueWidth);
    }

    private LaboratoryGenerator(@NotNull final LabInterpretation labInterpretation, final float key1Width, final float key2Width,
            final float valueWidth) {
        this.labInterpretation = labInterpretation;
        this.key1Width = key1Width;
        this.key2Width = key2Width;
        this.valueWidth = valueWidth;
    }

    @NotNull
    @Override
    public String title() {
        return "Laboratory (" + Formats.date(labInterpretation.mostRecentRelevantDate()) + ")";
    }

    @NotNull
    @Override
    public Table contents() {
        Table table = Tables.createFixedWidthCols(new float[] { key1Width, key2Width, valueWidth });

        table.addCell(Cells.createKey("Liver function"));
        addMostRecentLabEntryByName(table, "Total bilirubin");
        table.addCell(Cells.createEmpty());
        addMostRecentLabEntryByCode(table, "ASAT");
        table.addCell(Cells.createEmpty());
        addMostRecentLabEntryByCode(table, "ALAT");
        table.addCell(Cells.createEmpty());
        addMostRecentLabEntryByCode(table, "ALP");
        table.addCell(Cells.createEmpty());
        addMostRecentLabEntryByName(table, "Albumin");

        table.addCell(Cells.createKey("Kidney function"));
        addMostRecentLabEntryByName(table, "Creatinine");
        table.addCell(Cells.createEmpty());
        addMostRecentLabEntryByName(table, "CKD-EPI eGFR");

        table.addCell(Cells.createKey("Hemoglobin"));
        addMostRecentLabEntryByName(table, "Hemoglobin", false);

        table.addCell(Cells.createKey("Thrombocytes"));
        addMostRecentLabEntryByName(table, "Thrombocytes", false);

        table.addCell(Cells.createKey("PT"));
        addMostRecentLabEntryByCode(table, "PT", false);

        table.addCell(Cells.createKey("INR"));
        addMostRecentLabEntryByCode(table, "INR", false);

        return table;
    }

    private void addMostRecentLabEntryByName(@NotNull Table table, @NotNull String name) {
        addMostRecentLabEntryByName(table, name, true);
    }

    private void addMostRecentLabEntryByName(@NotNull Table table, @NotNull String name, boolean displayHeader) {
        addLabEntry(table, displayHeader ? name : Strings.EMPTY, labInterpretation.mostRecentByName(name));
    }

    private void addMostRecentLabEntryByCode(@NotNull Table table, @NotNull String code) {
        addMostRecentLabEntryByCode(table, code, true);
    }

    private void addMostRecentLabEntryByCode(@NotNull Table table, @NotNull String code, boolean displayHeader) {
        addLabEntry(table, displayHeader ? code : Strings.EMPTY, labInterpretation.mostRecentByCode(code));
    }

    private void addLabEntry(@NotNull Table table, @NotNull String header, @Nullable LabValue lab) {
        String key = header;
        String value = Strings.EMPTY;

        Style style = Styles.tableValueHighlightStyle();
        if (lab != null) {
            if (key.isEmpty()) {
                key = limitAddition(lab);
            } else {
                key = key + " " + limitAddition(lab);
            }

            value = lab.value() + " " + lab.unit();
            if (labInterpretation.mostRecentRelevantDate().isAfter(lab.date())) {
                value = value + " (" + Formats.date(lab.date()) + ")";
            }

            if (lab.isOutsideRef() != null && lab.isOutsideRef()) {
                style = Styles.tableValueWarnStyle();
                String outOfRangeAddition = outOfRangeAddition(labInterpretation.allValuesSortedDescending(lab));
                if (!outOfRangeAddition.isEmpty()) {
                    value = value + " " + outOfRangeAddition;
                }
            }
        }

        table.addCell(Cells.createKey(key));
        table.addCell(Cells.create(new Paragraph(value).addStyle(style)));
    }

    @NotNull
    private static String limitAddition(@NotNull LabValue lab) {
        Double refLimitLow = lab.refLimitLow();
        Double refLimitUp = lab.refLimitUp();

        if (refLimitLow == null && refLimitUp == null) {
            return Strings.EMPTY;
        }

        String limit;
        if (refLimitLow == null) {
            limit = "< " + refLimitUp;
        } else if (refLimitUp == null) {
            limit = "> " + refLimitLow;
        } else {
            limit = refLimitLow + " - " + refLimitUp;
        }

        return "(" + limit + " " + lab.unit() + ")";
    }

    @NotNull
    private static String outOfRangeAddition(@NotNull List<LabValue> values) {
        assert values.get(0).isOutsideRef();

        int outOfRangeCount = 1;
        double moreRecentValue = values.get(0).value();
        boolean trendIsUp = true;
        boolean trendIsDown = true;

        boolean inOutOfRefChain = true;
        int index = 1;
        while (inOutOfRefChain && index < values.size()) {
            LabValue lab = values.get(index);

            if (lab.isOutsideRef()) {
                outOfRangeCount++;
                if (moreRecentValue > lab.value()) {
                    trendIsDown = false;
                } else {
                    trendIsUp = false;
                }
                moreRecentValue = lab.value();
            } else {
                inOutOfRefChain = false;
            }

            index++;
        }

        if (outOfRangeCount > 1) {
            assert !(trendIsUp && trendIsDown);

            String trend;
            if (trendIsUp) {
                trend = "up";
            } else if (trendIsDown) {
                trend = "down";
            } else {
                trend = "unknown";
            }

            return "out of range for " + outOfRangeCount + " cons. measurements, trend is " + trend;
        } else {
            return Strings.EMPTY;
        }
    }
}
