package com.hartwig.actin.doid.config

object DoidManualConfigFactory {

    private val MAIN_CANCER_DOIDS = setOf(
        "0050619", // paranasal sinus cancer
        "0060119", // pharynx cancer
        "0080374", // gastroesophageal cancer
        "119", // vaginal cancer
        "219", // colon cancer
        "263", // kidney cancer
        "363", // uterine cancer
        "734", // urethra cancer
        "1245", // vulva cancer
        "1324", // lung cancer
        "1325", // bronchus cancer
        "1380", // endometrial cancer
        "1521", // cecum cancer
        "1612", // breast cancer
        "1725", // peritoneum cancer
        "1781", // thyroid gland cancer
        "1793", // pancreatic cancer
        "1964", // fallopian tube cancer
        "1993", // rectum cancer
        "2394", // ovarian cancer
        "2596", // larynx cancer
        "2998", // testicular cancer
        "3277", // thymus cancer
        "3571", // liver cancer
        "3953", // adrenal gland cancer
        "4159", // skin cancer
        "4362", // cervical cancer
        "4607", // biliary tract cancer
        "4960", // bone marrow cancer
        "5041", // esophageal cancer
        "8564", // lip cancer
        "8649", // tongue cancer
        "8850", // salivary gland cancer
        "9256", // colorectal cancer
        "10283", // prostate cancer
        "10534", // stomach cancer
        "10811", // nasal cavity cancer
        "11054", // urinary bladder cancer
        "11239", // appendix cancer
        "11615", // penile cancer
        "11819", // ureter cancer
        "11920", // tracheal cancer
        "11934", // head and neck cancer
        "14110", // anus cancer
        "3068" // glioblastoma
    )

    private val ADENO_SQUAMOUS_MAPPINGS = setOf(
        createMapping("4829", "3907", "3910"), // Lung
        createMapping("5623", "5514", "3458"), // Breast
        createMapping("5624", "5537", "4896"), // Bile duct
        createMapping("5625", "3748", "4914"), // Esophageal
        createMapping("5626", "5530", "4923"), // Thymus
        createMapping("5627", "5535", "3500"), // Gallbladder
        createMapping("5628", "5527", "3502"), // Ampulla of vater
        createMapping("5629", "234", "5519"), // Colon
        createMapping("5630", "6961", "6316"), // Bartholin's gland / Vulva
        createMapping("5631", "5533", "2870"), // Endometrial
        createMapping("5634", "10287", "2526"), // Prostate
        createMapping("5635", "5516", "5517"), // Stomach
        createMapping("5636", "3744", "3702"), // Cervical
        createMapping("5637", "0080323", "4074"), // Stomach
        createMapping("4830", "1749", "299") // Adenosquamous carcinoma
    )

    private val ADDITIONAL_DOIDS_PER_DOID = mapOf(
        "4829" to "3908", // Lung adenosquamous > NSCLC
        "6438" to "6039", // Malignant choroid melanoma > Uveal melanoma
        "7807" to "6039", // Choroid necrotic melanoma > Uveal melanoma
        "6994" to "6039", // Iris melanoma > Uveal melanoma
        "6524" to "6039", // Ciliary body melanoma > Uveal melanoma
        "6039" to "1752", // Uveal melanoma > Ocular melanoma
        "1751" to "1752", // Malignant conjunctival melanoma > Ocular melanoma
        "234" to "0050861", // Colon adenocarcinoma > Colorectal adenocarcinoma
        "1996" to "0050861", // Rectum adenocarcinoma > Colorectal adenocarcinoma
        "1520" to "0080199" // Colon carcinoma > Colorectal carcinoma
    )

    fun create(): DoidManualConfig {
        return DoidManualConfig(
            mainCancerDoids = MAIN_CANCER_DOIDS,
            adenoSquamousMappings = ADENO_SQUAMOUS_MAPPINGS,
            additionalDoidsPerDoid = ADDITIONAL_DOIDS_PER_DOID
        )
    }

    private fun createMapping(adenoSquamousDoid: String, squamousDoid: String, adenoDoid: String): AdenoSquamousMapping {
        return AdenoSquamousMapping(
            adenoSquamousDoid = adenoSquamousDoid,
            squamousDoid = squamousDoid,
            adenoDoid = adenoDoid
        )
    }
}
