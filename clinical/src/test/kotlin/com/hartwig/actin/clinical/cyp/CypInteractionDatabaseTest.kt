package com.hartwig.actin.clinical.cyp

import org.junit.Assert.*
import org.junit.Test

class CypInteractionDatabaseTest{

    @Test
    fun shouldReadDatabaseFromTsvFile() {
        val victim = CypInteractionDatabase.readFromFile(System.getProperty("user.dir") + "/src/test/resources/cyp/cyp_interactions.tsv")

    }

}