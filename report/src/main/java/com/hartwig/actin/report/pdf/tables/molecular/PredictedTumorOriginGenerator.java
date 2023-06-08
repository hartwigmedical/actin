package com.hartwig.actin.report.pdf.tables.molecular;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class PredictedTumorOriginGenerator implements TableGenerator {

    public static final int PADDING_LEFT = 12;
    @NotNull
    private final MolecularRecord molecular;
    private final float width;

    public PredictedTumorOriginGenerator(@NotNull final MolecularRecord molecular, final float width) {
        this.molecular = molecular;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "Predicted tumor origin";
    }

    @NotNull
    @Override
    public Table contents() {
        PredictedTumorOrigin predictedTumorOrigin = molecular.characteristics().predictedTumorOrigin();
        List<CuppaPrediction> predictions = TumorOriginInterpreter.predictionsToDisplay(predictedTumorOrigin);

        if (predictions.isEmpty() || predictedTumorOrigin == null) {
            String message = predictedTumorOrigin == null
                    ? Formats.VALUE_UNKNOWN
                    : String.format("All tumor cohorts have a prediction lower than 10%%. Highest prediction: %s (%s)",
                            predictedTumorOrigin.tumorType(),
                            Formats.percentage(predictedTumorOrigin.likelihood()));
            return Tables.createSingleColWithWidth(width).addCell(Cells.createContentNoBorder(message));
        } else {
            int numColumns = predictions.size() + 1;
            float[] widths = new float[numColumns];
            Arrays.fill(widths, width / numColumns);

            Table table = new Table(widths);

            table.addHeaderCell(Cells.createEmpty());
            IntStream.range(0, predictions.size())
                    .forEach(i -> table.addHeaderCell(Cells.createHeader(String.format("%d. %s", i + 1, predictions.get(i).cancerType()))));

            table.addCell(Cells.createContentBold("Combined prediction score"));
            predictions.stream().map(p -> Cells.createContentBold(Formats.percentage(p.likelihood()))).forEach(table::addCell);

            table.addCell(Cells.createContent("This score is calculated by combining information on:"));
            predictions.stream().map(p -> Cells.createContent("")).forEach(table::addCell);

            addCellWithPadding("(1) SNV types", table);
            addClassifierCells(predictions, CuppaPrediction::snvPairwiseClassifier, table);
            addCellWithPadding("(2) SNV genomic localisation distribution", table);
            addClassifierCells(predictions, CuppaPrediction::genomicPositionClassifier, table);
            addCellWithPadding("(3) Driver genes and passenger characteristics", table);
            addClassifierCells(predictions, CuppaPrediction::featureClassifier, table);

            table.addCell(Cells.createSpanningSubNote(String.format("Other cohorts have a combined prediction of %s or lower",
                    Formats.percentage(TumorOriginInterpreter.greatestOmittedLikelihood(predictedTumorOrigin))), table));

            return table;
        }
    }

    private static void addCellWithPadding(String text, Table table) {
        table.addCell(Cells.createContent(text).setPaddingLeft(PADDING_LEFT));
    }

    private static void addClassifierCells(List<CuppaPrediction> predictions, Function<CuppaPrediction, Double> classifierFunction,
            Table table) {
        predictions.stream()
                .map(classifierFunction)
                .map(v -> v == null ? Formats.VALUE_UNKNOWN : Formats.percentage(v))
                .map(Cells::createContent)
                .forEach(table::addCell);
    }
}
