package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Cells.createContent
import com.hartwig.actin.report.pdf.util.Cells.createEvaluationResult
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.hartwig.actin.trial.sort.CriterionReferenceComparator
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType

class TrialMatchingDetailsChapter(private val report: Report, override val include: Boolean) : ReportChapter {
    override fun name(): String {
        return "Trial Matching Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        val (eligible: List<TrialMatch>, nonEligible: List<TrialMatch>) = report.treatmentMatch.trialMatches
            .map(TrialClassification::createForTrialMatch)
            .fold(TrialClassification(), TrialClassification::combine)

        if (eligible.isNotEmpty()) {
            addTrialMatches(document, eligible, "Potentially eligible open trials & cohorts", true)
        }
        if (nonEligible.isNotEmpty()) {
            if (eligible.isNotEmpty()) {
                document.add(pageBreak())
            }
            addTrialMatches(document, nonEligible, "Other trials & cohorts", false)
        }
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
        document.add(createTrialIdentificationTable(trial.identification, trial.isPotentiallyEligible))
        document.add(blankLine())
        val trialEvaluationPerCriterion = toWorstEvaluationPerReference(trial.evaluations)
        if (hasDisplayableEvaluations(trialEvaluationPerCriterion, displayFailOnly)) {
            document.add(makeWrapping(createEvaluationTable(trialEvaluationPerCriterion, displayFailOnly)))
        }
        for (cohort in trial.cohorts) {
            document.add(blankLine())
            document.add(
                createCohortIdentificationTable(
                    trial.identification.trialId,
                    cohort.metadata,
                    cohort.isPotentiallyEligible
                )
            )
            val cohortEvaluationPerCriterion = toWorstEvaluationPerReference(cohort.evaluations)
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
        table.addCell(Cells.createSpanningTitle(identification.trialId, table))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Potentially eligible"))
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Acronym"))
        table.addCell(Cells.createValue(identification.acronym))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Title"))
        table.addCell(Cells.createValue(identification.title))
        return table
    }

    private fun createCohortIdentificationTable(trialId: String, metadata: CohortMetadata, isPotentiallyEligible: Boolean): Table {
        val indentWidth = 10f
        val keyWidth = 90f
        val valueWidth = contentWidth() - (keyWidth + indentWidth + 10)
        val table = Tables.createFixedWidthCols(indentWidth, keyWidth, valueWidth).setWidth(contentWidth()).setKeepTogether(true)
        table.addCell(Cells.createSpanningTitle(trialId + " - " + metadata.description, table))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Cohort ID"))
        table.addCell(Cells.createValue(metadata.cohortId))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Potentially eligible?"))
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Open for inclusion?"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.open)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Has slots available?"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.slotsAvailable)))
        if (metadata.blacklist) {
            table.addCell(Cells.createEmpty())
            table.addCell(Cells.createKey("Blacklisted for eligibility?"))
            table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.blacklist)))
        }
        return table
    }

    private fun createEvaluationTable(evaluations: Map<CriterionReference, Evaluation>, displayFailOnly: Boolean): Table {
        val referenceWidth = contentWidth() - (RULE_COL_WIDTH + EVALUATION_COL_WIDTH)
        val table = Tables.createFixedWidthCols(RULE_COL_WIDTH, referenceWidth, EVALUATION_COL_WIDTH).setWidth(contentWidth())
        table.addHeaderCell(Cells.createHeader("Rule"))
        table.addHeaderCell(Cells.createHeader("Reference"))
        table.addHeaderCell(Cells.createHeader("Evaluation"))
        val references = evaluations.keys.sortedWith(CriterionReferenceComparator()).distinct()
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

        private fun hasDisplayableEvaluations(
            evaluationsPerCriterion: Map<CriterionReference, Evaluation>,
            displayFailOnly: Boolean
        ): Boolean {
            if (!displayFailOnly) {
                return evaluationsPerCriterion.isNotEmpty()
            }
            for (evaluation in evaluationsPerCriterion.values) {
                if (evaluation.result == EvaluationResult.FAIL) {
                    return true
                }
            }
            return false
        }

        private fun toWorstEvaluationPerReference(evaluations: Map<Eligibility, Evaluation>): Map<CriterionReference, Evaluation> {
            val worstResultPerCriterion: MutableMap<CriterionReference, EvaluationResult> = mutableMapOf()
            for ((key, value) in evaluations) {
                for (reference in key.references) {
                    val currentWorst = worstResultPerCriterion[reference]
                    val evaluation = value.result
                    if (currentWorst != null) {
                        val newWorst: EvaluationResult = if (currentWorst.isWorseThan(evaluation)) currentWorst else evaluation
                        worstResultPerCriterion[reference] = newWorst
                    } else {
                        worstResultPerCriterion[reference] = evaluation
                    }
                }
            }
            val worstEvaluationPerCriterion: MutableMap<CriterionReference, Evaluation> = mutableMapOf()
            for ((key, evaluation) in evaluations) {
                for (reference in key.references) {
                    val worst = worstResultPerCriterion[reference]!!
                    val current = worstEvaluationPerCriterion[reference]
                    if (current == null) {
                        worstEvaluationPerCriterion[reference] = evaluation.copy(result = worst)
                    } else {
                        val recoverable = if (evaluation.result == worst) {
                            current.recoverable && evaluation.recoverable
                        } else current.recoverable
                        worstEvaluationPerCriterion[reference] = current.addMessagesAndEvents(evaluation).copy(recoverable = recoverable)
                    }
                }
            }
            return worstEvaluationPerCriterion
        }

        private fun addEvaluationsOfType(
            table: Table, references: Iterable<CriterionReference>,
            evaluations: Map<CriterionReference, Evaluation>, resultToRender: EvaluationResult
        ) {
            for (reference in references) {
                val evaluation = evaluations[reference]
                if (evaluation!!.result == resultToRender) {
                    table.addCell(createContent(reference.id))
                    table.addCell(createContent(reference.text))
                    val evalTable = Tables.createSingleColWithWidth(EVALUATION_COL_WIDTH).setKeepTogether(true)
                    evalTable.addCell(Cells.createEvaluation(evaluation))
                    when (evaluation.result) {
                        EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED -> {
                            for (passMessage in evaluation.passSpecificMessages) {
                                evalTable.addCell(createMessageCell(passMessage))
                            }
                        }

                        EvaluationResult.WARN -> {
                            for (warnMessage in evaluation.warnSpecificMessages) {
                                evalTable.addCell(createMessageCell(warnMessage))
                            }
                            if (evaluation.undeterminedSpecificMessages.isNotEmpty()) {
                                evalTable.addCell(createEvaluationResult(EvaluationResult.UNDETERMINED))
                                for (undeterminedMessage in evaluation.undeterminedSpecificMessages) {
                                    evalTable.addCell(createMessageCell(undeterminedMessage))
                                }
                            }
                        }

                        EvaluationResult.UNDETERMINED -> {
                            for (undeterminedMessage in evaluation.undeterminedSpecificMessages) {
                                evalTable.addCell(createMessageCell(undeterminedMessage))
                            }
                        }

                        EvaluationResult.FAIL -> {
                            for (failMessage in evaluation.failSpecificMessages) {
                                evalTable.addCell(createMessageCell(failMessage))
                            }
                            if (evaluation.recoverable) {
                                if (evaluation.warnSpecificMessages.isNotEmpty()) {
                                    evalTable.addCell(createEvaluationResult(EvaluationResult.WARN))
                                    for (warnMessage in evaluation.warnSpecificMessages) {
                                        evalTable.addCell(createMessageCell(warnMessage))
                                    }
                                }
                                if (evaluation.undeterminedSpecificMessages.isNotEmpty()) {
                                    evalTable.addCell(createEvaluationResult(EvaluationResult.UNDETERMINED))
                                    for (undeterminedMessage in evaluation.undeterminedSpecificMessages) {
                                        evalTable.addCell(createMessageCell(undeterminedMessage))
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                    table.addCell(createContent(evalTable))
                }
            }
        }

        private fun createMessageCell(message: String): Cell {
            return Cells.create(Paragraph(insertSpacesAroundPlus(message)))
        }

        fun insertSpacesAroundPlus(input: String): String {
            // Regular expression to match "+" surrounded by non-whitespace characters
            val regex = "(\\S)\\+(\\S)".toRegex()

            // Replace matched instances with spaces around "+"
            return input.replace(regex, "$1 + $2")
        }

        private fun blankLine(): Paragraph {
            return Paragraph(" ")
        }

        private fun pageBreak(): AreaBreak {
            return AreaBreak(AreaBreakType.NEXT_PAGE)
        }
    }

    private data class TrialClassification(val eligible: List<TrialMatch> = emptyList(), val nonEligible: List<TrialMatch> = emptyList()) {

        fun combine(other: TrialClassification): TrialClassification {
            return TrialClassification(eligible + other.eligible, nonEligible + other.nonEligible)
        }

        companion object {
            fun createForTrialMatch(trialMatch: TrialMatch): TrialClassification {
                return if (isOpenAndPotentiallyEligible(trialMatch)) {
                    TrialClassification(eligible = listOf(trialMatch))
                } else {
                    TrialClassification(nonEligible = listOf(trialMatch))
                }
            }

            private fun isOpenAndPotentiallyEligible(trial: TrialMatch): Boolean {
                if (!trial.isPotentiallyEligible || !trial.identification.open) {
                    return false
                }
                if (trial.cohorts.isEmpty()) {
                    return true
                }
                return trial.cohorts.any { it.isPotentiallyEligible && !it.metadata.blacklist && it.metadata.open }
            }
        }
    }
}