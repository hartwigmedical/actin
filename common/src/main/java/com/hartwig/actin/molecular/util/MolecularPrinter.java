package com.hartwig.actin.molecular.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
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
        printer.print(" Has sufficient quality: " + toYesNoUnknown(record.hasSufficientQuality()));
        printer.print(" Purity: " + formatPercentage(record.characteristics().purity()));
        printer.print(" Predicted tumor origin: " + predictedTumorString(record.characteristics().predictedTumorOrigin()));
        printer.print(" Microsatellite unstable?: " + toYesNoUnknown(record.characteristics().isMicrosatelliteUnstable()));
        printer.print(" Homologous repair deficient?: " + toYesNoUnknown(record.characteristics().isHomologousRepairDeficient()));
        printer.print(" Tumor mutational burden: " + formatDouble(record.characteristics().tumorMutationalBurden()));
        printer.print(" Tumor mutational load: " + formatInteger(record.characteristics().tumorMutationalLoad()));

        MolecularEvidence evidence = record.evidence();
        printer.print("Events with evidence for approved treatment: " + toEvents(evidence.approvedEvidence()));
        printer.print("Events associated with ACTIN trial eligibility: " + toEvents(evidence.actinTrials()));
        printer.print("Events associated with external trials: " + toEvents(evidence.externalTrials()));
        printer.print("Events with evidence for on-label experimental treatment: " + toEvents(evidence.onLabelExperimentalEvidence()));
        printer.print("Events with evidence for off-label experimental treatment: " + toEvents(evidence.offLabelExperimentalEvidence()));
        printer.print("Events with evidence for pre-clinical treatment: " + toEvents(evidence.preClinicalEvidence()));
        printer.print("Events with known resistance evidence: " + toEvents(evidence.knownResistanceEvidence()));
        printer.print("Events with suspect resistance evidence: " + toEvents(evidence.suspectResistanceEvidence()));
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

        return predictedTumorOrigin.tumorType() + " (" + formatPercentage(predictedTumorOrigin.likelihood()) + ")";
    }

    @NotNull
    private static String toEvents(@NotNull Iterable<? extends EvidenceEntry> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (EvidenceEntry evidence : evidences) {
            events.add(evidence.event());
        }
        return concat(events);
    }

    @NotNull
    private static String toYesNoUnknown(@Nullable Boolean bool) {
        if (bool == null) {
            return "Unknown";
        }

        return bool ? "Yes" : "No";
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
