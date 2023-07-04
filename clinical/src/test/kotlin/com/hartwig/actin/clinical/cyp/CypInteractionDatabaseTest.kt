package com.hartwig.actin.clinical.cyp

import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CypInteractionDatabaseTest {

    @Test
    fun shouldReadDatabaseFromTsvFile() {
        val victim = CypInteractionDatabase.readFromFile(System.getProperty("user.dir") + "/src/test/resources/cyp/cyp_interactions.tsv")
        assertThat(victim.findByName("abiraterone")).containsExactly(createInteraction(CypInteraction.Type.INHIBITOR, CypInteraction.Strength.MODERATE, "2D6"),
            createInteraction(CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.MODERATE_SENSITIVE, "3A4"))
    }

    private fun createInteraction(type: CypInteraction.Type, strength: CypInteraction.Strength, cyp: String): ImmutableCypInteraction? =
        ImmutableCypInteraction.builder().type(type).strength(strength).cyp(cyp).build()

}