package com.hartwig.actin.report.pdf.tables.molecular;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Actionable;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.report.pdf.tables.TableGenerator;
import com.hartwig.actin.report.pdf.util.Cells;
import com.hartwig.actin.report.pdf.util.Formats;
import com.hartwig.actin.report.pdf.util.Tables;
import com.itextpdf.layout.element.Table;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class MolecularDriversGenerator implements TableGenerator {

    @NotNull
    private final MolecularRecord molecular;
    private final float width;

    public MolecularDriversGenerator(@NotNull final MolecularRecord molecular, final float width) {
        this.molecular = molecular;
        this.width = width;
    }

    @NotNull
    @Override
    public String title() {
        return "Drivers";
    }

    @NotNull
    @Override
    public Table contents() {
        float colWidth = width / 8;
        Table table = Tables.createFixedWidthCols(colWidth, colWidth * 2, colWidth, colWidth, colWidth, colWidth, colWidth);

        table.addHeaderCell(Cells.createHeader("Type"));
        table.addHeaderCell(Cells.createHeader("Driver"));
        table.addHeaderCell(Cells.createHeader("Driver likelihood"));
        table.addHeaderCell(Cells.createHeader("Actionable in " + molecular.evidence().actinSource()));
        table.addHeaderCell(Cells.createHeader("Actionable in " + molecular.evidence().externalTrialSource()));
        table.addHeaderCell(Cells.createHeader("Best evidence in " + molecular.evidence().evidenceSource()));
        table.addHeaderCell(Cells.createHeader("Resistance in " + molecular.evidence().evidenceSource()));

        boolean hasSubclonal = addVariants(table);
        addAmplifications(table);
        addLosses(table);
        addDisruptions(table);
        addFusions(table);
        addViruses(table);

        if (hasSubclonal) {
            table.addCell(Cells.createSpanningSubNote("* Variant has > 50% likelihood of being sub-clonal", table));
        }

        return Tables.makeWrapping(table);
    }

    private boolean addVariants(@NotNull Table table) {
        boolean hasSubclonal = false;
        for (Variant variant : molecular.drivers().variants()) {
            table.addCell(Cells.createContent("Mutation (" + variant.driverType().display() + ")"));

            double boundedVariantCopies = Math.max(0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()));
            String variantCopyString = boundedVariantCopies < 1
                    ? Formats.singleDigitNumber(boundedVariantCopies)
                    : Formats.noDigitNumber(boundedVariantCopies);

            double boundedTotalCopies = Math.max(0, variant.totalCopyNumber());
            String totalCopyString =
                    boundedTotalCopies < 1 ? Formats.singleDigitNumber(boundedTotalCopies) : Formats.noDigitNumber(boundedTotalCopies);

            String driver = variant.gene() + " " + variant.impact() + " (" + variantCopyString + "/" + totalCopyString + " copies)";
            if (variant.clonalLikelihood() <= 0.5) {
                hasSubclonal = true;
                driver = driver + "*";
            }
            table.addCell(Cells.createContent(driver));
            table.addCell(Cells.createContent(interpretLikelihood(variant.driverLikelihood())));

            addActionability(table, variant);
        }
        return hasSubclonal;
    }

    @NotNull
    private static String interpretLikelihood(double driverLikelihood) {
        if (driverLikelihood >= 0.8) {
            return "High";
        } else if (driverLikelihood >= 0.2) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    private void addAmplifications(@NotNull Table table) {
        for (Amplification amplification : molecular.drivers().amplifications()) {
            String addon = amplification.isPartial() ? " (partial)" : Strings.EMPTY;
            table.addCell(Cells.createContent("Amplification" + addon));
            table.addCell(Cells.createContent(amplification.gene() + " ampl, " + amplification.copies() + " copies"));
            table.addCell(Cells.createContent(Strings.EMPTY));

            addActionability(table, amplification);
        }
    }

    private void addLosses(@NotNull Table table) {
        for (Loss loss : molecular.drivers().losses()) {
            table.addCell(Cells.createContent("Loss"));
            table.addCell(Cells.createContent(loss.gene() + " del"));
            table.addCell(Cells.createContent(Strings.EMPTY));

            addActionability(table, loss);
        }
    }

    private void addDisruptions(@NotNull Table table) {
        for (Disruption disruption : molecular.drivers().disruptions()) {
            table.addCell(Cells.createContent(disruption.isHomozygous() ? "Homozygous disruption" : "Non-homozygous disruption"));
            String addon = !disruption.details().isEmpty() ? ", " + disruption.details() : Strings.EMPTY;
            table.addCell(Cells.createContent(disruption.gene() + addon));
            table.addCell(Cells.createContent(Strings.EMPTY));

            if (disruption.isHomozygous()) {
                addActionability(table, disruption);
            } else {
                table.addCell(Cells.createContent(Strings.EMPTY));
                table.addCell(Cells.createContent(Strings.EMPTY));
                table.addCell(Cells.createContent(Strings.EMPTY));
                table.addCell(Cells.createContent(Strings.EMPTY));
            }
        }
    }

    private void addFusions(@NotNull Table table) {
        for (Fusion fusion : molecular.drivers().fusions()) {
            table.addCell(Cells.createContent(fusion.driverType().display()));
            String name = fusion.fiveGene() + "-" + fusion.threeGene() + " fusion";
            table.addCell(Cells.createContent(name + ", " + fusion.details()));
            table.addCell(Cells.createContent(fusion.driverLikelihood().display()));

            addActionability(table, fusion);
        }
    }

    private void addViruses(@NotNull Table table) {
        for (Virus virus : molecular.drivers().viruses()) {
            table.addCell(Cells.createContent("Virus"));
            table.addCell(Cells.createContent(virus.name() + ", " + virus.details()));
            table.addCell(Cells.createContent(virus.driverLikelihood().display()));

            addActionability(table, virus);
        }
    }

    private void addActionability(@NotNull Table table, @NotNull Actionable actionable) {
        table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(isActionableInActin(actionable))));
        table.addCell(Cells.createContentYesNo(Formats.yesNoUnknown(isActionableInExternal(actionable))));
        table.addCell(Cells.createContent(bestEvidence(actionable)));
        table.addCell(Cells.createContent(highestResistance(actionable)));
    }

    private boolean isActionableInActin(@NotNull Actionable actionable) {
        return hasEvidence(molecular.evidence().actinTrials(), actionable);
    }

    private boolean isActionableInExternal(@NotNull Actionable actionable) {
        return hasEvidence(molecular.evidence().externalTrials(), actionable);
    }

    @NotNull
    private String bestEvidence(@NotNull Actionable actionable) {
        if (hasEvidence(molecular.evidence().approvedEvidence(), actionable)) {
            return "Approved";
        } else if (hasEvidence(molecular.evidence().onLabelExperimentalEvidence(), actionable) || hasEvidence(molecular.evidence()
                .offLabelExperimentalEvidence(), actionable)) {
            return "Experimental";
        } else if (hasEvidence(molecular.evidence().preClinicalEvidence(), actionable)) {
            return "Pre-clinical";
        }

        return Strings.EMPTY;
    }

    @NotNull
    private String highestResistance(@NotNull Actionable actionable) {
        if (hasEvidence(molecular.evidence().knownResistanceEvidence(), actionable)) {
            return "Known";
        } else if (hasEvidence(molecular.evidence().suspectResistanceEvidence(), actionable)) {
            return "Suspect";
        }

        return Strings.EMPTY;
    }

    private static boolean hasEvidence(@NotNull Iterable<EvidenceEntry> evidences, @NotNull Actionable actionable) {
        for (EvidenceEntry evidence : evidences) {
            if (evidence.event().equals(actionable.event())) {
                return true;
            }
        }
        return false;
    }
}
