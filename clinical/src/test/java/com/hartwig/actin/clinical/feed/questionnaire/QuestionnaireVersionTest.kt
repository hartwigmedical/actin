package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireVersion.Companion.version
import org.junit.Assert
import org.junit.Test

class QuestionnaireVersionTest {
    @Test
    fun canResolveAllVersions() {
        assertVersion(QuestionnaireVersion.V1_6, TestQuestionnaireFactory.createTestQuestionnaireValueV1_6())
        assertVersion(QuestionnaireVersion.V1_5, TestQuestionnaireFactory.createTestQuestionnaireValueV1_5())
        assertVersion(QuestionnaireVersion.V1_4, TestQuestionnaireFactory.createTestQuestionnaireValueV1_4())
        assertVersion(QuestionnaireVersion.V1_3, TestQuestionnaireFactory.createTestQuestionnaireValueV1_3())
        assertVersion(QuestionnaireVersion.V1_2, TestQuestionnaireFactory.createTestQuestionnaireValueV1_2())
        assertVersion(QuestionnaireVersion.V1_1, TestQuestionnaireFactory.createTestQuestionnaireValueV1_1())
        assertVersion(QuestionnaireVersion.V1_0, TestQuestionnaireFactory.createTestQuestionnaireValueV1_0())
        assertVersion(QuestionnaireVersion.V0_2, TestQuestionnaireFactory.createTestQuestionnaireValueV0_2())
        assertVersion(QuestionnaireVersion.V0_1, TestQuestionnaireFactory.createTestQuestionnaireValueV0_1())
    }

    @Test(expected = IllegalStateException::class)
    fun crashOnUnresolvedVersion() {
        version(entry("Not an entry"))
    }

    companion object {
        private fun assertVersion(expected: QuestionnaireVersion, questionnaire: String) {
            Assert.assertEquals(expected, version(entry(questionnaire)))
        }

        private fun entry(questionnaire: String): QuestionnaireEntry {
            return ImmutableQuestionnaireEntry.builder()
                .from(TestQuestionnaireFactory.createTestQuestionnaireEntry())
                .text(questionnaire)
                .build()
        }
    }
}