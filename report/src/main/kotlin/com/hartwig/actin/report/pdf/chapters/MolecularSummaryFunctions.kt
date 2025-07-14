package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.ExperimentType.HARTWIG_TARGETED
import com.hartwig.actin.datamodel.molecular.ExperimentType.PANEL

object MolecularSummaryFunctions {

    enum class SummaryType {
        SHORT_SUMMARY,
        LONG_SUMMARY,
        FULL
    }

    fun selectMolecularSummary(isMolecularDetailsPage: Boolean, experimentType: ExperimentType? = null): SummaryType {
        return when {
            isMolecularDetailsPage -> SummaryType.FULL
            experimentType == HARTWIG_TARGETED || experimentType == PANEL -> SummaryType.SHORT_SUMMARY
            else -> SummaryType.LONG_SUMMARY
        }
    }
}