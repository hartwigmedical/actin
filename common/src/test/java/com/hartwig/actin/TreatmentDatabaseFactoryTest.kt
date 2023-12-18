package com.hartwig.actin

import com.google.common.io.Resources
import com.hartwig.actin.TreatmentDatabaseFactory.createFromPath
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.IOException
import java.nio.file.NoSuchFileException

class TreatmentDatabaseFactoryTest {
    @Test
    @Throws(IOException::class)
    fun shouldCreateDatabaseFromDirectory() {
        val treatmentDatabase = createFromPath(Resources.getResource("clinical").path)
        Assertions.assertThat(treatmentDatabase).isNotNull()
        Assertions.assertThat(treatmentDatabase.findTreatmentByName("Capecitabine+Oxaliplatin")).isNotNull()
        Assertions.assertThat(treatmentDatabase.findTreatmentByName("CAPECITABINE AND OXALIPLATIN")).isNotNull()
    }

    @Test
    fun shouldThrowExceptionOnCreateWhenJsonFilesAreMissing() {
        Assertions.assertThatThrownBy { createFromPath(Resources.getResource("molecular").path) }
            .isInstanceOf(
                NoSuchFileException::class.java
            )
    }
}
