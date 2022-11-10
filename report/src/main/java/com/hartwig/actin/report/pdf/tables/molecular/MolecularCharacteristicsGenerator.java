package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularCharacteristicsGenerator implements TableGenerator {

    private static final String VALUE_NOT_AVAILABLE = "N/A";

    @NotNull
    private final MolecularRecord molecular;
    private final float width;

    public MolecularCharacteristicsGenerator(@NotNull final MolecularRecord molecular, final float width) {
        this.molecular = molecular;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "General";
    }

    @NotNull
    @Override
    public Table contents() {
        float colWidth = width / 12;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth * 3, colWidth, colWidth, colWidth, colWidth, colWidth * 3);

        table.addHeaderCell(Cells.createHeader("Purity"));
        table.addHeaderCell(Cells.createHeader("Sufficient Quality"));
        table.addHeaderCell(Cells.createHeader("Predicted tumor origin"));
        table.addHeaderCell(Cells.createHeader("TML Status"));
        table.addHeaderCell(Cells.createHeader("TMB Status"));
        table.addHeaderCell(Cells.createHeader("MS Stability"));
        table.addHeaderCell(Cells.createHeader("HR Status"));
        table.addHeaderCell(Cells.createHeader("DPYD"));

        MolecularCharacteristics characteristics = molecular.characteristics();
        table.addCell(createPurityCell(characteristics.purity()));
        table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(molecular.hasSufficientQuality())));
        table.addCell(createPredictedTumorOriginCell(characteristics.predictedTumorOrigin()));
        table.addCell(createTMLStatusCell(characteristics.hasHighTumorMutationalLoad(), characteristics.tumorMutationalLoad()));
        table.addCell(createTMBStatusCell(characteristics.hasHighTumorMutationalBurden(), characteristics.tumorMutationalBurden()));
        table.addCell(createMSStabilityCell(characteristics.isMicrosatelliteUnstable()));
        table.addCell(createHRStatusCell(characteristics.isHomologousRepairDeficient()));
        table.addCell(Cells.createContent(createDPYDString(molecular.pharmaco())));

        return table;
    }

    @NotNull
    private Cell createPurityCell(@Nullable Double purity) {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn("None");
        }

        if (purity == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String purityString = Formats.percentage(purity);
        if (purity < 0.2) {
            return Cells.createContentWarn(purityString);
        } else {
            return Cells.createContent(purityString);
        }
    }

    @NotNull
    private Cell createPredictedTumorOriginCell(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(VALUE_NOT_AVAILABLE);
        }

        String interpretation = TumorOriginInterpreter.interpret(predictedTumorOrigin);
        if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin) && molecular.hasSufficientQuality()) {
            return Cells.createContent(interpretation);
        } else {
            return Cells.createContentWarn(interpretation);
        }
    }

    @NotNull
    private Cell createTMLStatusCell(@Nullable Boolean hasHighTumorMutationalLoad, @Nullable Integer tumorMutationalLoad) {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(VALUE_NOT_AVAILABLE);
        }

        if (hasHighTumorMutationalLoad == null || tumorMutationalLoad == null) {
            return Cells.createValueWarn(Formats.VALUE_UNKNOWN);
        }

        String interpretation = hasHighTumorMutationalLoad ? "High" : "Low";
        String value = interpretation + " (" + tumorMutationalLoad + ")";
        Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(value) : Cells.createContentWarn(value);

        if (hasHighTumorMutationalLoad) {
            cell.addStyle(Styles.tableHighlightStyle());
        }

        return cell;
    }

    @NotNull
    private Cell createTMBStatusCell(@Nullable Boolean hasHighTumorMutationalBurden, @Nullable Double tumorMutationalBurden) {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(VALUE_NOT_AVAILABLE);
        }

        if (hasHighTumorMutationalBurden == null || tumorMutationalBurden == null) {
            return Cells.createValueWarn(Formats.VALUE_UNKNOWN);
        }

        String interpretation = hasHighTumorMutationalBurden ? "High" : "Low";
        String value = interpretation + " (" + Formats.singleDigitNumber(tumorMutationalBurden) + ")";
        Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(value) : Cells.createContentWarn(value);

        if (hasHighTumorMutationalBurden) {
            cell.addStyle(Styles.tableHighlightStyle());
        }

        return cell;
    }

    @NotNull
    private Cell createMSStabilityCell(@Nullable Boolean isMicrosatelliteUnstable) {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(VALUE_NOT_AVAILABLE);
        }

        if (isMicrosatelliteUnstable == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String status = isMicrosatelliteUnstable ? "Unstable" : "Stable";
        Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(status) : Cells.createContentWarn(status);

        if (isMicrosatelliteUnstable) {
            cell.addStyle(Styles.tableHighlightStyle());
        }

        return cell;
    }

    @NotNull
    private Cell createHRStatusCell(@Nullable Boolean isHomologousRepairDeficient) {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(VALUE_NOT_AVAILABLE);
        }

        if (isHomologousRepairDeficient == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String status = isHomologousRepairDeficient ? "Deficient" : "Proficient";
        Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(status) : Cells.createContentWarn(status);

        if (isHomologousRepairDeficient) {
            cell.addStyle(Styles.tableHighlightStyle());
        }

        return cell;
    }

    @NotNull
    private static String createDPYDString(@NotNull Set<PharmacoEntry> pharmaco) {
        PharmacoEntry dpyd = findPharmacoEntry(pharmaco, "DPYD");
        if (dpyd == null) {
            return Formats.VALUE_UNKNOWN;
        }

        StringJoiner joiner = Formats.commaJoiner();
        for (Haplotype haplotype : dpyd.haplotypes()) {
            joiner.add(haplotype.name() + " (" + haplotype.function() + ")");
        }
        return joiner.toString();
    }

    @Nullable
    private static PharmacoEntry findPharmacoEntry(@NotNull Set<PharmacoEntry> pharmaco, @NotNull String geneToFind) {
        for (PharmacoEntry entry : pharmaco) {
            if (entry.gene().equals(geneToFind)) {
                return entry;
            }
        }

        return null;
    }
}
