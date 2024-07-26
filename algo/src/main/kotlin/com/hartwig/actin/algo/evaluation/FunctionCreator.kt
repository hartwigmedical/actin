package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.trial.datamodel.EligibilityFunction

typealias FunctionCreator = (eligibilityFunction: EligibilityFunction) -> EvaluationFunction