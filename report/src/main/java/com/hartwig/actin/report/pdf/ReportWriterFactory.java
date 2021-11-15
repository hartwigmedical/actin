package com.hartwig.actin.report.pdf;

import org.jetbrains.annotations.NotNull;

public final class ReportWriterFactory {

    private ReportWriterFactory() {
    }

    @NotNull
    public static ReportWriter createProductionReportWriter(@NotNull String outputDirectory) {
        return new ReportWriter(true, outputDirectory);
    }

    @NotNull
    public static ReportWriter createInMemoryReportWriter() {
        return new ReportWriter(false, null);
    }
}
