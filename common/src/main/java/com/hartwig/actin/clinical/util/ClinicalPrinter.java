package com.hartwig.actin.clinical.util;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.util.DatamodelPrinter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClinicalPrinter {

    @NotNull
    private final DatamodelPrinter printer;

    public static void printRecord(@NotNull ClinicalRecord record) {
        new ClinicalPrinter(DatamodelPrinter.withDefaultIndentation()).print(record);
    }

    public ClinicalPrinter(@NotNull final DatamodelPrinter printer) {
        this.printer = printer;
    }

    public void print(@NotNull ClinicalRecord record) {
        printer.print("Sample: " + record.sampleId());
        printer.print("Birth year: " + record.patient().birthYear());
        printer.print("Gender: " + record.patient().gender());
        printer.print("Primary tumor location: " + tumorLocation(record.tumor()));
        printer.print("Primary tumor type: " + tumorType(record.tumor()));
        printer.print("WHO status: " + record.clinicalStatus().who());
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

}
