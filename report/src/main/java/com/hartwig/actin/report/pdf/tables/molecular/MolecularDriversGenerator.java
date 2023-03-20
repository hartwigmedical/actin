package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.report.interpretation.ClonalityInterpreter;
import com.hartwig.actin.report.interpretation.EvaluatedCohort;
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory;
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.hartwig.actin.treatment.TreatmentConstants;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularDriversGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord molecular;
    @NotNull
    private final List<EvaluatedCohort> cohorts;
    private final float width;

    public MolecularDriversGenerator(@NotNull final MolecularRecord molecular, @NotNull final List<EvaluatedCohort> cohorts,
            final float width) {
        this.molecular = molecular;
        this.cohorts = cohorts;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "Drivers";
    }

    @NotNull
    @Override
    public Table contents() {
        float colWidth = width / 8;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth * 2, colWidth, colWidth, colWidth, colWidth, colWidth);

        table.addHeaderCell(Cells.createHeader("Type"));
        table.addHeaderCell(Cells.createHeader("Driver"));
        table.addHeaderCell(Cells.createHeader("Driver likelihood"));
        table.addHeaderCell(Cells.createHeader("Trials in " + TreatmentConstants.ACTIN_SOURCE));
        table.addHeaderCell(Cells.createHeader("Trials in " + molecular.externalTrialSource()));
        table.addHeaderCell(Cells.createHeader("Best evidence in " + molecular.evidenceSource()));
        table.addHeaderCell(Cells.createHeader("Resistance in " + molecular.evidenceSource()));

        MolecularDriversInterpreter molecularDriversInterpreter =
                MolecularDriversInterpreter.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers(), cohorts);
        MolecularDriverEntryFactory factory = new MolecularDriverEntryFactory(molecularDriversInterpreter);

        factory.create().forEach(entry -> {
            table.addCell(Cells.createContent(entry.driverType()));
            table.addCell(Cells.createContent(entry.driver()));
            table.addCell(Cells.createContent(formatDriverLikelihood(entry.driverLikelihood())));
            table.addCell(Cells.createContent(concat(entry.actinTrials())));
            table.addCell(Cells.createContent(concat(entry.externalTrials())));
            table.addCell(Cells.createContent(nullToEmpty(entry.bestResponsiveEvidence())));
            table.addCell(Cells.createContent(nullToEmpty(entry.bestResistanceEvidence())));
        });

        if (molecularDriversInterpreter.hasPotentiallySubClonalVariants()) {
            String note = "* Variant has > " + Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF) + " likelihood of being sub-clonal";
            table.addCell(Cells.createSpanningSubNote(note, table));
        }

        return Tables.makeWrapping(table);
    }

    @NotNull
    private static String formatDriverLikelihood(@Nullable DriverLikelihood driverLikelihood) {
        return Optional.ofNullable(driverLikelihood).map(DriverLikelihood::toString).orElse(Formats.VALUE_UNKNOWN);
    }

    @NotNull
    private static String concat(@NotNull Set<String> treatments) {
        if (treatments.isEmpty()) {
            return Strings.EMPTY;
        }
        StringJoiner joiner = Formats.commaJoiner();
        for (String treatment : treatments) {
            joiner.add(treatment);
        }
        return joiner.toString();
    }

    @NotNull
    private static String nullToEmpty(@Nullable String string) {
        return string != null ? string : Strings.EMPTY;
    }
}
