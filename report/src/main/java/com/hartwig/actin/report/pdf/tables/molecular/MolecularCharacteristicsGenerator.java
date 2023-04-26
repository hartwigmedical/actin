package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.List;
import java.util.Optional;
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
        Table table = Tables.createFixedWidthCols(colWidth,
                colWidth,
                colWidth * 2,
                colWidth,
                colWidth,
                colWidth,
                colWidth,
                colWidth * 2,
                colWidth * 2);

        List.of("Purity",
                "Sufficient Quality",
                "Predicted tumor origin",
                "TML Status",
                "TMB Status",
                "MS Stability",
                "HR Status",
                "DPYD",
                "UGT1A1").forEach(title -> table.addHeaderCell(Cells.createHeader(title)));

        MolecularCharacteristics characteristics = molecular.characteristics();
        List.of(createPurityCell(characteristics.purity()),
                Cells.createContentYesNo(Formats.yesNoUnknown(molecular.hasSufficientQuality())),
                createPredictedTumorOriginCell(),
                createTMLStatusCell(),
                createTMBStatusCell(),
                createMSStabilityCell(),
                createHRStatusCell(),
                Cells.createContent(createPeachSummaryForGene(molecular.pharmaco(), "DPYD")),
                Cells.createContent(createPeachSummaryForGene(molecular.pharmaco(), "UGT1A1"))).forEach(table::addCell);

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
    Cell createPredictedTumorOriginCell() {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE);
        }

        PredictedTumorOrigin predictedTumorOrigin = molecular.characteristics().predictedTumorOrigin();
        String interpretation = TumorOriginInterpreter.interpret(predictedTumorOrigin);
        if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin) && molecular.hasSufficientQuality()) {
            return Cells.createContent(interpretation);
        } else {
            return Cells.createContentWarn(interpretation);
        }
    }

    Optional<String> createTMLStatusStringOption() {
        Boolean hasHighTumorMutationalLoad = molecular.characteristics().hasHighTumorMutationalLoad();
        Integer tumorMutationalLoad = molecular.characteristics().tumorMutationalLoad();
        if (hasHighTumorMutationalLoad == null || tumorMutationalLoad == null) {
            return Optional.empty();
        }
        return Optional.of(String.format("%s (%d)", hasHighTumorMutationalLoad ? "High" : "Low", tumorMutationalLoad));
    }

    @NotNull
    private Cell createTMLStatusCell() {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE);
        }

        return createTMLStatusStringOption().map(value -> {
            Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(value) : Cells.createContentWarn(value);

            if (Boolean.TRUE.equals(molecular.characteristics().hasHighTumorMutationalLoad())) {
                cell.addStyle(Styles.tableHighlightStyle());
            }
            return cell;
        }).orElse(Cells.createContentWarn(Formats.VALUE_UNKNOWN));
    }

    @NotNull
    private Cell createTMBStatusCell() {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE);
        }

        Boolean hasHighTumorMutationalBurden = molecular.characteristics().hasHighTumorMutationalBurden();
        Double tumorMutationalBurden = molecular.characteristics().tumorMutationalBurden();
        if (hasHighTumorMutationalBurden == null || tumorMutationalBurden == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String interpretation = hasHighTumorMutationalBurden ? "High" : "Low";
        String value = interpretation + " (" + Formats.singleDigitNumber(tumorMutationalBurden) + ")";
        Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(value) : Cells.createContentWarn(value);

        if (hasHighTumorMutationalBurden) {
            cell.addStyle(Styles.tableHighlightStyle());
        }

        return cell;
    }

    Optional<String> createMSStabilityStringOption() {
        Boolean isMicrosatelliteUnstable = molecular.characteristics().isMicrosatelliteUnstable();
        if (isMicrosatelliteUnstable == null) {
            return Optional.empty();
        }
        return Optional.of(isMicrosatelliteUnstable ? "Unstable" : "Stable");
    }

    @NotNull
    private Cell createMSStabilityCell() {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE);
        }

        return createMSStabilityStringOption().map(value -> {
            Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(value) : Cells.createContentWarn(value);

            if (Boolean.TRUE.equals(molecular.characteristics().isMicrosatelliteUnstable())) {
                cell.addStyle(Styles.tableHighlightStyle());
            }
            return cell;
        }).orElse(Cells.createContentWarn(Formats.VALUE_UNKNOWN));
    }

    Optional<String> createHRStatusStringOption() {
        Boolean isHomologousRepairDeficient = molecular.characteristics().isHomologousRepairDeficient();
        if (isHomologousRepairDeficient == null) {
            return Optional.empty();
        }
        return Optional.of(isHomologousRepairDeficient ? "Deficient" : "Proficient");
    }

    @NotNull
    private Cell createHRStatusCell() {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE);
        }

        return createHRStatusStringOption().map(value -> {
            Cell cell = molecular.hasSufficientQuality() ? Cells.createContent(value) : Cells.createContentWarn(value);

            if (Boolean.TRUE.equals(molecular.characteristics().isHomologousRepairDeficient())) {
                cell.addStyle(Styles.tableHighlightStyle());
            }
            return cell;
        }).orElse(Cells.createContentWarn(Formats.VALUE_UNKNOWN));
    }

    @NotNull
    private static String createPeachSummaryForGene(@NotNull Set<PharmacoEntry> pharmaco, String gene) {
        PharmacoEntry pharmacoEntry = findPharmacoEntry(pharmaco, gene);
        if (pharmacoEntry == null) {
            return Formats.VALUE_UNKNOWN;
        }

        StringJoiner joiner = Formats.commaJoiner();
        for (Haplotype haplotype : pharmacoEntry.haplotypes()) {
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
