package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory

data class EventsWithMessages(val events: Set<String>, val specificMessage: String, val generalMessage: String)

object MolecularEventUtil {

    fun evaluatePotentialWarnsForEventGroups(eventsWithMessages: List<EventsWithMessages>): Evaluation? {
        val (warnEvents, warnSpecificMessages, warnGeneralMessages) = eventsWithMessages.filter { (events, _, _) -> events.isNotEmpty() }
            .fold(
                Triple(
                    emptySet<String>(),
                    emptySet<String>(),
                    emptySet<String>()
                )
            ) { (allEvents, specificMessages, generalMessages), (events, specific, general) ->
                Triple(allEvents + events, specificMessages + specific, generalMessages + general)
            }

        return if (warnEvents.isNotEmpty() && warnSpecificMessages.isNotEmpty() && warnGeneralMessages.isNotEmpty()) {
            Evaluation(
                result = EvaluationResult.WARN,
                recoverable = false,
                warnSpecificMessages = warnSpecificMessages,
                warnGeneralMessages = warnGeneralMessages,
                inclusionMolecularEvents = warnEvents
            )
        } else null
    }

    fun noMolecularEvaluation(): Evaluation {
        return EvaluationFactory.undetermined("No molecular data", "No molecular data")
    }
}