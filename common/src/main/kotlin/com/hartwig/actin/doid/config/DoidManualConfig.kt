package com.hartwig.actin.doid.config

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
    AdenoSquamousMapping(adenoSquamousDoid = "4829", squamousDoid = "3907", adenoDoid = "3910"), // Lung
    AdenoSquamousMapping(adenoSquamousDoid = "5623", squamousDoid = "5514", adenoDoid = "3458"), // Breast
    AdenoSquamousMapping(adenoSquamousDoid = "5624", squamousDoid = "5537", adenoDoid = "4896"), // Bile duct
    AdenoSquamousMapping(adenoSquamousDoid = "5625", squamousDoid = "3748", adenoDoid = "4914"), // Esophageal
    AdenoSquamousMapping(adenoSquamousDoid = "5626", squamousDoid = "5530", adenoDoid = "4923"), // Thymus
    AdenoSquamousMapping(adenoSquamousDoid = "5627", squamousDoid = "5535", adenoDoid = "3500"), // Gallbladder
    AdenoSquamousMapping(adenoSquamousDoid = "5628", squamousDoid = "5527", adenoDoid = "3502"), // Ampulla of vater
    AdenoSquamousMapping(adenoSquamousDoid = "5629", squamousDoid = "234", adenoDoid = "5519"), // Colon
    AdenoSquamousMapping(adenoSquamousDoid = "5630", squamousDoid = "6961", adenoDoid = "6316"), // Bartholin's gland / Vulva
    AdenoSquamousMapping(adenoSquamousDoid = "5631", squamousDoid = "5533", adenoDoid = "2870"), // Endometrial
    AdenoSquamousMapping(adenoSquamousDoid = "5634", squamousDoid = "10287", adenoDoid = "2526"), // Prostate
    AdenoSquamousMapping(adenoSquamousDoid = "5635", squamousDoid = "5516", adenoDoid = "5517"), // Stomach
    AdenoSquamousMapping(adenoSquamousDoid = "5636", squamousDoid = "3744", adenoDoid = "3702"), // Cervical
    AdenoSquamousMapping(adenoSquamousDoid = "5637", squamousDoid = "0080323", adenoDoid = "4074"), // Stomach
    AdenoSquamousMapping(adenoSquamousDoid = "4830", squamousDoid = "1749", adenoDoid = "299") // Adenosquamous carcinoma
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

private val CHILD_TO_PARENT_RELATIONSHIPS_TO_EXCLUDE = setOf(
    "235" to "1475" // colonic benign neoplasm > lymphangioma
)

data class DoidManualConfig(
    val mainCancerDoids: Set<String>,
    val adenoSquamousMappings: Set<AdenoSquamousMapping>,
    val additionalDoidsPerDoid: Map<String, String>,
    val childToParentRelationshipsToExclude: Set<Pair<String, String>>
) {

    companion object {
        fun create(): DoidManualConfig {
            return DoidManualConfig(
                mainCancerDoids = MAIN_CANCER_DOIDS,
                adenoSquamousMappings = ADENO_SQUAMOUS_MAPPINGS,
                additionalDoidsPerDoid = ADDITIONAL_DOIDS_PER_DOID,
                childToParentRelationshipsToExclude = CHILD_TO_PARENT_RELATIONSHIPS_TO_EXCLUDE
            )
        }
    }
}
