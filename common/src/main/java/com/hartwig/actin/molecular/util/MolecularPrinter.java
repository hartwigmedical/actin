package com.hartwig.actin.molecular.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.StringJoiner;

import com.google.common.collect.Multimap;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory;
import com.hartwig.actin.util.DatamodelPrinter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularPrinter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#'%'", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    @NotNull
    private final DatamodelPrinter printer;

    public static void printRecord(@NotNull MolecularRecord record) {
        new MolecularPrinter(DatamodelPrinter.withDefaultIndentation()).print(record);
    }

    private MolecularPrinter(@NotNull final DatamodelPrinter printer) {
        this.printer = printer;
    }

    public void print(@NotNull MolecularRecord record) {
        printer.print("Sample: " + record.sampleId());
        printer.print(" Experiment type '" + record.type() + "' on " + formatDate(record.date()));
        printer.print(" Contains tumor cells: " + toYesNoUnknown(record.containsTumorCells()));
        printer.print(" Has sufficient quality and purity: " + toYesNoUnknown(record.hasSufficientQualityAndPurity()));
        printer.print(" Purity: " + formatPercentage(record.characteristics().purity()));
        printer.print(" Predicted tumor origin: " + predictedTumorString(record.characteristics().predictedTumorOrigin()));
        printer.print(" Microsatellite unstable?: " + toYesNoUnknown(record.characteristics().isMicrosatelliteUnstable()));
        printer.print(" Homologous repair deficient?: " + toYesNoUnknown(record.characteristics().isHomologousRepairDeficient()));
        printer.print(" Tumor mutational burden: " + formatDouble(record.characteristics().tumorMutationalBurden()));
        printer.print(" Tumor mutational load: " + formatInteger(record.characteristics().tumorMutationalLoad()));

        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(record);
        printer.print(" Events with evidence for approved treatment: " + keys(evidence.approvedTreatmentsPerEvent()));
        printer.print(" Events associated with external trials: " + keys(evidence.externalEligibleTrialsPerEvent()));
        printer.print(
                " Events with evidence for on-label experimental treatment: " + keys(evidence.onLabelExperimentalTreatmentsPerEvent()));
        printer.print(
                " Events with evidence for off-label experimental treatment: " + keys(evidence.offLabelExperimentalTreatmentsPerEvent()));
        printer.print(" Events with evidence for pre-clinical treatment: " + keys(evidence.preClinicalTreatmentsPerEvent()));
        printer.print(" Events with known resistance evidence: " + keys(evidence.knownResistantTreatmentsPerEvent()));
        printer.print(" Events with suspect resistance evidence: " + keys(evidence.suspectResistanceTreatmentsPerEvent()));
    }

    @NotNull
    private static String formatDate(@Nullable LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : "unknown date";
    }

    @NotNull
    private static String formatDouble(@Nullable Double number) {
        return number != null ? NUMBER_FORMAT.format(number) : "unknown";
    }

    @NotNull
    private static String formatPercentage(@Nullable Double percentage) {
        return percentage != null ? PERCENTAGE_FORMAT.format(percentage * 100) : "unknown";
    }

    @NotNull
    private static String formatInteger(@Nullable Integer integer) {
        return integer != null ? String.valueOf(integer) : "unknown";
    }

    @NotNull
    private static String predictedTumorString(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        if (predictedTumorOrigin == null) {
            return "Not determined";
        }

        return predictedTumorOrigin.cancerType() + " (" + formatPercentage(predictedTumorOrigin.likelihood()) + ")";
    }

    @NotNull
    private static String toYesNoUnknown(@Nullable Boolean bool) {
        if (bool == null) {
            return "Unknown";
        }

        return bool ? "Yes" : "No";
    }

    @NotNull
    private static String keys(@NotNull Multimap<String, String> multimap) {
        return concat(multimap.keySet());
    }

    @NotNull
    private static String concat(@NotNull Iterable<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        String result = joiner.toString();
        return !result.isEmpty() ? result : "None";
    }
}
