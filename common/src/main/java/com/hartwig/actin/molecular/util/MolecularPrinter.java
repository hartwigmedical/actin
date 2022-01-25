package com.hartwig.actin.molecular.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.util.DatamodelPrinter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularPrinter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

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
        printer.print("Experiment type '" + record.type() + "' on " + toDate(record.date()));
        printer.print("Has reliable quality?: " + toYesNo(record.hasReliableQuality()));
        printer.print("Mutations: " + mutationString(record.mutations()));
        printer.print("Activated genes: " + concat(record.activatedGenes()));
        printer.print("Inactivated genes: " + inactivatedGeneString(record.inactivatedGenes()));
        printer.print("Amplified genes: " + concat(record.amplifiedGenes()));
        printer.print("Wildtype genes: " + concat(record.wildtypeGenes()));
        printer.print("Fusions: " + fusionString(record.fusions()));
        printer.print("Microsatellite unstable?: " + toYesNo(record.isMicrosatelliteUnstable()));
        printer.print("Homologous repair deficient?: " + toYesNo(record.isHomologousRepairDeficient()));
        printer.print("Tumor mutational burden: " + NUMBER_FORMAT.format(record.tumorMutationalBurden()));
        printer.print("Tumor mutational load: " + record.tumorMutationalLoad());

        MolecularEvidence evidence = record.evidence();
        printer.print("ACTIN actionable events: " + toEvents(evidence.actinTrialEvidence()));
        printer.print("General trial actionable events: " + toEvents(evidence.generalTrialEvidence()));
        printer.print("General responsive evidence: " + toEvents(evidence.generalResponsiveEvidence()));
        printer.print("General resistance evidence: " + toEvents(evidence.generalResistanceEvidence()));
    }

    @NotNull
    private static String toDate(@Nullable LocalDate date) {
        return date != null ? DATE_FORMAT.format(date) : "unknown date";
    }

    @NotNull
    private static String mutationString(@NotNull List<GeneMutation> mutations) {
        Set<String> strings = Sets.newHashSet();
        for (GeneMutation mutation : mutations) {
            strings.add(mutation.gene() + " " + mutation.mutation());
        }
        return concat(strings);
    }

    @NotNull
    private static String inactivatedGeneString(@NotNull Set<InactivatedGene> inactivatedGenes) {
        Set<String> strings = Sets.newHashSet();
        for (InactivatedGene inactivatedGene : inactivatedGenes) {
            strings.add(inactivatedGene.gene() + " (" + (inactivatedGene.hasBeenDeleted() ? "deleted" : "not deleted") + ")");
        }
        return concat(strings);
    }

    @NotNull
    private static String fusionString(@NotNull List<FusionGene> fusions) {
        Set<String> strings = Sets.newHashSet();
        for (FusionGene fusion : fusions) {
            strings.add(fusion.fiveGene() + "-" + fusion.threeGene());
        }
        return concat(strings);
    }

    @NotNull
    private static String toEvents(@NotNull Multimap<String, String> evidence) {
        return concat(evidence.keySet());
    }

    @NotNull
    private static String toYesNo(@Nullable Boolean bool) {
        if (bool == null) {
            return "Unknown";
        }

        return bool ? "Yes" : "No";
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        String result = joiner.toString();
        return !result.isEmpty() ? result : "None";
    }
}
