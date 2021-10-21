package com.hartwig.actin.report.pdf.components;

import java.time.LocalDate;

import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;

import org.jetbrains.annotations.NotNull;

public class SidePanel {

    private static final float ROW_SPACING = 35;
    private static final float VALUE_TEXT_Y_OFFSET = 18;
    private static final float MAX_WIDTH = 120;

    private static final float RECTANGLE_WIDTH = 170;
    private static final float RECTANGLE_HEIGHT = 84;

    @NotNull
    private final String patientId;

    public SidePanel(@NotNull final String patientId) {
        this.patientId = patientId;
    }

    public void render(@NotNull PdfPage page) {
        PdfCanvas canvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), page.getDocument());
        Rectangle pageSize = page.getPageSize();
        canvas.rectangle(pageSize.getWidth(), pageSize.getHeight(), -RECTANGLE_WIDTH, -RECTANGLE_HEIGHT);
        canvas.setFillColor(Styles.PALETTE_BLUE);
        canvas.fill();

        int sideTextIndex = 0;
        Canvas cv = new Canvas(canvas, page.getPageSize());

        cv.add(createDiv(pageSize, ++sideTextIndex, "Patient", patientId));
        cv.add(createDiv(pageSize, ++sideTextIndex, "Report Date", Formats.date(LocalDate.now())));

        canvas.release();
    }

    @NotNull
    private static Div createDiv(@NotNull Rectangle pageSize, int index, @NotNull String label, @NotNull String value) {
        Div div = new Div();
        div.setKeepTogether(true);

        float yPos = (pageSize.getHeight() + 15) - index * ROW_SPACING;
        float xPos = pageSize.getWidth() - RECTANGLE_WIDTH + 15;

        div.add(new Paragraph(label.toUpperCase()).addStyle(Styles.sidePanelLabelStyle()).setFixedPosition(xPos, yPos, MAX_WIDTH));

        float valueFontSize = maxPointSizeForWidth(Styles.fontBold(), 10, 6, value, MAX_WIDTH);
        yPos -= VALUE_TEXT_Y_OFFSET;
        div.add(new Paragraph(value).addStyle(Styles.sidePanelValueStyle().setFontSize(valueFontSize))
                .setHeight(15)
                .setFixedPosition(xPos, yPos, MAX_WIDTH)
                .setFixedLeading(valueFontSize));

        return div;
    }

    private static float maxPointSizeForWidth(@NotNull PdfFont font, float initialFontSize, float minFontSize, @NotNull String text,
            float maxWidth) {
        float fontIncrement = 0.1F;

        float fontSize = initialFontSize;
        float width = font.getWidth(text, initialFontSize);
        while (width > maxWidth && fontSize > minFontSize) {
            fontSize -= fontIncrement;
            width = font.getWidth(text, fontSize);
        }

        return fontSize;
    }
}
