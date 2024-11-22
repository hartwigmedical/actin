package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireVersion.Companion.version
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuestionnaireVersionTest {

    @Test
    fun `Should resolve all versions`() {
        assertVersion(QuestionnaireVersion.V1_7, TestQuestionnaireFactory.createTestQuestionnaireValueV1_7())
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
    fun `Should throw exception on unresolved version`() {
        version(TestQuestionnaireFactory.entryWithText("Not an entry"))
    }

    private fun assertVersion(expected: QuestionnaireVersion, questionnaire: String) {
        assertThat(version(TestQuestionnaireFactory.entryWithText(questionnaire))).isEqualTo(expected)
    }
}