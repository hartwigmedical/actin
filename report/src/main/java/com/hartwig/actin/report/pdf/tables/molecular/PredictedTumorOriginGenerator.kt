package com.hartwig.actin.report.pdf.tables.molecular;

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
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.jetbrains.annotations.NotNull;

public class PredictedTumorOriginGenerator implements TableGenerator {

    private static final int PADDING_LEFT = 20;
    private static final int PADDING_RIGHT = 25;
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

        if (predictions.isEmpty()) {
            String message = predictedTumorOrigin == null
                    ? Formats.VALUE_UNKNOWN
                    : String.format("All tumor cohorts have a prediction lower than 10%%. Highest prediction: %s (%s)",
                            predictedTumorOrigin.cancerType(),
                            Formats.percentage(predictedTumorOrigin.likelihood()));
            return Tables.createSingleColWithWidth(width).addCell(Cells.createContentNoBorder(message));
        } else {
            int numColumns = predictions.size() + 1;
            Table table = new Table(numColumns);

            table.addHeaderCell(Cells.createEmpty());
            IntStream.range(0, predictions.size())
                    .mapToObj(i -> String.format("%d. %s", i + 1, predictions.get(i).cancerType()))
                    .map(text -> Cells.createHeader(text).setPaddingLeft(PADDING_LEFT))
                    .forEach(table::addHeaderCell);

            table.addCell(Cells.createContentBold("Combined prediction score"));
            predictions.stream().map(p -> {
                Cell likelihoodCell = Cells.createContentBold(Formats.percentage(p.likelihood())).setPaddingLeft(PADDING_LEFT);
                if (!TumorOriginInterpreter.likelihoodMeetsConfidenceThreshold(p.likelihood())) {
                    likelihoodCell.addStyle(Styles.tableNoticeStyle());
                }
                return likelihoodCell;
            }).forEach(table::addCell);

            table.addCell(Cells.createContent("This score is calculated by combining information on:"));
            predictions.stream().map(p -> Cells.createContent("")).forEach(table::addCell);

            addClassifierRow("(1) SNV types", predictions, CuppaPrediction::snvPairwiseClassifier, table);
            addClassifierRow("(2) SNV genomic localisation distribution", predictions, CuppaPrediction::genomicPositionClassifier, table);
            addClassifierRow("(3) Driver genes and passenger characteristics", predictions, CuppaPrediction::featureClassifier, table);

            table.addCell(Cells.createSpanningSubNote(String.format("Other cohorts have a combined prediction of %s or lower",
                    Formats.percentage(TumorOriginInterpreter.greatestOmittedLikelihood(predictedTumorOrigin))), table));

            return table;
        }
    }

    private static void addClassifierRow(String classifierText, List<CuppaPrediction> predictions,
            Function<CuppaPrediction, Double> classifierFunction, Table table) {
        table.addCell(Cells.createContent(classifierText).setPaddingLeft(PADDING_LEFT));

        predictions.stream()
                .map(classifierFunction)
                .map(v -> v == null ? Formats.VALUE_UNKNOWN : Formats.percentage(v))
                .map(text -> Cells.createContent(text).setPaddingLeft(PADDING_LEFT).setPaddingRight(PADDING_RIGHT))
                .forEach(table::addCell);
    }
}
