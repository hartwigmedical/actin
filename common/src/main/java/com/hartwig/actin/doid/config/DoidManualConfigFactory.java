package com.hartwig.actin.doid.config;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class DoidManualConfigFactory {

    private static final Set<String> MAIN_CANCER_DOIDS = Sets.newHashSet();

    static {
        MAIN_CANCER_DOIDS.add("0050619"); // paranasal sinus cancer
        MAIN_CANCER_DOIDS.add("0060119"); // pharynx cancer
        MAIN_CANCER_DOIDS.add("0080374"); // gastroesophageal cancer
        MAIN_CANCER_DOIDS.add("119"); // vaginal cancer
        MAIN_CANCER_DOIDS.add("219"); // colon cancer
        MAIN_CANCER_DOIDS.add("263"); // kidney cancer
        MAIN_CANCER_DOIDS.add("363"); // uterine cancer
        MAIN_CANCER_DOIDS.add("734"); // urethra cancer
        MAIN_CANCER_DOIDS.add("1245"); // vulva cancer
        MAIN_CANCER_DOIDS.add("1324"); // lung cancer
        MAIN_CANCER_DOIDS.add("1325"); // bronchus cancer
        MAIN_CANCER_DOIDS.add("1380"); // endometrial cancer
        MAIN_CANCER_DOIDS.add("1521"); // cecum cancer
        MAIN_CANCER_DOIDS.add("1612"); // breast cancer
        MAIN_CANCER_DOIDS.add("1725"); // peritoneum cancer
        MAIN_CANCER_DOIDS.add("1781"); // thyroid gland cancer
        MAIN_CANCER_DOIDS.add("1793"); // pancreatic cancer
        MAIN_CANCER_DOIDS.add("1964"); // fallopian tube cancer
        MAIN_CANCER_DOIDS.add("1993"); // rectum cancer
        MAIN_CANCER_DOIDS.add("2394"); // ovarian cancer
        MAIN_CANCER_DOIDS.add("2596"); // larynx cancer
        MAIN_CANCER_DOIDS.add("2998"); // testicular cancer
        MAIN_CANCER_DOIDS.add("3277"); // thymus cancer
        MAIN_CANCER_DOIDS.add("3571"); // liver cancer
        MAIN_CANCER_DOIDS.add("3953"); // adrenal gland cancer
        MAIN_CANCER_DOIDS.add("3996"); // urinary system cancer
        MAIN_CANCER_DOIDS.add("4159"); // skin cancer
        MAIN_CANCER_DOIDS.add("4362"); // cervical cancer
        MAIN_CANCER_DOIDS.add("4607"); // biliary tract cancer
        MAIN_CANCER_DOIDS.add("4960"); // bone marrow cancer
        MAIN_CANCER_DOIDS.add("5041"); // esophageal cancer
        MAIN_CANCER_DOIDS.add("8564"); // lip cancer
        MAIN_CANCER_DOIDS.add("8649"); // tongue cancer
        MAIN_CANCER_DOIDS.add("8850"); // salivary gland cancer
        MAIN_CANCER_DOIDS.add("9256"); // colorectal cancer
        MAIN_CANCER_DOIDS.add("10283"); // prostate cancer
        MAIN_CANCER_DOIDS.add("10534"); // stomach cancer
        MAIN_CANCER_DOIDS.add("10811"); // nasal cavity cancer
        MAIN_CANCER_DOIDS.add("11054"); // urinary bladder cancer
        MAIN_CANCER_DOIDS.add("11239"); // appendix cancer
        MAIN_CANCER_DOIDS.add("11615"); // penile cancer
        MAIN_CANCER_DOIDS.add("11819"); // ureter cancer
        MAIN_CANCER_DOIDS.add("11920"); // tracheal cancer
        MAIN_CANCER_DOIDS.add("11934"); // head and neck cancer
        MAIN_CANCER_DOIDS.add("14110"); // anus cancer
    }

    private DoidManualConfigFactory() {
    }

    @NotNull
    public static DoidManualConfig create() {
        return ImmutableDoidManualConfig.builder().mainCancerDoids(MAIN_CANCER_DOIDS).build();
    }
}
