package com.hartwig.actin.report.pdf;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.hartwig.actin.report.ReportApplication;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Style;

import org.jetbrains.annotations.NotNull;

public final class ReportResources {

    static final String METADATA_TITLE = "HMF ACTIN Report v" + ReportApplication.VERSION;
    static final String METADATA_AUTHOR = "Hartwig ACTIN System";

    public static final String NOT_AVAILABLE = "NA";

    public static final float PAGE_MARGIN_TOP = 100; // Top margin also excludes the chapter title, which is rendered in the header
    public static final float PAGE_MARGIN_LEFT = 30;
    public static final float PAGE_MARGIN_RIGHT = 30;
    public static final float PAGE_MARGIN_BOTTOM = 40;

    public static final DeviceRgb PALETTE_WHITE = new DeviceRgb(255, 255, 255);
    public static final DeviceRgb PALETTE_MID_GREY = new DeviceRgb(101, 106, 108);
    public static final DeviceRgb PALETTE_DARK_GREY = new DeviceRgb(39, 47, 50);
    public static final DeviceRgb PALETTE_BLACK = new DeviceRgb(0, 0, 0);

    public static final DeviceRgb PALETTE_BLUE = new DeviceRgb(74, 134, 232);

    private static final String FONT_REGULAR_PATH = "fonts/nimbus-sans/NimbusSansL-Regular.ttf";
    private static final String FONT_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold.ttf";

    @NotNull
    public static DecimalFormat decimalFormat(@NotNull String format) {
        // To make sure every decimal format uses a dot as separator rather than a comma.
        return new DecimalFormat(format, DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    }

    @NotNull
    public static PdfFont fontRegular() {
        // Cannot be created statically as every PDF needs their own private font objects.
        return createFontFromProgram(loadFontProgram(FONT_REGULAR_PATH));
    }

    @NotNull
    public static PdfFont fontBold() {
        // Cannot be created statically as every PDF needs their own private font objects.
        return createFontFromProgram(loadFontProgram(FONT_BOLD_PATH));
    }

    public static Style chapterTitleStyle() {
        return new Style().setFont(fontBold()).setFontSize(10).setFontColor(ReportResources.PALETTE_BLACK);
    }

    public static Style tableTitleStyle() {
        return new Style().setFont(fontBold()).setFontSize(8).setFontColor(ReportResources.PALETTE_BLUE);
    }

    public static Style tableHeaderStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(ReportResources.PALETTE_MID_GREY);
    }

    public static Style tableContentStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(ReportResources.PALETTE_DARK_GREY);
    }

    public static Style labelStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(ReportResources.PALETTE_BLACK);
    }

    public static Style valueStyle() {
        return new Style().setFont(fontRegular()).setFontSize(8).setFontColor(ReportResources.PALETTE_BLUE);
    }

    public static Style subTextStyle() {
        return new Style().setFont(fontRegular()).setFontSize(6).setFontColor(ReportResources.PALETTE_BLACK);
    }

    public static Style pageNumberStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(ReportResources.PALETTE_BLUE);
    }

    public static Style sidePanelLabelStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(ReportResources.PALETTE_WHITE);
    }

    public static Style sidePanelValueStyle() {
        return new Style().setFont(fontBold()).setFontSize(10).setFontColor(ReportResources.PALETTE_WHITE);
    }

    @NotNull
    private static PdfFont createFontFromProgram(@NotNull FontProgram program) {
        return PdfFontFactory.createFont(program, PdfEncodings.IDENTITY_H);
    }

    @NotNull
    private static FontProgram loadFontProgram(@NotNull String resourcePath) {
        try {
            return FontProgramFactory.createFont(resourcePath);
        } catch (IOException exception) {
            // Should never happen, fonts are loaded from code
            throw new IllegalStateException(exception);
        }
    }
}
