package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
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

    //TODO: Show selection of content in case of WGS QC fail
    @NotNull
    @Override
    public Table contents() {
        float colWidth = width / 12;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth * 3, colWidth, colWidth, colWidth, colWidth, colWidth * 3);

        table.addHeaderCell(Cells.createHeader("Purity"));
        table.addHeaderCell(Cells.createHeader("Reliable Quality"));
        table.addHeaderCell(Cells.createHeader("Predicted tumor origin"));
        table.addHeaderCell(Cells.createHeader("TML Status"));
        table.addHeaderCell(Cells.createHeader("TMB Status"));
        table.addHeaderCell(Cells.createHeader("MS Stability"));
        table.addHeaderCell(Cells.createHeader("HR Status"));
        table.addHeaderCell(Cells.createHeader("DPYD"));

        table.addCell(createPurityCell(molecular.characteristics().purity()));
        table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(molecular.hasSufficientQuality())));
        table.addCell(createPredictedTumorOriginCell(molecular.characteristics().predictedTumorOrigin()));
        table.addCell(createTMLStatusCell(molecular.characteristics().tumorMutationalLoad()));
        table.addCell(createTMBStatusCell(molecular.characteristics().tumorMutationalBurden()));
        table.addCell(createMSStabilityCell(molecular.characteristics().isMicrosatelliteUnstable()));
        table.addCell(createHRStatusCell(molecular.characteristics().isHomologousRepairDeficient()));
        table.addCell(Cells.createContent(createDPYDString(molecular.pharmaco())));

        return table;
    }

    @NotNull
    private static Cell createPredictedTumorOriginCell(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        String interpretation = TumorOriginInterpreter.interpret(predictedTumorOrigin);
        if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin)) {
            return Cells.createContent(interpretation);
        } else {
            return Cells.createContentWarn(interpretation);
        }
    }

    @NotNull
    private static Cell createPurityCell(@Nullable Double purity) {
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
    private static Cell createTMLStatusCell(@Nullable Integer tumorMutationalLoad) {
        if (tumorMutationalLoad == null) {
            return Cells.createValueWarn(Formats.VALUE_UNKNOWN);
        }

        String interpretation = tumorMutationalLoad >= 140 ? "High" : "Low";
        Cell cell = Cells.createContent(interpretation + " (" + tumorMutationalLoad + ")");
        if (interpretation.equals("High")) {
            cell.addStyle(Styles.tableHighlightStyle());
        }
        return cell;
    }

    @NotNull
    private static Cell createTMBStatusCell(@Nullable Double tumorMutationalBurden) {
        if (tumorMutationalBurden == null) {
            return Cells.createValueWarn(Formats.VALUE_UNKNOWN);
        }

        String interpretation = tumorMutationalBurden >= 10 ? "High" : "Low";
        Cell cell = Cells.createContent(interpretation + " (" + Formats.singleDigitNumber(tumorMutationalBurden) + ")");
        if (interpretation.equals("High")) {
            cell.addStyle(Styles.tableHighlightStyle());
        }
        return cell;
    }

    @NotNull
    private static Cell createMSStabilityCell(@Nullable Boolean isMicrosatelliteUnstable) {
        if (isMicrosatelliteUnstable == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String status = isMicrosatelliteUnstable ? "Unstable" : "Stable";
        Cell cell = Cells.createContent(status);
        if (isMicrosatelliteUnstable) {
            cell.addStyle(Styles.tableHighlightStyle());
        }
        return cell;
    }

    @NotNull
    private static Cell createHRStatusCell(@Nullable Boolean isHomologousRepairDeficient) {
        if (isHomologousRepairDeficient == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String status = isHomologousRepairDeficient ? "Deficient" : "Proficient";
        Cell cell = Cells.createContent(status);
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
