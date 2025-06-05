package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialSource

object InterpretedCohortFunctions {

    //TODO("Support multiple sources per trial in trial API")

    fun sourceOrLocationMatchesRequestingSource(source: TrialSource?, locations: Set<String>, requestingSource: TrialSource): Boolean {
        return source == requestingSource || locations.any { location -> TrialSource.fromDescription(location) == requestingSource }
    }
}