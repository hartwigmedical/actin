package com.hartwig.actin.molecular.datamodel.driver;

public interface CopyNumberDriver extends Driver, GeneAlteration {

    int minCopies();

    int maxCopies();

}
