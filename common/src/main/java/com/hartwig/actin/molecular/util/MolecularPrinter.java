package com.hartwig.actin.molecular.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;
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
        printer.print(" Has reliable quality?: " + toYesNoUnknown(record.hasReliableQuality()));
        printer.print(" Mutations: " + mutationString(record.mutations()));
        printer.print(" Activated genes: " + concat(record.activatedGenes()));
        printer.print(" Inactivated genes: " + inactivatedGeneString(record.inactivatedGenes()));
        printer.print(" Amplified genes: " + concat(record.amplifiedGenes()));
        printer.print(" Wildtype genes: " + concat(record.wildtypeGenes()));
        printer.print(" Fusions: " + fusionString(record.fusions()));
        printer.print(" Predicted tumor origin: " + predictedTumorString(record.predictedTumorOrigin()));
        printer.print(" Microsatellite unstable?: " + toYesNoUnknown(record.isMicrosatelliteUnstable()));
        printer.print(" Homologous repair deficient?: " + toYesNoUnknown(record.isHomologousRepairDeficient()));
        printer.print(" Tumor mutational burden: " + formatDouble(record.tumorMutationalBurden()));
        printer.print(" Tumor mutational load: " + formatInteger(record.tumorMutationalLoad()));

        printer.print("Events with evidence for approved treatment: " + toEvents(record.approvedResponsiveEvidence()));
        printer.print("Events associated with ACTIN trial eligibility: " + toEvents(record.actinTrials()));
        printer.print("Events associated with external trials: " + toEvents(record.externalTrials()));
        printer.print("Events with evidence for experimental treatment: " + toEvents(record.experimentalResponsiveEvidence()));
        printer.print("Other events with evidence: " + toEvents(record.otherResponsiveEvidence()));
        printer.print("Events with resistance evidence: " + toEvents(record.resistanceEvidence()));
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
    private static String formatInteger(@Nullable Integer integer) {
        return integer != null ? String.valueOf(integer) : "unknown";
    }

    @NotNull
    private static String mutationString(@NotNull Iterable<GeneMutation> mutations) {
        Set<String> strings = Sets.newHashSet();
        for (GeneMutation mutation : mutations) {
            strings.add(mutation.gene() + " " + mutation.mutation());
        }
        return concat(strings);
    }

    @NotNull
    private static String inactivatedGeneString(@NotNull Iterable<InactivatedGene> inactivatedGenes) {
        Set<String> strings = Sets.newHashSet();
        for (InactivatedGene inactivatedGene : inactivatedGenes) {
            strings.add(inactivatedGene.gene());
        }
        return concat(strings);
    }

    @NotNull
    private static String fusionString(@NotNull Iterable<FusionGene> fusions) {
        Set<String> strings = Sets.newHashSet();
        for (FusionGene fusion : fusions) {
            strings.add(fusion.fiveGene() + "-" + fusion.threeGene());
        }
        return concat(strings);
    }

    @NotNull
    private static String predictedTumorString(@Nullable PredictedTumorOrigin predictedTumorOrigin) {
        if (predictedTumorOrigin == null) {
            return "Not determined";
        }

        return predictedTumorOrigin.tumorType() + " (" + PERCENTAGE_FORMAT.format(predictedTumorOrigin.likelihood() * 100) + ")";
    }

    @NotNull
    private static String toEvents(@NotNull List<MolecularEvidence> evidences) {
        Set<String> events = Sets.newTreeSet();
        for (MolecularEvidence evidence : evidences) {
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
