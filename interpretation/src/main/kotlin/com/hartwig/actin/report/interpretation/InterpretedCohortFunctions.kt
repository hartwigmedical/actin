package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialSource

object InterpretedCohortFunctions {

    fun sourceOrLocationMatchesRequestingSource(sources: Set<TrialSource>, locations: Set<String>, requestingSource: TrialSource): Boolean {
        return requestingSource in sources || locations.any { location -> TrialSource.fromDescription(location) == requestingSource }
    }
}