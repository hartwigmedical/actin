package com.hartwig.actin.report.pdf.chapters

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType

class TrialMatchingDetailsChapter(private val report: Report) : ReportChapter {
    override fun name(): String {
        return "Trial Matching Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        val eligible: MutableList<TrialMatch> = Lists.newArrayList()
        val nonEligible: MutableList<TrialMatch> = Lists.newArrayList()
        for (trial in report.treatmentMatch().trialMatches()) {
            if (isOpenAndPotentiallyEligible(trial)) {
                eligible.add(trial)
            } else {
                nonEligible.add(trial)
            }
        }
        if (!eligible.isEmpty()) {
            addTrialMatches(document, eligible, "Potentially eligible open trials & cohorts", true)
        }
        if (!nonEligible.isEmpty()) {
            if (!eligible.isEmpty()) {
                document.add(pageBreak())
            }
            addTrialMatches(document, nonEligible, "Other trials & cohorts", false)
        }
    }

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addTrialMatches(
        document: Document, trials: List<TrialMatch>, title: String,
        trialsAreEligible: Boolean
    ) {
        document.add(Paragraph(title).addStyle(Styles.tableTitleStyle()))
        var addBlank = false
        for (trial in trials) {
            if (addBlank) {
                if (trialsAreEligible) {
                    document.add(pageBreak())
                } else {
                    document.add(blankLine())
                }
            }
            addTrialDetails(document, trial)
            addBlank = true
        }
    }

    private fun addTrialDetails(document: Document, trial: TrialMatch) {
        val displayFailOnly = !trial.isPotentiallyEligible
        document.add(createTrialIdentificationTable(trial.identification(), trial.isPotentiallyEligible))
        document.add(blankLine())
        val trialEvaluationPerCriterion = toWorstEvaluationPerReference(trial.evaluations())
        if (hasDisplayableEvaluations(trialEvaluationPerCriterion, displayFailOnly)) {
            document.add(makeWrapping(createEvaluationTable(trialEvaluationPerCriterion, displayFailOnly)))
        }
        for (cohort in trial.cohorts()) {
            document.add(blankLine())
            document.add(
                createCohortIdentificationTable(
                    trial.identification().trialId(),
                    cohort.metadata(),
                    cohort.isPotentiallyEligible
                )
            )
            val cohortEvaluationPerCriterion = toWorstEvaluationPerReference(cohort.evaluations())
            if (hasDisplayableEvaluations(cohortEvaluationPerCriterion, displayFailOnly)) {
                document.add(blankLine())
                document.add(makeWrapping(createEvaluationTable(cohortEvaluationPerCriterion, displayFailOnly)))
            }
        }
    }

    private fun createTrialIdentificationTable(identification: TrialIdentification, isPotentiallyEligible: Boolean): Table {
        val indentWidth = 10f
        val keyWidth = 90f
        val valueWidth = contentWidth() - (keyWidth + indentWidth + 10)
        val table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth()).setKeepTogether(true)
        table.addCell(Cells.createSpanningTitle(identification.trialId(), table))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Potentially eligible"))
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Acronym"))
        table.addCell(Cells.createValue(identification.acronym()))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Title"))
        table.addCell(Cells.createValue(identification.title()))
        return table
    }

    private fun createCohortIdentificationTable(
        trialId: String, metadata: CohortMetadata,
        isPotentiallyEligible: Boolean
    ): Table {
        val indentWidth = 10f
        val keyWidth = 90f
        val valueWidth = contentWidth() - (keyWidth + indentWidth + 10)
        val table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth()).setKeepTogether(true)
        table.addCell(Cells.createSpanningTitle(trialId + " - " + metadata.description(), table))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Cohort ID"))
        table.addCell(Cells.createValue(metadata.cohortId()))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Potentially eligible?"))
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Open for inclusion?"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.open())))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Has slots available?"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.slotsAvailable())))
        if (metadata.blacklist()) {
            table.addCell(Cells.createEmpty())
            table.addCell(Cells.createKey("Blacklisted for eligibility?"))
            table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.blacklist())))
        }
        return table
    }

    private fun createEvaluationTable(evaluations: Map<CriterionReference, Evaluation>, displayFailOnly: Boolean): Table {
        val referenceWidth = contentWidth() - (RULE_COL_WIDTH + EVALUATION_COL_WIDTH)
        val table = Tables.createFixedWidthCols(RULE_COL_WIDTH, referenceWidth, EVALUATION_COL_WIDTH).setWidth(contentWidth())
        table.addHeaderCell(Cells.createHeader("Rule"))
        table.addHeaderCell(Cells.createHeader("Reference"))
        table.addHeaderCell(Cells.createHeader("Evaluation"))
        val references: MutableSet<CriterionReference> = Sets.newTreeSet(CriterionReferenceComparator())
        references.addAll(evaluations.keys)
        addEvaluationsOfType(table, references, evaluations, EvaluationResult.NOT_IMPLEMENTED)
        addEvaluationsOfType(table, references, evaluations, EvaluationResult.FAIL)
        if (!displayFailOnly) {
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.WARN)
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.UNDETERMINED)
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.PASS)
            addEvaluationsOfType(table, references, evaluations, EvaluationResult.NOT_EVALUATED)
        }
        return table
    }

    companion object {
        private const val RULE_COL_WIDTH = 30f
        private const val EVALUATION_COL_WIDTH = 150f
        private fun isOpenAndPotentiallyEligible(trial: TrialMatch): Boolean {
            if (!trial.isPotentiallyEligible || !trial.identification().open()) {
                return false
            }
            if (trial.cohorts().isEmpty()) {
                return true
            }
            for (cohort in trial.cohorts()) {
                if (cohort.isPotentiallyEligible && !cohort.metadata().blacklist() && cohort.metadata().open()) {
                    return true
                }
            }
            return false
        }

        private fun hasDisplayableEvaluations(
            evaluationsPerCriterion: Map<CriterionReference, Evaluation>,
            displayFailOnly: Boolean
        ): Boolean {
            if (!displayFailOnly) {
                return !evaluationsPerCriterion.isEmpty()
            }
            for (evaluation in evaluationsPerCriterion.values) {
                if (evaluation.result() == EvaluationResult.FAIL) {
                    return true
                }
            }
            return false
        }

        private fun toWorstEvaluationPerReference(evaluations: Map<Eligibility, Evaluation>): Map<CriterionReference, Evaluation> {
            val worstResultPerCriterion: MutableMap<CriterionReference, EvaluationResult> = Maps.newHashMap()
            for ((key, value) in evaluations) {
                for (reference in key.references()) {
                    val currentWorst = worstResultPerCriterion[reference]
                    val evaluation = value.result()
                    if (currentWorst != null) {
                        val newWorst: EvaluationResult = if (currentWorst.isWorseThan(evaluation)) currentWorst else evaluation
                        worstResultPerCriterion[reference] = newWorst
                    } else {
                        worstResultPerCriterion[reference] = evaluation
                    }
                }
            }
            val worstEvaluationPerCriterion: MutableMap<CriterionReference, Evaluation> = Maps.newHashMap()
            for ((key, evaluation) in evaluations) {
                for (reference in key.references()) {
                    val worst = worstResultPerCriterion[reference]
                    val current = worstEvaluationPerCriterion[reference]
                    if (current == null) {
                        worstEvaluationPerCriterion[reference] = ImmutableEvaluation.builder().from(evaluation).result(worst).build()
                    } else {
                        val updatedBuilder = ImmutableEvaluation.builder()
                            .from(current)
                            .addAllPassSpecificMessages(evaluation.passSpecificMessages())
                            .addAllPassGeneralMessages(evaluation.passGeneralMessages())
                            .addAllWarnSpecificMessages(evaluation.warnSpecificMessages())
                            .addAllWarnGeneralMessages(evaluation.warnGeneralMessages())
                            .addAllUndeterminedSpecificMessages(evaluation.undeterminedSpecificMessages())
                            .addAllUndeterminedGeneralMessages(evaluation.undeterminedGeneralMessages())
                            .addAllFailSpecificMessages(evaluation.failSpecificMessages())
                            .addAllFailGeneralMessages(evaluation.failGeneralMessages())
                        if (evaluation.result() == worst) {
                            updatedBuilder.recoverable(current.recoverable() && evaluation.recoverable())
                        }
                        worstEvaluationPerCriterion[reference] = updatedBuilder.build()
                    }
                }
            }
            return worstEvaluationPerCriterion
        }

        private fun addEvaluationsOfType(
            table: Table, references: Set<CriterionReference>,
            evaluations: Map<CriterionReference, Evaluation>, resultToRender: EvaluationResult
        ) {
            for (reference in references) {
                val evaluation = evaluations[reference]
                if (evaluation!!.result() == resultToRender) {
                    table.addCell(Cells.createContent(reference.id()))
                    table.addCell(Cells.createContent(reference.text()))
                    val evalTable = Tables.createSingleColWithWidth(EVALUATION_COL_WIDTH).setKeepTogether(true)
                    evalTable.addCell(Cells.createEvaluation(evaluation))
                    if (evaluation.result() == EvaluationResult.PASS || evaluation.result() == EvaluationResult.NOT_EVALUATED) {
                        for (passMessage in evaluation.passSpecificMessages()) {
                            evalTable.addCell(Cells.create(Paragraph(passMessage)))
                        }
                    } else if (evaluation.result() == EvaluationResult.WARN) {
                        for (warnMessage in evaluation.warnSpecificMessages()) {
                            evalTable.addCell(Cells.create(Paragraph(warnMessage)))
                        }
                        if (!evaluation.undeterminedSpecificMessages().isEmpty()) {
                            evalTable.addCell(createEvaluationResult(EvaluationResult.UNDETERMINED))
                            for (undeterminedMessage in evaluation.undeterminedSpecificMessages()) {
                                evalTable.addCell(Cells.create(Paragraph(undeterminedMessage)))
                            }
                        }
                    } else if (evaluation.result() == EvaluationResult.UNDETERMINED) {
                        for (undeterminedMessage in evaluation.undeterminedSpecificMessages()) {
                            evalTable.addCell(Cells.create(Paragraph(undeterminedMessage)))
                        }
                    } else if (evaluation.result() == EvaluationResult.FAIL) {
                        for (failMessage in evaluation.failSpecificMessages()) {
                            evalTable.addCell(Cells.create(Paragraph(failMessage)))
                        }
                        if (evaluation.recoverable()) {
                            if (!evaluation.warnSpecificMessages().isEmpty()) {
                                evalTable.addCell(createEvaluationResult(EvaluationResult.WARN))
                                for (warnMessage in evaluation.warnSpecificMessages()) {
                                    evalTable.addCell(Cells.create(Paragraph(warnMessage)))
                                }
                            }
                            if (!evaluation.undeterminedSpecificMessages().isEmpty()) {
                                evalTable.addCell(createEvaluationResult(EvaluationResult.UNDETERMINED))
                                for (undeterminedMessage in evaluation.undeterminedSpecificMessages()) {
                                    evalTable.addCell(Cells.create(Paragraph(undeterminedMessage)))
                                }
                            }
                        }
                    }
                    table.addCell(createContent(evalTable))
                }
            }
        }

        private fun blankLine(): Paragraph {
            return Paragraph(" ")
        }

        private fun pageBreak(): AreaBreak {
            return AreaBreak(AreaBreakType.NEXT_PAGE)
        }
    }
}