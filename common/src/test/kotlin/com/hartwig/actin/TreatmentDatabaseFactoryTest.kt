package com.hartwig.actin

import com.google.common.io.Resources
import com.hartwig.actin.TreatmentDatabaseFactory.createFromPath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.nio.file.NoSuchFileException

class TreatmentDatabaseFactoryTest {

    @Test
    fun `Should create database from directory`() {
        val treatmentDatabase = createFromPath(Resources.getResource("clinical").path)
        assertThat(treatmentDatabase).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("Capecitabine+Oxaliplatin")).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("CAPECITABINE AND OXALIPLATIN")).isNotNull()
    }

    @Test
    fun `Should throw exception on create when files are missing`() {
        assertThatThrownBy { createFromPath(Resources.getResource("molecular").path) }
            .isInstanceOf(NoSuchFileException::class.java)
    }
}
