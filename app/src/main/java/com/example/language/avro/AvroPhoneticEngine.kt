package com.example.language.avro

import com.example.language.dictionary.BanglishDictionary

/**
 * High-performance, comprehensive Avro-style Phonetic Engine inspired by Ridmik Keyboard.
 * Converts English phonetic typing (Banglish) to Unicode Bengali (বাংলা).
 * Supports comprehensive vowels, consonants, Kar, Fola (য-ফলা, র-ফলা, ব-ফলা),
 * Reph (রেফ), Hasanta, Khondo-To, Bisorgo, Chandrabindu, Bengali digits, and complex Juktakkhor.
 */
class AvroPhoneticEngine {

    companion object {
        // Bengali Unicode Constants
        const val HASANTA = "\u09CD" // ্
        const val DARI = "\u09F7"    // ।
        const val ANUSBAR = "\u0982" // ং
        const val BISARGA = "\u0983" // ঃ
        const val CHANDRABINDU = "\u0981" // ঁ
        const val KHANDA_TA = "\u09CE" // ৎ
        const val ZWNJ = "\u200C"
        const val ZWJ = "\u200D"

        // Bengali Digits
        private val BENGALI_DIGITS = mapOf(
            '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
            '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
        )

        // Independent Vowels
        private val INDEPENDENT_VOWELS = mapOf(
            "a" to "অ",
            "aa" to "আ",
            "A" to "আ",
            "i" to "ই",
            "I" to "ঈ",
            "ee" to "ঈ",
            "u" to "উ",
            "U" to "ঊ",
            "oo" to "ঊ",
            "rri" to "ঋ",
            "rRI" to "ঋ",
            "ri" to "ঋ",
            "e" to "এ",
            "E" to "এ",
            "oi" to "ঐ",
            "OI" to "ঐ",
            "o" to "ও",
            "O" to "ও",
            "ou" to "ঔ",
            "OU" to "ঔ",
            "ow" to "ঔ"
        )

        // Dependent Vowel Signs (Kar)
        private val VOWEL_KARS = mapOf(
            "a" to "া",
            "aa" to "া",
            "A" to "া",
            "i" to "ি",
            "I" to "ী",
            "ee" to "ী",
            "u" to "ু",
            "U" to "ূ",
            "oo" to "ূ",
            "rri" to "ৃ",
            "rRI" to "ৃ",
            "ri" to "ৃ",
            "e" to "ে",
            "E" to "ে",
            "oi" to "ৈ",
            "OI" to "ৈ",
            "o" to "ো",
            "O" to "ো",
            "ou" to "ৌ",
            "OU" to "ৌ",
            "ow" to "ৌ"
        )

        // Consonant & Juktakkhor mappings (Ordered by length descending for greedy match)
        private val CONSONANTS = mapOf(
            // Quadruple / Triple consonants
            "kkhN" to "ক্ষ্ণ",
            "kkhm" to "ক্ষ্ম",
            "cchb" to "চ্ছ্ব",
            "shchh" to "শ্ছ",
            "Shk" to "ষ্ক",
            "Shkh" to "ষ্খ",
            "ShTh" to "ষ্ঠ",
            "ShN" to "ষ্ণ",
            "Shph" to "ষ্ফ",
            "Shm" to "ষ্ম",
            "nggh" to "ঙ্ঘ",
            "nchh" to "ঞ্ছ",

            // Compound Juktakkhors (2-3 chars)
            "kkh" to "ক্ষ",
            "ksh" to "ক্ষ",
            "gny" to "জ্ঞ",
            "jny" to "জ্ঞ",
            "cch" to "চ্ছ",
            "jjh" to "জ্ঝ",
            "ngk" to "ঙ্ক",
            "ngkh" to "ঙ্খ",
            "ngg" to "ঙ্গ",
            "nch" to "ঞ্চ",
            "nj" to "ঞ্জ",
            "njh" to "ঞ্ঝ",
            "shch" to "শ্চ",
            "shn" to "শ্ন",
            "shb" to "শ্ব",
            "shm" to "শ্ম",
            "shl" to "শ্ল",
            "ShT" to "ষ্ট",
            "Shp" to "ষ্প",
            "sk" to "স্ক",
            "skh" to "স্খ",
            "st" to "স্ত",
            "sth" to "স্থ",
            "sp" to "স্প",
            "sph" to "স্ফ",
            "sm" to "স্ম",
            "sn" to "স্ন",
            "sl" to "স্ল",
            "sb" to "স্ব",
            "nt" to "ন্ত",
            "nth" to "ন্থ",
            "nd" to "ন্দ",
            "ndh" to "ন্ধ",
            "nn" to "ন্ন",
            "nm" to "ন্ম",
            "mp" to "ম্প",
            "mph" to "ম্ফ",
            "mb" to "ম্ব",
            "mbh" to "ম্ভ",
            "mm" to "ম্ম",
            "ml" to "ম্ল",
            "pt" to "প্ত",
            "ps" to "প্স",
            "pn" to "প্ন",
            "pl" to "প্ল",
            "bd" to "ব্দ",
            "bdh" to "ব্ধ",
            "bb" to "ব্ব",
            "bl" to "ব্ল",
            "kt" to "ক্ত",
            "kn" to "ক্ন",
            "km" to "ক্ম",
            "kl" to "ক্ল",
            "gd" to "গ্দ",
            "gdh" to "গ্ধ",
            "gn" to "গ্ন",
            "gm" to "গ্ম",
            "gl" to "গ্ল",
            "tt" to "ত্ত",
            "tth" to "ত্থ",
            "tn" to "ত্ন",
            "tm" to "ত্ম",
            "tb" to "ত্ব",
            "dd" to "দ্দ",
            "ddh" to "দ্ধ",
            "db" to "দ্ব",
            "dm" to "দ্ম",
            "dhn" to "ধ্ন",
            "dhm" to "ধ্ম",
            "dhb" to "ধ্ব",
            "hm" to "হ্ম",
            "hn" to "হ্ন",
            "hN" to "হ্ণ",
            "hl" to "হ্ল",
            "hb" to "হ্ব",
            "hr" to "হ্র",
            "hri" to "হৃ",
            "NT" to "ণ্ট",
            "NTh" to "ণ্ঠ",
            "ND" to "ণ্ড",
            "NDh" to "ণ্ঢ",
            "NN" to "ণ্ণ",

            // 2-character basic consonants
            "kh" to "খ",
            "gh" to "ঘ",
            "Ng" to "ঙ",
            "ch" to "চ",
            "Ch" to "ছ",
            "jh" to "ঝ",
            "NG" to "ঞ",
            "Th" to "ঠ",
            "Dh" to "ঢ",
            "th" to "থ",
            "dh" to "ধ",
            "ph" to "ফ",
            "bh" to "ভ",
            "sh" to "শ",
            "Sh" to "ষ",
            "Rh" to "ঢ়",

            // Single basic consonants
            "k" to "ক",
            "g" to "গ",
            "c" to "চ",
            "j" to "জ",
            "T" to "ট",
            "D" to "ড",
            "N" to "ণ",
            "t" to "ত",
            "d" to "দ",
            "n" to "ন",
            "p" to "প",
            "f" to "ফ",
            "b" to "ব",
            "v" to "ভ",
            "m" to "ম",
            "z" to "য",
            "Z" to "য",
            "r" to "র",
            "l" to "ল",
            "s" to "স",
            "S" to "শ",
            "h" to "হ",
            "R" to "ড়",
            "y" to "য়",
            "Y" to "য়",
            "w" to "ও",
            "W" to "ও"
        )

        // Common high-frequency Ridmik-style dictionary
        private val COMMON_DICTIONARY: Map<String, List<String>> = mapOf(
            "ami" to listOf("আমি"),
            "amar" to listOf("আমার"),
            "amader" to listOf("আমাদের"),
            "amra" to listOf("আমরা"),
            "amake" to listOf("আমাকে"),
            "tumi" to listOf("তুমি"),
            "tomar" to listOf("তোমার"),
            "tomader" to listOf("তোমাদের"),
            "tomra" to listOf("তোমরা"),
            "tomake" to listOf("তোমাকে"),
            "tui" to listOf("তুই"),
            "tor" to listOf("তোর"),
            "toke" to listOf("তোকে"),
            "apni" to listOf("আপনি"),
            "apnar" to listOf("আপনার"),
            "apnader" to listOf("আপনাদের"),
            "apnake" to listOf("আপনাকে"),
            "se" to listOf("সে"),
            "tar" to listOf("তার"),
            "take" to listOf("তাকে"),
            "tara" to listOf("তারা"),
            "tader" to listOf("তাদের"),
            "tini" to listOf("তিনি"),
            "tar" to listOf("তাঁর", "তার"),
            "ei" to listOf("এই"),
            "oi" to listOf("ওই", "ঐ"),
            "eta" to listOf("এটা"),
            "ota" to listOf("ওটা"),
            "egulo" to listOf("এগুলো"),
            "ogulo" to listOf("ওগুলো"),
            "jekono" to listOf("যেকোনো", "যেকোন"),
            "je" to listOf("যে"),
            "ke" to listOf("কে"),
            "ki" to listOf("কি", "কী"),
            "kire" to listOf("কিরে"),
            "kisu" to listOf("কিছু"),
            "koy" to listOf("কই"),
            "koi" to listOf("কই"),
            "koro" to listOf("করো", "কর"),
            "kintu" to listOf("কিন্তু"),
            "keno" to listOf("কেন"),
            "kothay" to listOf("কোথায়", "কোথায়"),
            "kokhon" to listOf("কখন"),
            "kivabe" to listOf("কিভাবে", "কীভাবে"),
            "kamne" to listOf("কেমনে"),
            "kemon" to listOf("কেমন"),
            "kichu" to listOf("কিছু"),
            "onek" to listOf("অনেক"),
            "ektu" to listOf("একটু"),
            "khub" to listOf("খুব"),
            "beshi" to listOf("বেশি", "বেশী"),
            "kom" to listOf("কম"),
            "bhalo" to listOf("ভালো", "ভাল", "ভালোই"),
            "valo" to listOf("ভালো", "ভাল", "ভালোই"),
            "shob" to listOf("সব"),
            "shobai" to listOf("সবাই"),
            "shobar" to listOf("সবার"),
            "shobkichu" to listOf("সবকিছু"),
            "shotti" to listOf("সত্যি", "সত্য"),
            "mitha" to listOf("মিথ্যা", "মিছা"),
            "thik" to listOf("ঠিক"),
            "bhul" to listOf("ভুল"),
            "hobe" to listOf("হবে"),
            "hoy" to listOf("হয়", "হয়"),
            "hoyeche" to listOf("হয়েছে", "হয়েছে"),
            "hoyeche" to listOf("হয়েছে"),
            "hocche" to listOf("হচ্ছে"),
            "holo" to listOf("হলো"),
            "hoi" to listOf("হই"),
            "holey" to listOf("হলেই"),
            "ache" to listOf("আছে"),
            "acho" to listOf("আছো", "আছ"),
            "achis" to listOf("আছিস"),
            "achen" to listOf("আছেন"),
            "achi" to listOf("আছি"),
            "chilo" to listOf("ছিল"),
            "chilam" to listOf("ছিলাম"),
            "chile" to listOf("ছিলে"),
            "chilen" to listOf("ছিলেন"),
            "na" to listOf("না"),
            "ha" to listOf("হ্যাঁ", "হা"),
            "tai" to listOf("তাই"),
            "ebong" to listOf("এবং"),
            "othoba" to listOf("অথবা"),
            "aron" to listOf("কারণ"),
            "karon" to listOf("কারণ"),
            "bondhu" to listOf("বন্ধু"),
            "bhai" to listOf("ভাই"),
            "bon" to listOf("বোন"),
            "baba" to listOf("বাবা"),
            "ma" to listOf("মা"),
            "chele" to listOf("ছেলে"),
            "meye" to listOf("মেয়ে", "মেয়ে"),
            "manush" to listOf("মানুষ"),
            "shomoy" to listOf("সময়", "সময়"),
            "ekhon" to listOf("এখন"),
            "tokhon" to listOf("তখন"),
            "kokhon" to listOf("কখন"),
            "ajke" to listOf("আজকে", "আজ"),
            "aj" to listOf("আজ"),
            "kal" to listOf("কাল"),
            "poroshu" to listOf("পরশু"),
            "din" to listOf("দিন"),
            "raat" to listOf("রাত"),
            "shokal" to listOf("সকাল"),
            "shondha" to listOf("সন্ধ্যা"),
            "bikel" to listOf("বিকেল"),
            "dupur" to listOf("দুপুর"),
            "kaj" to listOf("কাজ"),
            "kotha" to listOf("কথা"),
            "koro" to listOf("করো", "কর"),
            "kori" to listOf("করি"),
            "korbo" to listOf("করবো", "করব"),
            "korte" to listOf("করতে"),
            "likhi" to listOf("লিখি"),
            "nam" to listOf("নাম", "নম"),
            "korchi" to listOf("করছি"),
            "korechi" to listOf("করেছি"),
            "korben" to listOf("করবেন"),
            "koren" to listOf("করেন"),
            "koris" to listOf("করিস"),
            "korun" to listOf("করুন"),
            "kore" to listOf("করে"),
            "korle" to listOf("করলে"),
            "dekho" to listOf("দেখো", "দেখ"),
            "dekhi" to listOf("দেখি"),
            "dekhbo" to listOf("দেখব", "দেখবো"),
            "dekhechi" to listOf("দেখেছি"),
            "dekhun" to listOf("দেখুন"),
            "shuno" to listOf("শুনো", "শোন"),
            "shuni" to listOf("শুনি"),
            "shunbo" to listOf("শুনব", "শুনবো"),
            "shunechi" to listOf("শুনেছি"),
            "bujhi" to listOf("বুঝি"),
            "bujhechi" to listOf("বুঝেছি"),
            "bujhlen" to listOf("বুঝলেন"),
            "bujhte" to listOf("বুঝতে"),
            "bolbo" to listOf("বলব", "বলবো"),
            "bolo" to listOf("বলো", "বল"),
            "boli" to listOf("বলি"),
            "bolechi" to listOf("বলেছি"),
            "bolchen" to listOf("বলছেন"),
            "jabo" to listOf("যাবো", "যাব"),
            "jai" to listOf("যাই"),
            "jachhi" to listOf("যাচ্ছি"),
            "gechi" to listOf("গেছি"),
            "gelo" to listOf("গেল"),
            "asbo" to listOf("আসব", "আসবো"),
            "asi" to listOf("আসি"),
            "ashchi" to listOf("আসছি"),
            "eshechi" to listOf("এসেছি"),
            "ashun" to listOf("আসুন"),
            "parbo" to listOf("পারব", "পারবো"),
            "pari" to listOf("পারি"),
            "parbi" to listOf("পারবি"),
            "parben" to listOf("পারবেন"),
            "parbe" to listOf("পারবে"),
            "shuru" to listOf("শুরু"),
            "shesh" to listOf("শেষ"),
            "ghor" to listOf("ঘর"),
            "bari" to listOf("বাড়ি", "বারি"),
            "desh" to listOf("দেশ"),
            "deshe" to listOf("দেশে"),
            "desher" to listOf("দেশের"),
            "bangladesh" to listOf("বাংলাদেশ"),
            "bangladeshi" to listOf("বাংলাদেশী", "বাংলাদেশি"),
            "bangla" to listOf("বাংলা"),
            "banglay" to listOf("বাংলায়", "বাংলায়"),
            "bengali" to listOf("বাঙালি", "বাঙ্গালী"),
            "dhaka" to listOf("ঢাকা"),
            "dhakay" to listOf("ঢাকায়", "ঢাকাতে"),
            "kolkata" to listOf("কলকাতা"),
            "chittagong" to listOf("চট্টগ্রাম"),
            "khabar" to listOf("খাবার"),
            "khawa" to listOf("খাওয়া", "খাওয়া"),
            "khabo" to listOf("খাব", "খাবো"),
            "kheyechi" to listOf("খেয়েছি", "খেয়েছি"),
            "kheyeche" to listOf("খেয়েছে", "খেয়েছে"),
            "khao" to listOf("খাও"),
            "kheye" to listOf("খেয়ে", "খেয়ে"),
            "pani" to listOf("পানি"),
            "jol" to listOf("জল"),
            "cha" to listOf("চা"),
            "dudh" to listOf("দুধ"),
            "bhat" to listOf("ভাত"),
            "mach" to listOf("মাছ"),
            "mangsho" to listOf("মাংস"),
            "rooti" to listOf("রুটি"),
            "shobji" to listOf("সবজি"),
            "fol" to listOf("ফল"),
            "mishti" to listOf("মিষ্টি"),
            "boi" to listOf("বই"),
            "boiporte" to listOf("বইপড়তে"),
            "katha" to listOf("কথা"),
            "gan" to listOf("গান"),
            "gaan" to listOf("গান"),
            "shona" to listOf("শোনা"),
            "mon" to listOf("মন"),
            "mone" to listOf("মনে"),
            "moner" to listOf("মনের"),
            "chokh" to listOf("চোখ"),
            "haat" to listOf("হাত"),
            "pa" to listOf("পা"),
            "matha" to listOf("মাথা"),
            "mukh" to listOf("মুখ"),
            "naam" to listOf("নাম"),
            "prothom" to listOf("প্রথম"),
            "dwitiyo" to listOf("দ্বিতীয়", "দ্বিতীয়"),
            "tritiyo" to listOf("তৃতীয়", "তৃতীয়"),
            "shesh" to listOf("শেষ"),
            "dhonnobad" to listOf("ধন্যবাদ"),
            "donnobad" to listOf("ধন্যবাদ"),
            "thanks" to listOf("ধন্যবাদ"),
            "bhalobashi" to listOf("ভালোবাসি"),
            "valobashi" to listOf("ভালোবাসি"),
            "bhalobasha" to listOf("ভালোবাসা"),
            "valobasha" to listOf("ভালোবাসা"),
            "bhalobasho" to listOf("ভালোবাসো"),
            "valobasho" to listOf("ভালোবাসো"),
            "priyo" to listOf("প্রিয়", "প্রিয়"),
            "bondhu" to listOf("বন্ধু"),
            "bondhura" to listOf("বন্ধুরা"),
            "bondhutto" to listOf("বন্ধুত্ব"),
            "bhaiya" to listOf("ভাইয়া", "ভাইয়া"),
            "apu" to listOf("আপু"),
            "choto" to listOf("ছোট"),
            "boro" to listOf("বড়", "বড়"),
            "shundor" to listOf("সুন্দর"),
            "shundori" to listOf("সুন্দরী"),
            "kharap" to listOf("খারাপ"),
            "bhalo" to listOf("ভালো", "ভাল", "ভালোই"),
            "valo" to listOf("ভালো", "ভাল", "ভালোই"),
            "bhalobhabe" to listOf("ভালোভাবে"),
            "valobhabe" to listOf("ভালোভাবে"),
            "shuvo" to listOf("শুভ"),
            "subho" to listOf("শুভ"),
            "shuvo shokal" to listOf("শুভ সকাল"),
            "shuvo ratri" to listOf("শুভ রাত্রি"),
            "shuvo jonmodin" to listOf("শুভ জন্মদিন"),
            "noboborsho" to listOf("নববর্ষ"),
            "eid mubarak" to listOf("ঈদ মোবারক"),
            "inshallah" to listOf("ইনশাআল্লাহ"),
            "mashallah" to listOf("মাশাআল্লাহ"),
            "alhamdulillah" to listOf("আলহামদুলিল্লাহ"),
            "subhanallah" to listOf("সুবহানাল্লাহ"),
            "allah" to listOf("আল্লাহ"),
            "khuda hafez" to listOf("খোদা হাফেজ"),
            "shotti" to listOf("সত্যি", "সত্য"),
            "mithya" to listOf("মিথ্যা"),
            "thik" to listOf("ঠিক"),
            "tik" to listOf("ঠিক"),
            "thikache" to listOf("ঠিক আছে"),
            "tikache" to listOf("ঠিক আছে"),
            "bhul" to listOf("ভুল"),
            "shomossha" to listOf("সমস্যা"),
            "somossa" to listOf("সমস্যা"),
            "shomadhan" to listOf("সমাধান"),
            "sahajjo" to listOf("সাহায্য"),
            "help" to listOf("সাহায্য"),
            "somoy" to listOf("সময়", "সময়"),
            "shomoy" to listOf("সময়", "সময়"),
            "shomoymoto" to listOf("সময়মতো"),
            "ghonta" to listOf("ঘণ্টা", "ঘন্টা"),
            "minute" to listOf("মিনিট"),
            "second" to listOf("সেকেন্ড"),
            "bochor" to listOf("বছর"),
            "mash" to listOf("মাস"),
            "shoptaho" to listOf("সপ্তাহ"),
            "ajke" to listOf("আজকে", "আজ"),
            "kalke" to listOf("কালকে", "কাল"),
            "gotokal" to listOf("গতকাল"),
            "agami" to listOf("আগামী"),
            "porikkha" to listOf("পরীক্ষা"),
            "school" to listOf("স্কুল"),
            "college" to listOf("কলেজ"),
            "university" to listOf("বিশ্ববিদ্যালয়"),
            "porashona" to listOf("পড়াশোনা", "পড়াশোনা"),
            "chakri" to listOf("চাকরি", "চাকরী"),
            "office" to listOf("অফিস"),
            "basha" to listOf("বাসা"),
            "ghor" to listOf("ঘর"),
            "rasta" to listOf("রাস্তা"),
            "gari" to listOf("গাড়ি", "গাড়ি"),
            "taka" to listOf("টাকা"),
            "poisha" to listOf("পয়সা", "পয়সা"),
            "bazar" to listOf("বাজার"),
            "dokan" to listOf("দোকান"),
            "mobile" to listOf("মোবাইল"),
            "phone" to listOf("ফোন"),
            "number" to listOf("নম্বর", "নাম্বার"),
            "message" to listOf("মেসেজ"),
            "chithi" to listOf("চিঠি"),
            "khobor" to listOf("খবর"),
            "notun" to listOf("নতুন"),
            "puraton" to listOf("পুরাতন", "পুরানো"),
            "shobcheye" to listOf("সবচেয়ে", "সবচাইতে"),
            "sobcheye" to listOf("সবচেয়ে"),
            "shobai" to listOf("সবাই"),
            "sobai" to listOf("সবাই"),
            "shobar" to listOf("সবার"),
            "sobar" to listOf("সবার"),
            "shobkichu" to listOf("সবকিছু"),
            "sobkichu" to listOf("সবকিছু"),
            "onno" to listOf("অন্য"),
            "onnoder" to listOf("অন্যদের"),
            "nijer" to listOf("নিজের"),
            "nije" to listOf("নিজে"),
            "shathe" to listOf("সাথে"),
            "sathe" to listOf("সাথে"),
            "pashe" to listOf("পাশে"),
            "shamne" to listOf("সামনে"),
            "pechone" to listOf("পেছনে"),
            "upore" to listOf("উপরে"),
            "niche" to listOf("নিচে"),
            "bhitore" to listOf("ভিতরে"),
            "baire" to listOf("বাইরে"),
            "majhe" to listOf("মাঝে"),
            "majhemajhe" to listOf("মাঝেমধ্যে"),
            "shobshomoy" to listOf("সবসময়", "সবসময়"),
            "sobshomoy" to listOf("সবসময়"),
            "kokhono" to listOf("কখনো", "কখনও"),
            "abar" to listOf("আবার"),
            "abaro" to listOf("আবারও", "আবারো"),
            "onek" to listOf("অনেক"),
            "onnek" to listOf("অনেক"),
            "ektu" to listOf("একটু"),
            "kichu" to listOf("কিছু"),
            "kisu" to listOf("কিছু"),
            "khub" to listOf("খুব"),
            "beshi" to listOf("বেশি", "বেশী"),
            "kom" to listOf("কম"),
            "aro" to listOf("আরও", "আরো"),
            "shudhu" to listOf("শুধু"),
            "sudhu" to listOf("শুধু"),
            "matro" to listOf("মাত্র"),
            "karon" to listOf("কারণ"),
            "aron" to listOf("কারণ"),
            "tai" to listOf("তাই"),
            "ebong" to listOf("এবং"),
            "o" to listOf("ও"),
            "othoba" to listOf("অথবা"),
            "ba" to listOf("বা"),
            "jodi" to listOf("যদি"),
            "tobu" to listOf("তবু", "তবুও"),
            "kintu" to listOf("কিন্তু"),
            "jekono" to listOf("যেকোনো", "যেকোন"),
            "jemon" to listOf("যেমন"),
            "temon" to listOf("তেমন"),
            "kemon" to listOf("কেমন"),
            "kamon" to listOf("কেমন"),
            "keno" to listOf("কেন"),
            "kothay" to listOf("কোথায়", "কোথায়"),
            "kothai" to listOf("কোথায়", "কোথায়"),
            "kokhon" to listOf("কখন"),
            "kivabe" to listOf("কিভাবে", "কীভাবে"),
            "kibhabe" to listOf("কিভাবে", "কীভাবে"),
            "kamne" to listOf("কেমনে"),
            "ki" to listOf("কি", "কী"),
            "kee" to listOf("কী", "কি"),
            "ke" to listOf("কে"),
            "kara" to listOf("কারা"),
            "kake" to listOf("কাকে"),
            "kar" to listOf("কার"),
            "kader" to listOf("কাদের"),
            "kototuku" to listOf("কতটুকু"),
            "koto" to listOf("কত"),
            "koyta" to listOf("কয়টা", "কয়টা"),
            "shuru" to listOf("শুরু"),
            "shesh" to listOf("শেষ"),
            "cholche" to listOf("চলছে"),
            "cholo" to listOf("চলো"),
            "choli" to listOf("চলি"),
            "bolchi" to listOf("বলছি"),
            "bolte" to listOf("বলতে"),
            "shunte" to listOf("শুনতে"),
            "likhte" to listOf("লিখতে"),
            "likhchi" to listOf("লিখছি"),
            "likhbo" to listOf("লিখব", "লিখবো"),
            "likhechi" to listOf("লিখেছি"),
            "lekhok" to listOf("লেখক"),
            "lekha" to listOf("লেখা"),
            "dekhte" to listOf("দেখতে"),
            "dekhchi" to listOf("দেখছি"),
            "bujhte" to listOf("বুঝতে"),
            "bujhlam" to listOf("বুঝলাম"),
            "bujhchi" to listOf("বুঝছি"),
            "ghum" to listOf("ঘুম"),
            "ghumate" to listOf("ঘুমাতে"),
            "ghumiye" to listOf("ঘুমিয়ে", "ঘুমিয়ে"),
            "uthun" to listOf("উঠুন"),
            "utho" to listOf("উঠো"),
            "boshun" to listOf("বসুন"),
            "bosho" to listOf("বসো"),
            "thakun" to listOf("থাকুন"),
            "thako" to listOf("থাকো"),
            "thakbo" to listOf("থাকব", "থাকবো"),
            "thakchi" to listOf("থাকছি"),
            "hobe" to listOf("হবে"),
            "hoy" to listOf("হয়", "হয়"),
            "hocche" to listOf("হচ্ছে"),
            "hoyeche" to listOf("হয়েছে", "হয়েছে"),
            "holo" to listOf("হলো"),
            "ache" to listOf("আছে"),
            "achi" to listOf("আছি"),
            "acho" to listOf("আছো"),
            "achis" to listOf("আছিস"),
            "achen" to listOf("আছেন"),
            "chilo" to listOf("ছিল"),
            "chilam" to listOf("ছিলাম"),
            "chile" to listOf("ছিলে"),
            "chilen" to listOf("ছিলেন")
        )
    }

    /**
     * Converts a single word phonetically or returns candidate options.
     * Incorporates dictionary matching, prefix lookup, and rule-based fallback.
     */
    fun getCandidates(input: String, maxCandidates: Int = 4): List<String> {
        val cleanInput = input.trim()
        if (cleanInput.isEmpty()) return emptyList()

        val candidates = LinkedHashSet<String>()
        val lower = cleanInput.lowercase()

        // User explicit high-priority patterns:
        // k -> ki, koro, koy (কি, করো, কই)
        // ko -> koro, kothay, koy (করো, কোথায়, কই)
        // ki -> kire, kisu (কিরে, কিছু, কি)
        if (lower == "k") {
            candidates.add("কি")
            candidates.add("করো")
            candidates.add("কই")
            candidates.add("কোথায়")
            candidates.add("কিছু")
        } else if (lower == "ko") {
            candidates.add("করো")
            candidates.add("কোথায়")
            candidates.add("কই")
            candidates.add("কোনো")
            candidates.add("কখন")
        } else if (lower == "ki") {
            candidates.add("কি")
            candidates.add("কিরে")
            candidates.add("কিছু")
            candidates.add("কিন্তু")
            candidates.add("কী")
        }

        // 1. Exact match in Common Dictionary and Banglish Dictionary (1000+ words)
        COMMON_DICTIONARY[lower]?.let {
            candidates.addAll(it)
        }
        BanglishDictionary.BANGLISH_MAP[lower]?.let {
            candidates.add(it)
        }

        // 2. Rule-based phonetic conversion
        val parsed = parsePhonetic(cleanInput)
        if (parsed.isNotEmpty()) {
            candidates.add(parsed)
        }

        // 3. Prefix lookups in BanglishDictionary & Common Dictionary for fast predictive typing
        val banglishMatches = BanglishDictionary.getBengaliFromBanglish(lower, maxCandidates)
        candidates.addAll(banglishMatches)

        val prefixMatches = COMMON_DICTIONARY.filterKeys { it.startsWith(lower) && it != lower }
            .flatMap { it.value }
            .take(maxCandidates)
        candidates.addAll(prefixMatches)

        // 4. Alternative phonetic heuristics (v -> bh, w -> o, s -> sh, z -> j)
        if (lower.contains("v") || lower.contains("w")) {
            val alt = cleanInput.replace("v", "bh").replace("w", "o")
            COMMON_DICTIONARY[alt.lowercase()]?.let { candidates.addAll(it) }
            val altParsed = parsePhonetic(alt)
            if (altParsed.isNotEmpty() && altParsed != parsed) {
                candidates.add(altParsed)
            }
        }

        if (lower.contains("s") && !lower.contains("sh")) {
            val altSh = cleanInput.replace("s", "sh")
            COMMON_DICTIONARY[altSh.lowercase()]?.let { candidates.addAll(it) }
            val altShParsed = parsePhonetic(altSh)
            if (altShParsed.isNotEmpty()) candidates.add(altShParsed)
        }

        if (lower.contains("z") && !lower.contains("j")) {
            val altJ = cleanInput.replace("z", "j")
            COMMON_DICTIONARY[altJ.lowercase()]?.let { candidates.addAll(it) }
        }

        return candidates.take(maxCandidates).toList()
    }

    /**
     * Returns frequent word suggestions matching a phonetic prefix.
     */
    fun getFrequentWordSuggestions(prefix: String, maxCount: Int = 4): List<String> {
        val clean = prefix.trim().lowercase()
        if (clean.isEmpty()) return emptyList()

        val results = LinkedHashSet<String>()
        if (clean == "k") {
            results.add("কি")
            results.add("করো")
            results.add("কই")
        } else if (clean == "ko") {
            results.add("করো")
            results.add("কোথায়")
            results.add("কই")
        } else if (clean == "ki") {
            results.add("কিরে")
            results.add("কিছু")
            results.add("কি")
        }

        COMMON_DICTIONARY[clean]?.let { results.addAll(it) }
        BanglishDictionary.BANGLISH_MAP[clean]?.let { results.add(it) }

        val banglishMatches = BanglishDictionary.getBengaliFromBanglish(clean, maxCount)
        results.addAll(banglishMatches)

        val prefixMatches = COMMON_DICTIONARY.filterKeys { it.startsWith(clean) }
            .flatMap { it.value }
            .take(maxCount)
        results.addAll(prefixMatches)

        return results.take(maxCount).toList()
    }

    /**
     * Primary live conversion of word.
     */
    fun convertWord(input: String): String {
        val clean = input.trim()
        if (clean.isEmpty()) return ""
        val lower = clean.lowercase()
        val direct = COMMON_DICTIONARY[lower]?.firstOrNull() ?: BanglishDictionary.BANGLISH_MAP[lower]
        if (direct != null) return direct

        return parsePhonetic(clean)
    }

    /**
     * Parses phonetic Banglish string into Bengali Unicode.
     */
    fun parsePhonetic(input: String): String {
        if (input.isEmpty()) return ""

        val result = StringBuilder()
        var i = 0
        val len = input.length
        var prevCharWasConsonant = false

        while (i < len) {
            val c = input[i]

            // Check for Bengali Digits
            if (BENGALI_DIGITS.containsKey(c)) {
                result.append(BENGALI_DIGITS[c])
                prevCharWasConsonant = false
                i++
                continue
            }

            // Check for special symbols: . | , : ^ ` $
            if (c == '.' || c == '|') {
                result.append(DARI)
                prevCharWasConsonant = false
                i++
                continue
            }
            if (c == ':' || (c == 'H' && (i == len - 1 || input[i + 1] == ' '))) {
                result.append(BISARGA)
                prevCharWasConsonant = false
                i++
                continue
            }
            if (c == '^') {
                result.append(CHANDRABINDU)
                prevCharWasConsonant = false
                i++
                continue
            }
            if (c == '`' && i + 1 < len && input[i + 1] == '`') {
                result.append(HASANTA)
                prevCharWasConsonant = false
                i += 2
                continue
            }
            if ((c == 't' || c == 'T') && i + 1 < len && (input[i + 1] == '`' || input[i + 1] == '$')) {
                result.append(KHANDA_TA)
                prevCharWasConsonant = false
                i += 2
                continue
            }

            // Anusbar vs Nga check
            if (c == 'n' && i + 1 < len && input[i + 1] == 'g') {
                if (i + 2 < len && isVowelChar(input[i + 2])) {
                    // followed by vowel: ঙ্গ
                    result.append("ঙ্গ")
                    prevCharWasConsonant = true
                    i += 2
                    continue
                } else if (i + 2 == len || !isAlphabet(input[i + 2])) {
                    // end of word: ং
                    result.append(ANUSBAR)
                    prevCharWasConsonant = false
                    i += 2
                    continue
                }
            }

            // Reph (রেফ): 'r' followed by consonant (and next is not 'r')
            // In Unicode Bengali, Reph is formed by: র (09B0) + ্ (09CD) + Consonant
            if (c == 'r' && i + 1 < len && !isVowelChar(input[i + 1]) && input[i + 1] != 'r' && isAlphabet(input[i + 1])) {
                val matchConsonant = matchConsonant(input, i + 1)
                if (matchConsonant != null) {
                    result.append("র").append(HASANTA).append(matchConsonant.bengali)
                    i += 1 + matchConsonant.length
                    prevCharWasConsonant = true
                    continue
                }
            }

            // Fola: y/z (য-ফলা), r (র-ফলা), w (ব-ফলা) after consonant
            if (prevCharWasConsonant) {
                if ((c == 'y' || c == 'z') && (i + 1 == len || isVowelChar(input[i + 1]))) {
                    result.append(HASANTA).append("য")
                    prevCharWasConsonant = true
                    i++
                    continue
                }
                if (c == 'r' && (i + 1 == len || isVowelChar(input[i + 1]))) {
                    result.append(HASANTA).append("র")
                    prevCharWasConsonant = true
                    i++
                    continue
                }
                if (c == 'w' && (i + 1 == len || isVowelChar(input[i + 1]))) {
                    result.append(HASANTA).append("ব")
                    prevCharWasConsonant = true
                    i++
                    continue
                }
            }

            // Check Vowels
            val vowelMatch = matchVowel(input, i)
            if (vowelMatch != null) {
                if (prevCharWasConsonant) {
                    val kar = VOWEL_KARS[vowelMatch.pattern]
                    if (kar != null) {
                        result.append(kar)
                    } else {
                        result.append(vowelMatch.bengali)
                    }
                } else {
                    result.append(vowelMatch.bengali)
                }
                prevCharWasConsonant = false
                i += vowelMatch.length
                continue
            }

            // Check Consonants
            val consonantMatch = matchConsonant(input, i)
            if (consonantMatch != null) {
                if (prevCharWasConsonant) {
                    result.append(HASANTA)
                }
                result.append(consonantMatch.bengali)
                prevCharWasConsonant = true
                i += consonantMatch.length
                continue
            }

            // Fallback for non-alphabetic characters
            result.append(c)
            prevCharWasConsonant = false
            i++
        }

        return result.toString()
    }

    private data class MatchResult(val pattern: String, val bengali: String, val length: Int)

    private fun matchVowel(input: String, startIndex: Int): MatchResult? {
        val sub = input.substring(startIndex)
        val candidates = listOf(
            3 to listOf("rri", "rRI"),
            2 to listOf("aa", "ee", "oo", "oi", "OI", "ou", "OU", "ow", "ri"),
            1 to listOf("a", "A", "i", "I", "u", "U", "e", "E", "o", "O")
        )

        for ((len, list) in candidates) {
            if (sub.length >= len) {
                for (pattern in list) {
                    if (sub.startsWith(pattern)) {
                        val bengali = INDEPENDENT_VOWELS[pattern] ?: continue
                        return MatchResult(pattern, bengali, len)
                    }
                }
            }
        }
        return null
    }

    private fun matchConsonant(input: String, startIndex: Int): MatchResult? {
        val sub = input.substring(startIndex)
        val sortedKeys = CONSONANTS.keys.sortedByDescending { it.length }

        for (key in sortedKeys) {
            if (sub.startsWith(key)) {
                val bengali = CONSONANTS[key] ?: continue
                return MatchResult(key, bengali, key.length)
            }
        }
        return null
    }

    private fun isVowelChar(c: Char): Boolean {
        return c in "aeiouAEIOU"
    }

    private fun isAlphabet(c: Char): Boolean {
        return (c in 'a'..'z') || (c in 'A'..'Z')
    }
}
