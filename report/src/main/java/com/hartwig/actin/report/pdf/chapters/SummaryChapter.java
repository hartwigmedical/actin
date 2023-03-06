package com.hartwig.actin.report.pdf.chapters;

import static com.hartwig.actin.report.pdf.util.Formats.STANDARD_KEY_WIDTH;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory;
import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.interpretation.EvaluatedTrial;
import com.hartwig.actin.report.interpretation.EvaluatedTrialFactory;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator;
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator;
import com.hartwig.actin.report.pdf.tables.treatment.EligibleActinTrialsGenerator;
import com.hartwig.actin.report.pdf.tables.treatment.EligibleApprovedTreatmentGenerator;
import com.hartwig.actin.report.pdf.tables.treatment.EligibleExternalTrialsGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SummaryChapter implements ReportChapter {

    @NotNull
    private final Report report;

    public SummaryChapter(@NotNull final Report report) {
        this.report = report;
    }

    @NotNull
    @Override
    public String name() {
        return "Summary";
    }

    @NotNull
    @Override
    public PageSize pageSize() {
        return PageSize.A4;
    }

    @Override
    public void render(@NotNull Document document) {
        addPatientDetails(document);
        addChapterTitle(document);
        addSummaryTable(document);
    }

    private void addPatientDetails(@NotNull Document document) {
        Paragraph patientDetailsLine = new Paragraph();
        patientDetailsLine.add(new Text("Gender: ").addStyle(Styles.reportHeaderLabelStyle()));
        patientDetailsLine.add(new Text(report.clinical().patient().gender().display()).addStyle(Styles.reportHeaderValueStyle()));
        patientDetailsLine.add(new Text(" | Birth year: ").addStyle(Styles.reportHeaderLabelStyle()));
        patientDetailsLine.add(new Text(String.valueOf(report.clinical().patient().birthYear())).addStyle(Styles.reportHeaderValueStyle()));
        patientDetailsLine.add(new Text(" | WHO: ").addStyle(Styles.reportHeaderLabelStyle()));
        patientDetailsLine.add(new Text(whoStatus(report.clinical().clinicalStatus().who())).addStyle(Styles.reportHeaderValueStyle()));
        document.add(patientDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));

        Paragraph tumorDetailsLine = new Paragraph();
        tumorDetailsLine.add(new Text("Tumor: ").addStyle(Styles.reportHeaderLabelStyle()));
        tumorDetailsLine.add(new Text(tumor(report.clinical().tumor())).addStyle(Styles.reportHeaderValueStyle()));
        tumorDetailsLine.add(new Text(" | Lesions: ").addStyle(Styles.reportHeaderLabelStyle()));
        tumorDetailsLine.add(new Text(lesions(report.clinical().tumor())).addStyle(Styles.reportHeaderValueStyle()));
        tumorDetailsLine.add(new Text(" | Stage: ").addStyle(Styles.reportHeaderLabelStyle()));
        tumorDetailsLine.add(new Text(stage(report.clinical().tumor())).addStyle(Styles.reportHeaderValueStyle()));
        document.add(tumorDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT));
    }

    @NotNull
    private static String whoStatus(@Nullable Integer who) {
        return who != null ? String.valueOf(who) : Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static String tumor(@NotNull TumorDetails tumor) {
        String location = tumorLocation(tumor);
        String type = tumorType(tumor);

        if (location == null || type == null) {
            return Formats.VALUE_UNKNOWN;
        } else {
            return location + (!type.isEmpty() ? " - " + type : Strings.EMPTY);
        }
    }

    @Nullable
    private static String tumorLocation(@NotNull TumorDetails tumor) {
        String tumorLocation = tumor.primaryTumorLocation();

        if (tumorLocation != null) {
            String tumorSubLocation = tumor.primaryTumorSubLocation();
            return (tumorSubLocation != null && !tumorSubLocation.isEmpty())
                    ? tumorLocation + " (" + tumorSubLocation + ")"
                    : tumorLocation;
        }

        return null;
    }

    @Nullable
    private static String tumorType(@NotNull TumorDetails tumor) {
        String tumorType = tumor.primaryTumorType();

        if (tumorType != null) {
            String tumorSubType = tumor.primaryTumorSubType();
            return (tumorSubType != null && !tumorSubType.isEmpty()) ? tumorSubType : tumorType;
        }

        return null;
    }

    @NotNull
    private static String stage(@NotNull TumorDetails tumor) {
        TumorStage stage = tumor.stage();
        return stage != null ? stage.display() : Formats.VALUE_UNKNOWN;
    }

    @NotNull
    private static String lesions(@NotNull TumorDetails tumor) {
        Set<String> lesions = Sets.newTreeSet();
        if (tumor.hasCnsLesions() != null && tumor.hasCnsLesions()) {
            lesions.add("CNS");
        }

        if (tumor.hasBrainLesions() != null && tumor.hasBrainLesions()) {
            lesions.add("Brain");
        }

        if (tumor.hasLiverLesions() != null && tumor.hasLiverLesions()) {
            lesions.add("Liver");
        }

        if (tumor.hasBoneLesions() != null && tumor.hasBoneLesions()) {
            lesions.add("Bone");
        }

        if (tumor.hasLungLesions() != null && tumor.hasLungLesions()) {
            lesions.add("Lung");
        }

        if (tumor.otherLesions() != null) {
            lesions.addAll(tumor.otherLesions());
        }

        if (tumor.biopsyLocation() != null) {
            lesions.add(tumor.biopsyLocation());
        }

        if (lesions.isEmpty()) {
            return Formats.VALUE_UNKNOWN;
        } else {
            StringJoiner joiner = Formats.commaJoiner();
            for (String lesion : lesions) {
                joiner.add(lesion);
            }
            return joiner.toString();
        }
    }

    private void addChapterTitle(@NotNull Document document) {
        document.add(new Paragraph(name()).addStyle(Styles.chapterTitleStyle()));
    }

    private void addSummaryTable(@NotNull Document document) {
        Table table = Tables.createSingleColWithWidth(contentWidth());

        float keyWidth = STANDARD_KEY_WIDTH;
        float valueWidth = contentWidth() - keyWidth;

        List<EvaluatedTrial> trials = EvaluatedTrialFactory.create(report.treatmentMatch());
        AggregatedEvidence aggregatedEvidence = AggregatedEvidenceFactory.create(report.molecular());

        List<TableGenerator> generators = Lists.newArrayList(new PatientClinicalHistoryGenerator(report.clinical(), keyWidth, valueWidth),
                new MolecularSummaryGenerator(report.clinical(), report.molecular(), trials, keyWidth, valueWidth),
                new EligibleApprovedTreatmentGenerator(report.clinical(), report.molecular(), contentWidth()),
                EligibleActinTrialsGenerator.forOpenTrials(trials, contentWidth()));

        if (!aggregatedEvidence.externalEligibleTrialsPerEvent().isEmpty()) {
            generators.add(new EligibleExternalTrialsGenerator(report.molecular().externalTrialSource(),
                    aggregatedEvidence.externalEligibleTrialsPerEvent(),
                    keyWidth,
                    valueWidth));
        }

        for (int i = 0; i < generators.size(); i++) {
            TableGenerator generator = generators.get(i);
            table.addCell(Cells.createTitle(generator.title()));
            table.addCell(Cells.create(generator.contents()));
            if (i < generators.size() - 1) {
                table.addCell(Cells.createEmpty());
            }
        }

        document.add(table);
    }
}
