package com.hartwig.actin.molecular.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
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
        printer.print(" Purity: " + formatPercentage(record.characteristics().purity()) + " (" + record.characteristics().qc() + ")");
        printer.print(" Mutations: " + mutationString(record.events().mutations()));
        printer.print(" Activated genes: " + concat(record.events().activatedGenes()));
        printer.print(" Inactivated genes: " + inactivatedGeneString(record.events().inactivatedGenes()));
        printer.print(" Amplified genes: " + concat(record.events().amplifiedGenes()));
        printer.print(" Wildtype genes: " + concat(record.events().wildtypeGenes()));
        printer.print(" Fusions: " + fusionString(record.events().fusions()));
        printer.print(" Predicted tumor origin: " + predictedTumorString(record.characteristics().predictedTumorOrigin()));
        printer.print(" Microsatellite unstable?: " + toYesNoUnknown(record.characteristics().isMicrosatelliteUnstable()));
        printer.print(" Homologous repair deficient?: " + toYesNoUnknown(record.characteristics().isHomologousRepairDeficient()));
        printer.print(" Tumor mutational burden: " + formatDouble(record.characteristics().tumorMutationalBurden()));
        printer.print(" Tumor mutational load: " + formatInteger(record.characteristics().tumorMutationalLoad()));

        printer.print("Events with evidence for approved treatment: " + toEvents(record.evidence().approvedResponsiveEvidence()));
        printer.print("Events associated with ACTIN trial eligibility: " + toEvents(record.evidence().actinTrials()));
        printer.print("Events associated with external trials: " + toEvents(record.evidence().externalTrials()));
        printer.print("Events with evidence for experimental treatment: " + toEvents(record.evidence().experimentalResponsiveEvidence()));
        printer.print("Other events with evidence: " + toEvents(record.evidence().otherResponsiveEvidence()));
        printer.print("Events with resistance evidence: " + toEvents(record.evidence().resistanceEvidence()));
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

        return predictedTumorOrigin.tumorType() + " (" + formatPercentage(predictedTumorOrigin.likelihood()) + ")";
    }

    @NotNull
    private static String toEvents(@NotNull Iterable<EvidenceEntry> evidences) {
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
