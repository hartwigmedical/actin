package com.hartwig.actin.report.pdf.util;

import com.hartwig.actin.report.ReporterApplication;

public final class Constants {

    public static final String METADATA_TITLE = "HMF ACTIN Report v" + ReporterApplication.VERSION;
    public static final String METADATA_AUTHOR = "Hartwig ACTIN System";

    public static final float PAGE_MARGIN_TOP = 100; // Top margin also excludes the chapter title, which is rendered in the header
    public static final float PAGE_MARGIN_LEFT = 30;
    public static final float PAGE_MARGIN_RIGHT = 30;
    public static final float PAGE_MARGIN_BOTTOM = 40;

    private Constants() {
    }
}
