package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

//TODO (CB)!
data class EventsWithMessages(val events: Collection<String>?, val message: String)

object MolecularEventUtil {

    fun evaluatePotentialWarnsForEventGroups(eventsWithMessages: List<EventsWithMessages>): Evaluation? {
        val (warnEvents, warnMessages) = eventsWithMessages
            .filter { (events, _) -> !events.isNullOrEmpty() }
            .fold(
                Pair(emptySet<String>(), emptySet<String>())
            ) { (allEvents, allMessages), (events, messages) ->
                Pair(allEvents + events!!, allMessages + messages)
            }

        return if (warnEvents.isNotEmpty() && warnMessages.isNotEmpty()) {
            Evaluation(
                result = EvaluationResult.WARN,
                recoverable = false,
                warnMessages = warnMessages,
                inclusionMolecularEvents = warnEvents
            )
        } else null
    }
}