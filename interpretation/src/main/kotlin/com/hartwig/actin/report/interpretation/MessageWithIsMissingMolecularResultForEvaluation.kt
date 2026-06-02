package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.EvaluationMessage

class MessageWithIsMissingMolecularResultForEvaluation(
    val message: String,
    val isMissingMolecularResultForEvaluation: Boolean
) : EvaluationMessage {

    override fun combineBy(): String {
        return message
    }

    override fun combine(other: EvaluationMessage): EvaluationMessage {
        return this
    }

    override fun toString() = message

    override fun equals(other: Any?) = (other as? EvaluationMessage)?.toString() == message

    override fun hashCode() = message.hashCode()
}