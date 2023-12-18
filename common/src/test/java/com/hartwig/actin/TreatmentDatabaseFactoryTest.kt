package com.hartwig.actin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import com.google.common.io.Resources;

import org.junit.Test;

public class TreatmentDatabaseFactoryTest {

    @Test
    public void shouldCreateDatabaseFromDirectory() throws IOException {
        TreatmentDatabase treatmentDatabase = TreatmentDatabaseFactory.createFromPath(Resources.getResource("clinical").getPath());
        assertThat(treatmentDatabase).isNotNull();
        assertThat(treatmentDatabase.findTreatmentByName("Capecitabine+Oxaliplatin")).isNotNull();
        assertThat(treatmentDatabase.findTreatmentByName("CAPECITABINE AND OXALIPLATIN")).isNotNull();

    }

    @Test
    public void shouldThrowExceptionOnCreateWhenJsonFilesAreMissing() {
        assertThatThrownBy(() -> TreatmentDatabaseFactory.createFromPath(Resources.getResource("molecular").getPath())).isInstanceOf(
                NoSuchFileException.class);
    }
}
