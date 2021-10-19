package com.hartwig.actin.report.pdf.util;

import java.time.LocalDate;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;

import org.jetbrains.annotations.NotNull;

public final class Clinical {

    private Clinical() {
    }

    @NotNull
    public static String questionnaireDate(@NotNull ClinicalRecord record) {
        LocalDate questionnaireDate = record.patient().questionnaireDate();
        return questionnaireDate != null ? Formats.date(questionnaireDate) : "Date unknown";
    }
}
