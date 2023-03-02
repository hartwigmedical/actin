package com.hartwig.actin.report.pdf.util;

import java.io.IOException;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Style;

import org.jetbrains.annotations.NotNull;

public final class Styles {

    public static final DeviceRgb PALETTE_WHITE = new DeviceRgb(255, 255, 255);
    public static final DeviceRgb PALETTE_BLACK = new DeviceRgb(0, 0, 0);
    public static final DeviceRgb PALETTE_MID_GREY = new DeviceRgb(101, 106, 108);
    public static final DeviceRgb PALETTE_BLUE = new DeviceRgb(74, 134, 232);
    public static final DeviceRgb PALETTE_RED = new DeviceRgb(231, 85, 85);

    public static final DeviceRgb PALETTE_EVALUATION_PASS = new DeviceRgb(0, 150, 0);
    public static final DeviceRgb PALETTE_EVALUATION_WARN = new DeviceRgb(255, 130, 0);
    public static final DeviceRgb PALETTE_EVALUATION_FAILED = new DeviceRgb(231, 85, 85);
    public static final DeviceRgb PALETTE_EVALUATION_UNCLEAR = new DeviceRgb(85, 85, 85);

    public static final DeviceRgb PALETTE_YES_OR_NO_YES = new DeviceRgb(0, 150, 0);
    public static final DeviceRgb PALETTE_YES_OR_NO_NO = new DeviceRgb(231, 85, 85);
    public static final DeviceRgb PALETTE_YES_OR_NO_UNCLEAR = new DeviceRgb(85, 85, 85);

    public static final DeviceRgb PALETTE_WARN = PALETTE_EVALUATION_WARN;

    private static final String FONT_REGULAR_PATH = "fonts/nimbus-sans/NimbusSansL-Regular.ttf";
    private static final String FONT_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold.ttf";

    private static PdfFont fontRegular = createFont(FONT_REGULAR_PATH);
    private static PdfFont fontBold = createFont(FONT_BOLD_PATH);

    public static void initialize() {
        // Fonts must be re-initialized for each report
        fontRegular = createFont(FONT_REGULAR_PATH);
        fontBold = createFont(FONT_BOLD_PATH);
    }

    @NotNull
    public static Style reportTitleStyle() {
        return new Style().setFont(fontBold()).setFontSize(11).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style chapterTitleStyle() {
        return new Style().setFont(fontBold()).setFontSize(10).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableTitleStyle() {
        return new Style().setFont(fontBold()).setFontSize(8).setFontColor(Styles.PALETTE_BLUE);
    }

    @NotNull
    public static Style tableSubTitleStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_BLUE);
    }

    @NotNull
    public static Style tableSubStyle() {
        return new Style().setFont(fontRegular()).setFontSize(6).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableHeaderStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_MID_GREY);
    }

    @NotNull
    public static Style tableContentStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableNoticeStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_WARN);
    }
    @NotNull
    public static Style tableKeyStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableUnknownStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableHighlightStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableWarnStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_RED);
    }

    @NotNull
    public static Style reportHeaderLabelStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style reportHeaderValueStyle() {
        return new Style().setFont(fontBold()).setFontSize(8).setFontColor(Styles.PALETTE_BLUE);
    }

    @NotNull
    public static Style pageNumberStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_BLUE);
    }

    @NotNull
    public static Style sidePanelLabelStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_WHITE);
    }

    @NotNull
    public static Style sidePanelValueStyle() {
        return new Style().setFont(fontBold()).setFontSize(10).setFontColor(Styles.PALETTE_WHITE);
    }

    @NotNull
    public static PdfFont fontRegular() {
        // Each PDF needs its own private font objects, but they can be static as long as they are re-initialized for each report.
        return fontRegular;
    }

    @NotNull
    public static PdfFont fontBold() {
        // Each PDF needs its own private font objects, but they can be static as long as they are re-initialized for each report.
        return fontBold;
    }

    private static PdfFont createFont(String fontPath) {
        return PdfFontFactory.createFont(loadFontProgram(fontPath), PdfEncodings.IDENTITY_H);
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
