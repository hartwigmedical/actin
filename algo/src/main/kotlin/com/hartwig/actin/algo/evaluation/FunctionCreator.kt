package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.trial.EligibilityFunction

typealias FunctionCreator = (eligibilityFunction: EligibilityFunction) -> EvaluationFunction