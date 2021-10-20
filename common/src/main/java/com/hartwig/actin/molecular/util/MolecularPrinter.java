package com.hartwig.actin.molecular.util;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.common.DatamodelPrinter;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.jetbrains.annotations.NotNull;

public class MolecularPrinter {

    @NotNull
    private final DatamodelPrinter printer;

    public static void printRecord(@NotNull MolecularRecord record) {
        new MolecularPrinter(DatamodelPrinter.withDefaultIndentation()).print(record);
    }

    public MolecularPrinter(@NotNull final DatamodelPrinter printer) {
        this.printer = printer;
    }

    public void print(@NotNull MolecularRecord record) {
        printer.print("Sample: " + record.sampleId());
        printer.print("Has reliable quality: " + (record.hasReliableQuality() ? "Yes" : "No"));
        printer.print("Actionable events: " + concat(record.actionableGenomicEvents()));
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
