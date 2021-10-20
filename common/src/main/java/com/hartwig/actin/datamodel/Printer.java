package com.hartwig.actin.datamodel;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Printer {

    private static final Logger LOGGER = LogManager.getLogger(Printer.class);
    private static final int DEFAULT_INDENTATION = 1;

    private Printer() {
    }

    public static void printClinicalRecord(@NotNull ClinicalRecord record) {
        printClinicalRecord(record, DEFAULT_INDENTATION);
    }

    public static void printMolecularRecord(@NotNull MolecularRecord record) {
        printMolecularRecord(record, DEFAULT_INDENTATION);
    }

    @VisibleForTesting
    static void printClinicalRecord(@NotNull ClinicalRecord record, int indentation) {
        IndentedPrinter printer = new IndentedPrinter(indentation);
        printer.print("Sample: " + record.sampleId());
        printer.print("Birth year: " + record.patient().birthYear());
        printer.print("Gender: " + record.patient().gender());
        printer.print("Primary tumor location: " + tumorLocation(record.tumor()));
        printer.print("Primary tumor type: " + tumorType(record.tumor()));
        printer.print("WHO status: " + record.clinicalStatus().who());
    }

    @VisibleForTesting
    static void printMolecularRecord(@NotNull MolecularRecord record, int indentation) {
        IndentedPrinter printer = new IndentedPrinter(indentation);
        printer.print("Sample: " + record.sampleId());
        printer.print("Has reliable quality: " + (record.hasReliableQuality() ? "Yes" : "No"));
        printer.print("Has reliable purity: " + (record.hasReliablePurity() ? "Yes" : "No"));
        printer.print("Actionable events: " + concat(record.actionableGenomicEvents()));
    }

    @Nullable
    private static String tumorLocation(@NotNull TumorDetails tumor) {
        String location = tumor.primaryTumorLocation();
        if (location == null) {
            return null;
        }

        String subLocation = tumor.primaryTumorSubLocation();
        return subLocation != null && !subLocation.isEmpty() ? location + " (" + subLocation + ")" : location;
    }

    @Nullable
    private static String tumorType(@NotNull TumorDetails tumor) {
        String type = tumor.primaryTumorType();
        if (type == null) {
            return null;
        }

        String subType = tumor.primaryTumorSubType();
        return subType != null && !subType.isEmpty() ? type + " (" + subType + ")" : type;
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

    private static class IndentedPrinter {

        private final int indentation;

        public IndentedPrinter(final int indentation) {
            this.indentation = indentation;
        }

        public void print(@NotNull String line) {
            LOGGER.info("{}{}", Strings.repeat(" ", indentation), line);
        }
    }
}
