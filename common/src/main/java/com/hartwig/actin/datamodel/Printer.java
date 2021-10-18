package com.hartwig.actin.datamodel;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.base.Strings;
import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.datamodel.molecular.MolecularRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class Printer {

    private static final Logger LOGGER = LogManager.getLogger(Printer.class);
    private static final int DEFAULT_INDENTATION = 1;

    private Printer() {
    }

    public static void printClinicalRecord(@NotNull ClinicalRecord record) {
        printClinicalRecord(record, DEFAULT_INDENTATION);
    }

    public static void printClinicalRecord(@NotNull ClinicalRecord record, int indentation) {
        IndentedPrinter printer = new IndentedPrinter(indentation);
        printer.print("Sample: " + record.sampleId());
        printer.print("Birth year: " + record.patient().birthYear());
        printer.print("Gender: " + record.patient().gender());
        printer.print("Primary tumor: " + record.tumor().primaryTumorLocation() + " (" + record.tumor().primaryTumorType() + ")");
        printer.print("WHO status: " + record.clinicalStatus().who());
    }

    public static void printMolecularRecord(@NotNull MolecularRecord record) {
        printMolecularRecord(record, DEFAULT_INDENTATION);
    }

    public static void printMolecularRecord(@NotNull MolecularRecord record, int indentation) {
        IndentedPrinter printer = new IndentedPrinter(indentation);
        printer.print("Sample: " + record.sampleId());
        printer.print("Has reliable quality: " + (record.hasReliableQuality() ? "Yes" : "No"));
        printer.print("Has reliable purity: " + (record.hasReliablePurity() ? "Yes" : "No"));
        printer.print("Actionable events: " + concat(record.actionableGenomicEvents()));
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
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