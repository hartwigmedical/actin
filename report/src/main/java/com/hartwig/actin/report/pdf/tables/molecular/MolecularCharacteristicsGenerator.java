package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
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
        float colWidth = width / 7;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth, colWidth, colWidth, colWidth, colWidth);

        table.addHeaderCell(Cells.createHeader("Purity"));
        table.addHeaderCell(Cells.createHeader("Reliable Quality"));
        table.addHeaderCell(Cells.createHeader("Predicted tumor origin"));
        table.addHeaderCell(Cells.createHeader("TML Status"));
        table.addHeaderCell(Cells.createHeader("MS Stability"));
        table.addHeaderCell(Cells.createHeader("HR Status"));
        table.addHeaderCell(Cells.createHeader("DPYD"));

        table.addCell(createPurityCell(molecular.characteristics().purity()));
        table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(molecular.hasReliableQuality())));
        table.addCell(Cells.createContent(TumorOriginInterpreter.interpret(molecular.characteristics().predictedTumorOrigin())));
        table.addCell(Cells.createContent(createTMLStatusString(molecular.characteristics().tumorMutationalLoad())));
        table.addCell(createMSStabilityCell(molecular.characteristics().isMicrosatelliteUnstable()));
        table.addCell(createHRStatusCell(molecular.characteristics().isHomologousRepairDeficient()));
        table.addCell(Cells.createContent(createDPYDString(molecular.pharmaco())));

        return table;
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
    private static String createTMLStatusString(@Nullable Integer tumorMutationalLoad) {
        if (tumorMutationalLoad == null) {
            return Formats.VALUE_UNKNOWN;
        }

        String interpretation = tumorMutationalLoad >= 140 ? "High" : "Low";
        return interpretation + " (" + tumorMutationalLoad + ")";
    }

    @NotNull
    private static Cell createMSStabilityCell(@Nullable Boolean isMicrosatelliteUnstable) {
        if (isMicrosatelliteUnstable == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String status = isMicrosatelliteUnstable ? "Unstable" : "Stable";
        return Cells.createContent(status);
    }

    @NotNull
    private static Cell createHRStatusCell(@Nullable Boolean isHomologousRepairDeficient) {
        if (isHomologousRepairDeficient == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN);
        }

        String status = isHomologousRepairDeficient ? "Deficient" : "Proficient";
        return Cells.createContent(status);
    }

    @NotNull
    private static String createDPYDString(@NotNull Set<PharmacoEntry> pharmaco) {
        PharmacoEntry dpyd = findPharmacoEntry(pharmaco, "DPYD");
        return dpyd != null ? dpyd.haplotype() : Formats.VALUE_UNKNOWN;
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
