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
    public static final DeviceRgb PALETTE_BLUE = new DeviceRgb(74, 134, 232);
    public static final DeviceRgb PALETTE_RED = new DeviceRgb(231, 85, 85);

    private static final String FONT_REGULAR_PATH = "fonts/nimbus-sans/NimbusSansL-Regular.ttf";
    private static final String FONT_BOLD_PATH = "fonts/nimbus-sans/NimbusSansL-Bold.ttf";

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
    public static Style tableKeyStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableValueUnknownStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableValueHighlightStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style tableValueWarnStyle() {
        return new Style().setFont(fontBold()).setFontSize(7).setFontColor(Styles.PALETTE_RED);
    }

    @NotNull
    public static Style labelStyle() {
        return new Style().setFont(fontRegular()).setFontSize(7).setFontColor(Styles.PALETTE_BLACK);
    }

    @NotNull
    public static Style highlightStyle() {
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
        // Cannot be created statically as every PDF needs their own private font objects.
        return createFontFromProgram(loadFontProgram(FONT_REGULAR_PATH));
    }

    @NotNull
    public static PdfFont fontBold() {
        // Cannot be created statically as every PDF needs their own private font objects.
        return createFontFromProgram(loadFontProgram(FONT_BOLD_PATH));
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

    private Styles() {
    }
}
