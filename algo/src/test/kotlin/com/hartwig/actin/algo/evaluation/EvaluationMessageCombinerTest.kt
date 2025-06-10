package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

data class CombinableMessage(val combineKey: String, val message: String) : EvaluationMessage {
    override fun combineBy(): String {
        return combineKey
    }

    override fun combine(other: EvaluationMessage): EvaluationMessage {
        if (other is CombinableMessage)
            return CombinableMessage(combineKey, listOf(message, other.message).joinToString())
        throw IllegalArgumentException()
    }

    override fun toString(): String {
        return message
    }
}

class EvaluationMessageCombinerTest {

    @Test
    fun `Should combine messages in evaluation when keys match`() {
        val messageSet = setOf(CombinableMessage("key", "test1"), CombinableMessage("key", "test2"))
        val evaluation = EvaluationMessageCombiner.combineMessages(
            EvaluationTestFactory.withResult(EvaluationResult.PASS)
                .copy(passMessages = messageSet, warnMessages = messageSet, undeterminedMessages = messageSet, failMessages = messageSet)
        )
        val expectedMessage = "test1, test2"
        assertThat(evaluation.passMessagesStrings()).containsOnly(expectedMessage)
        assertThat(evaluation.warnMessagesStrings()).containsOnly(expectedMessage)
        assertThat(evaluation.failMessagesStrings()).containsOnly(expectedMessage)
        assertThat(evaluation.undeterminedMessagesStrings()).containsOnly(expectedMessage)
    }

    @Test
    fun `Should not combine messages in evaluation when keys don't match`() {
        val messageSet = setOf(CombinableMessage("key1", "test1"), CombinableMessage("key2", "test2"))
        val evaluation = EvaluationMessageCombiner.combineMessages(
            EvaluationTestFactory.withResult(EvaluationResult.PASS)
                .copy(passMessages = messageSet, warnMessages = messageSet, undeterminedMessages = messageSet, failMessages = messageSet)
        )
        val expectedMessage = setOf("test1", "test2")
        assertThat(evaluation.passMessagesStrings()).isEqualTo(expectedMessage)
        assertThat(evaluation.warnMessagesStrings()).isEqualTo(expectedMessage)
        assertThat(evaluation.failMessagesStrings()).isEqualTo(expectedMessage)
        assertThat(evaluation.undeterminedMessagesStrings()).isEqualTo(expectedMessage)
    }
}