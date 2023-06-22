package com.hartwig.actin.clinical.serialization;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.DrugClass;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;

import org.junit.Test;

public class DrugJsonTest {

    private static final String CLINICAL_DIRECTORY = Resources.getResource("clinical").getPath();
    private static final String DRUG_JSON = CLINICAL_DIRECTORY + File.separator + "drug.json";

    @Test
    public void shouldReadDrugsFromJsonFile() throws IOException {
        List<Drug> drugs = DrugJson.read(DRUG_JSON);
        assertEquals(2, drugs.size());
        assertEquals(ImmutableDrug.builder().name("Capecitabine").addDrugClasses(DrugClass.ANTIMETABOLITE).build(), drugs.get(0));
        assertEquals(ImmutableDrug.builder().name("Oxaliplatin").addDrugClasses(DrugClass.PLATINUM_COMPOUND).build(), drugs.get(1));
    }
}