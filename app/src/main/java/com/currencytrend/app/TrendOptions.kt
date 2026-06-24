package com.trendpulse.app

data class OptionItem(
    val code: String,
    val label: String
) {
    override fun toString(): String = label
}

object TrendOptions {

    val periods = listOf(
        OptionItem("1", "Today"),
        OptionItem("7", "7 days"),
        OptionItem("14", "14 days"),
        OptionItem("30", "30 days")
    )

    val countries = listOf(
        OptionItem("us", "United States"),
        OptionItem("in", "India"),
        OptionItem("gb", "United Kingdom"),
        OptionItem("ca", "Canada"),
        OptionItem("au", "Australia"),
        OptionItem("de", "Germany"),
        OptionItem("fr", "France"),
        OptionItem("jp", "Japan"),
        OptionItem("it", "Italy"),
        OptionItem("es", "Spain"),
        OptionItem("nl", "Netherlands"),
        OptionItem("se", "Sweden"),
        OptionItem("no", "Norway"),
        OptionItem("dk", "Denmark"),
        OptionItem("ie", "Ireland"),
        OptionItem("br", "Brazil"),
        OptionItem("mx", "Mexico"),
        OptionItem("kr", "South Korea"),
        OptionItem("ch", "Switzerland"),
        OptionItem("sg", "Singapore"),
        OptionItem("nz", "New Zealand"),
        OptionItem("za", "South Africa"),
        OptionItem("ae", "United Arab Emirates"),
        OptionItem("sa", "Saudi Arabia"),
        OptionItem("cn", "China"),
        OptionItem("ph", "Philippines"),
        OptionItem("id", "Indonesia"),
        OptionItem("th", "Thailand"),
        OptionItem("my", "Malaysia"),
        OptionItem("pk", "Pakistan"),
        OptionItem("bd", "Bangladesh"),
        OptionItem("lk", "Sri Lanka"),
        OptionItem("np", "Nepal")
    )

    val languages = listOf(
        OptionItem("en_us", "English (US)"),
        OptionItem("en_gb", "English (UK)"),
        OptionItem("en_in", "English (India)"),
        OptionItem("en_au", "English (Australia)"),
        OptionItem("en_nz", "English (New Zealand)"),
        OptionItem("en_ca", "English (Canada)"),
        OptionItem("fr_fr", "French"),
        OptionItem("fr_ca", "French (Canada)"),
        OptionItem("de_de", "German"),
        OptionItem("es_es", "Spanish"),
        OptionItem("es_mx", "Spanish (Mexico)"),
        OptionItem("ja_jp", "Japanese"),
        OptionItem("it_it", "Italian"),
        OptionItem("pt_br", "Portuguese (Brazil)"),
        OptionItem("ko_kr", "Korean"),
        OptionItem("nl_nl", "Dutch"),
        OptionItem("sv_se", "Swedish"),
        OptionItem("no_no", "Norwegian"),
        OptionItem("da_dk", "Danish"),
        OptionItem("zh_cn", "Chinese (Mandarin)"),
        OptionItem("ar_sa", "Arabic (Saudi Arabia)"),
        OptionItem("ar_ae", "Arabic (UAE)"),
        OptionItem("ms_my", "Malay"),
        OptionItem("th_th", "Thai"),
        OptionItem("id_id", "Indonesian"),
        OptionItem("tl_ph", "Filipino"),
        OptionItem("ur_pk", "Urdu"),
        OptionItem("si_lk", "Sinhala"),
        OptionItem("ne_np", "Nepali"),
        OptionItem("hi_in", "Hindi"),
        OptionItem("ta_in", "Tamil"),
        OptionItem("te_in", "Telugu"),
        OptionItem("ml_in", "Malayalam"),
        OptionItem("kn_in", "Kannada"),
        OptionItem("mr_in", "Marathi"),
        OptionItem("bn_in", "Bengali")
    )

    val musicGenres = listOf(
        OptionItem("14", "Pop"),
        OptionItem("18", "Hip-Hop/Rap"),
        OptionItem("21", "Rock"),
        OptionItem("11", "Jazz"),
        OptionItem("5", "Classical")
    )

    val southAsianMusicGenres = listOf(
        OptionItem("love", "Love"),
        OptionItem("inspirational", "Inspirational"),
        OptionItem("fast_beat", "Fast Beat"),
        OptionItem("devotional", "Devotional")
    )

    val movieGenres = listOf(
        OptionItem("4401", "Action & Adventure"),
        OptionItem("4404", "Comedy"),
        OptionItem("4406", "Drama"),
        OptionItem("4413", "Sci-Fi & Fantasy"),
        OptionItem("4416", "Thriller")
    )

    val marketIndexes = listOf(
        OptionItem("ALL_MAJOR", "All Major (DOW + NASDAQ + S&P 500)"),
        OptionItem("^GSPC", "S&P 500"),
        OptionItem("^DJI", "Dow Jones"),
        OptionItem("^IXIC", "NASDAQ Composite")
    )

    private val marketIndexesByCountry: Map<String, List<OptionItem>> = mapOf(
        "us" to listOf(
            OptionItem("ALL_MAJOR", "All Major (DOW + NASDAQ + S&P 500)"),
            OptionItem("^GSPC", "S&P 500"),
            OptionItem("^DJI", "Dow Jones"),
            OptionItem("^IXIC", "NASDAQ Composite")
        ),
        "in" to listOf(
            OptionItem("^NSEI", "NIFTY 50"),
            OptionItem("^BSESN", "SENSEX"),
            OptionItem("^NSEBANK", "NIFTY BANK"),
            OptionItem("^CNX500", "NIFTY 500")
        ),
        "gb" to listOf(
            OptionItem("^FTSE", "FTSE 100"),
            OptionItem("^FTMC", "FTSE 250"),
            OptionItem("^FTAS", "FTSE All-Share"),
            OptionItem("^FTCN", "FTSE 350")
        ),
        "ca" to listOf(
            OptionItem("^GSPTSE", "S&P/TSX Composite"),
            OptionItem("^TSX60", "S&P/TSX 60"),
            OptionItem("^CDNX", "TSX Venture"),
            OptionItem("^TSX", "TSX Composite")
        ),
        "au" to listOf(
            OptionItem("^AXJO", "S&P/ASX 200"),
            OptionItem("^AORD", "All Ordinaries"),
            OptionItem("^AXKO", "S&P/ASX 300"),
            OptionItem("^XAO", "ASX All Ordinaries")
        ),
        "de" to listOf(
            OptionItem("^GDAXI", "DAX"),
            OptionItem("^MDAXI", "MDAX"),
            OptionItem("^SDAXI", "SDAX"),
            OptionItem("^TECDAX", "TecDAX")
        ),
        "fr" to listOf(
            OptionItem("^FCHI", "CAC 40"),
            OptionItem("^SBF120", "SBF 120"),
            OptionItem("^PX1", "CAC Next 20"),
            OptionItem("^FR40", "France 40")
        ),
        "jp" to listOf(
            OptionItem("^N225", "Nikkei 225"),
            OptionItem("^TOPX", "TOPIX"),
            OptionItem("^JP225", "JP225")
        ),
        "kr" to listOf(
            OptionItem("^KS11", "KOSPI"),
            OptionItem("^KQ11", "KOSDAQ"),
            OptionItem("^KS200", "KOSPI 200"),
            OptionItem("^KPI200", "KPI 200")
        ),
        "br" to listOf(
            OptionItem("^BVSP", "Ibovespa"),
            OptionItem("^IBRX", "IBrX 100"),
            OptionItem("^IFIX", "IFIX")
        ),
        "mx" to listOf(
            OptionItem("^MXX", "IPC Mexico"),
            OptionItem("^MEXBOL", "Mexbol"),
            OptionItem("^IPSA", "IPC Mexico Plus"),
            OptionItem("^NIKM", "Mexico 40")
        ),
        "sg" to listOf(
            OptionItem("^STI", "Straits Times Index"),
            OptionItem("^FTSTI", "FTSE Straits Times"),
            OptionItem("^SGX", "SGX Composite"),
            OptionItem("^FSSTI", "FTSE STI")
        ),
        "cn" to listOf(
            OptionItem("000001.SS", "SSE Composite"),
            OptionItem("399001.SZ", "SZSE Component"),
            OptionItem("^HSI", "Hang Seng"),
            OptionItem("^CSI300", "CSI 300")
        ),
        "hk" to listOf(
            OptionItem("^HSI", "Hang Seng"),
            OptionItem("^HSCE", "Hang Seng China Enterprises"),
            OptionItem("^HSCCI", "Hang Seng Composite"),
            OptionItem("^HSF", "Hang Seng Future")
        ),
        "inr" to listOf(
            OptionItem("^NSEI", "NIFTY 50"),
            OptionItem("^BSESN", "SENSEX"),
            OptionItem("^NSEBANK", "NIFTY BANK"),
            OptionItem("^CNX100", "NIFTY 100")
        ),
        "sa" to listOf(
            OptionItem("^TASI", "Tadawul All Share"),
            OptionItem("^SASEIDX", "Saudi Arabia 30"),
            OptionItem("^TASI.TA", "TASI 20"),
            OptionItem("^S34", "Saudi 10")
        )
    )

    val podcastGenres = listOf(
        OptionItem("1301", "Arts"),
        OptionItem("1321", "Business"),
        OptionItem("1303", "Comedy"),
        OptionItem("1304", "Education"),
        OptionItem("1311", "News"),
        OptionItem("1318", "Technology")
    )

    private val weatherStateCityMap: Map<String, Map<String, List<OptionItem>>> = mapOf(
        "us" to mapOf(
            "California" to listOf(OptionItem("Los Angeles", "Los Angeles"), OptionItem("San Francisco", "San Francisco"), OptionItem("San Diego", "San Diego"), OptionItem("San Jose", "San Jose")),
            "Texas" to listOf(OptionItem("Houston", "Houston"), OptionItem("Dallas", "Dallas"), OptionItem("Austin", "Austin"), OptionItem("San Antonio", "San Antonio")),
            "New York" to listOf(OptionItem("New York", "New York City"), OptionItem("Buffalo", "Buffalo"), OptionItem("Albany", "Albany"), OptionItem("Rochester", "Rochester")),
            "Florida" to listOf(OptionItem("Miami", "Miami"), OptionItem("Orlando", "Orlando"), OptionItem("Tampa", "Tampa"), OptionItem("Jacksonville", "Jacksonville"))
        ),
        "in" to mapOf(
            "Tamil Nadu" to listOf(OptionItem("Chennai", "Chennai"), OptionItem("Coimbatore", "Coimbatore"), OptionItem("Madurai", "Madurai"), OptionItem("Tiruchirappalli", "Tiruchirappalli")),
            "Maharashtra" to listOf(OptionItem("Mumbai", "Mumbai"), OptionItem("Pune", "Pune"), OptionItem("Nagpur", "Nagpur"), OptionItem("Nashik", "Nashik")),
            "Karnataka" to listOf(OptionItem("Bengaluru", "Bengaluru"), OptionItem("Mysuru", "Mysuru"), OptionItem("Mangaluru", "Mangaluru"), OptionItem("Hubballi", "Hubballi")),
            "Delhi" to listOf(OptionItem("New Delhi", "New Delhi"), OptionItem("Dwarka", "Dwarka"), OptionItem("Rohini", "Rohini"), OptionItem("Saket", "Saket")),
            "Kerala" to listOf(OptionItem("Thiruvananthapuram", "Thiruvananthapuram"), OptionItem("Kochi", "Kochi"), OptionItem("Kozhikode", "Kozhikode"), OptionItem("Thrissur", "Thrissur"))
        ),
        "gb" to mapOf(
            "England" to listOf(OptionItem("London", "London"), OptionItem("Manchester", "Manchester"), OptionItem("Birmingham", "Birmingham"), OptionItem("Liverpool", "Liverpool")),
            "Scotland" to listOf(OptionItem("Glasgow", "Glasgow"), OptionItem("Edinburgh", "Edinburgh"), OptionItem("Aberdeen", "Aberdeen"), OptionItem("Dundee", "Dundee")),
            "Wales" to listOf(OptionItem("Cardiff", "Cardiff"), OptionItem("Swansea", "Swansea"), OptionItem("Newport", "Newport"), OptionItem("Wrexham", "Wrexham"))
        ),
        "ca" to mapOf(
            "Ontario" to listOf(OptionItem("Toronto", "Toronto"), OptionItem("Ottawa", "Ottawa"), OptionItem("Hamilton", "Hamilton"), OptionItem("London", "London")),
            "British Columbia" to listOf(OptionItem("Vancouver", "Vancouver"), OptionItem("Victoria", "Victoria"), OptionItem("Kelowna", "Kelowna"), OptionItem("Surrey", "Surrey")),
            "Quebec" to listOf(OptionItem("Montreal", "Montreal"), OptionItem("Quebec City", "Quebec City"), OptionItem("Laval", "Laval"), OptionItem("Gatineau", "Gatineau"))
        ),
        "ch" to mapOf(
            "Zurich" to listOf(OptionItem("Zurich", "Zurich"), OptionItem("Winterthur", "Winterthur"), OptionItem("Uster", "Uster"), OptionItem("Dübendorf", "Dübendorf")),
            "Geneva" to listOf(OptionItem("Geneva", "Geneva"), OptionItem("Carouge", "Carouge"), OptionItem("Lancy", "Lancy"), OptionItem("Meyrin", "Meyrin")),
            "Vaud" to listOf(OptionItem("Lausanne", "Lausanne"), OptionItem("Yverdon-les-Bains", "Yverdon-les-Bains"), OptionItem("Renens", "Renens"), OptionItem("Montreux", "Montreux"))
        ),
        "au" to mapOf(
            "New South Wales" to listOf(OptionItem("Sydney", "Sydney"), OptionItem("Newcastle", "Newcastle"), OptionItem("Wollongong", "Wollongong"), OptionItem("Parramatta", "Parramatta")),
            "Victoria" to listOf(OptionItem("Melbourne", "Melbourne"), OptionItem("Geelong", "Geelong"), OptionItem("Ballarat", "Ballarat"), OptionItem("Bendigo", "Bendigo")),
            "Queensland" to listOf(OptionItem("Brisbane", "Brisbane"), OptionItem("Gold Coast", "Gold Coast"), OptionItem("Cairns", "Cairns"), OptionItem("Townsville", "Townsville"))
        ),
        "de" to mapOf(
            "Bavaria" to listOf(OptionItem("Munich", "Munich"), OptionItem("Nuremberg", "Nuremberg"), OptionItem("Augsburg", "Augsburg"), OptionItem("Regensburg", "Regensburg")),
            "Berlin" to listOf(OptionItem("Berlin", "Berlin"), OptionItem("Potsdam", "Potsdam"), OptionItem("Spandau", "Spandau"), OptionItem("Kreuzberg", "Kreuzberg")),
            "Hesse" to listOf(OptionItem("Frankfurt", "Frankfurt"), OptionItem("Wiesbaden", "Wiesbaden"), OptionItem("Darmstadt", "Darmstadt"), OptionItem("Kassel", "Kassel"))
        ),
        "fr" to mapOf(
            "Ile-de-France" to listOf(OptionItem("Paris", "Paris"), OptionItem("Versailles", "Versailles"), OptionItem("Boulogne-Billancourt", "Boulogne-Billancourt"), OptionItem("Nanterre", "Nanterre")),
            "Occitanie" to listOf(OptionItem("Toulouse", "Toulouse"), OptionItem("Montpellier", "Montpellier"), OptionItem("Nimes", "Nimes"), OptionItem("Perpignan", "Perpignan"))
        ),
        "jp" to mapOf(
            "Kanto" to listOf(OptionItem("Tokyo", "Tokyo"), OptionItem("Yokohama", "Yokohama"), OptionItem("Saitama", "Saitama"), OptionItem("Chiba", "Chiba")),
            "Kansai" to listOf(OptionItem("Osaka", "Osaka"), OptionItem("Kyoto", "Kyoto"), OptionItem("Kobe", "Kobe"), OptionItem("Nara", "Nara"))
        ),
        "br" to mapOf(
            "Sao Paulo" to listOf(OptionItem("Sao Paulo", "Sao Paulo"), OptionItem("Campinas", "Campinas"), OptionItem("Santos", "Santos"), OptionItem("Sao Bernardo do Campo", "Sao Bernardo do Campo")),
            "Rio de Janeiro" to listOf(OptionItem("Rio de Janeiro", "Rio de Janeiro"), OptionItem("Niteroi", "Niteroi"), OptionItem("Petropolis", "Petropolis"), OptionItem("Duque de Caxias", "Duque de Caxias"))
        ),
        "mx" to mapOf(
            "Mexico City" to listOf(OptionItem("Mexico City", "Mexico City"), OptionItem("Coyoacan", "Coyoacan"), OptionItem("Iztapalapa", "Iztapalapa"), OptionItem("Naucalpan", "Naucalpan")),
            "Jalisco" to listOf(OptionItem("Guadalajara", "Guadalajara"), OptionItem("Zapopan", "Zapopan"), OptionItem("Tlaquepaque", "Tlaquepaque"), OptionItem("Tonalá", "Tonala"))
        ),
        "kr" to mapOf(
            "Seoul" to listOf(OptionItem("Seoul", "Seoul"), OptionItem("Gangnam", "Gangnam"), OptionItem("Jongno", "Jongno"), OptionItem("Mapo", "Mapo")),
            "Busan" to listOf(OptionItem("Busan", "Busan"), OptionItem("Haeundae", "Haeundae"), OptionItem("Suyeong", "Suyeong"), OptionItem("Sasang", "Sasang"))
        ),
        "sg" to mapOf(
            "Central" to listOf(OptionItem("Orchard", "Orchard"), OptionItem("Marina Bay", "Marina Bay"), OptionItem("Bugis", "Bugis"), OptionItem("River Valley", "River Valley")),
            "North-East" to listOf(OptionItem("Hougang", "Hougang"), OptionItem("Serangoon", "Serangoon"), OptionItem("Punggol", "Punggol"), OptionItem("Sengkang", "Sengkang"))
        ),
        "nz" to mapOf(
            "Auckland" to listOf(OptionItem("Auckland", "Auckland"), OptionItem("Manukau", "Manukau"), OptionItem("North Shore", "North Shore"), OptionItem("Waitakere", "Waitakere")),
            "Wellington" to listOf(OptionItem("Wellington", "Wellington"), OptionItem("Lower Hutt", "Lower Hutt"), OptionItem("Porirua", "Porirua"), OptionItem("Upper Hutt", "Upper Hutt"))
        ),
        "za" to mapOf(
            "Gauteng" to listOf(OptionItem("Johannesburg", "Johannesburg"), OptionItem("Pretoria", "Pretoria"), OptionItem("Soweto", "Soweto"), OptionItem("Sandton", "Sandton")),
            "Western Cape" to listOf(OptionItem("Cape Town", "Cape Town"), OptionItem("Stellenbosch", "Stellenbosch"), OptionItem("Paarl", "Paarl"), OptionItem("Somerset West", "Somerset West"))
        ),
        "ae" to mapOf(
            "Dubai" to listOf(OptionItem("Dubai", "Dubai"), OptionItem("Deira", "Deira"), OptionItem("Jumeirah", "Jumeirah"), OptionItem("Al Barsha", "Al Barsha")),
            "Abu Dhabi" to listOf(OptionItem("Abu Dhabi", "Abu Dhabi"), OptionItem("Al Ain", "Al Ain"), OptionItem("Mussafah", "Mussafah"), OptionItem("Khalifa City", "Khalifa City"))
        ),
        "sa" to mapOf(
            "Riyadh Province" to listOf(OptionItem("Riyadh", "Riyadh"), OptionItem("Diriyah", "Diriyah"), OptionItem("Al Kharj", "Al Kharj"), OptionItem("Al Majma'ah", "Al Majma'ah")),
            "Makkah Province" to listOf(OptionItem("Jeddah", "Jeddah"), OptionItem("Mecca", "Mecca"), OptionItem("Taif", "Taif"), OptionItem("Rabigh", "Rabigh"))
        ),
        "cn" to mapOf(
            "Beijing" to listOf(OptionItem("Beijing", "Beijing"), OptionItem("Haidian", "Haidian"), OptionItem("Chaoyang", "Chaoyang"), OptionItem("Shunyi", "Shunyi")),
            "Shanghai" to listOf(OptionItem("Shanghai", "Shanghai"), OptionItem("Pudong", "Pudong"), OptionItem("Minhang", "Minhang"), OptionItem("Jing'an", "Jing'an"))
        ),
        "ph" to mapOf(
            "Metro Manila" to listOf(OptionItem("Manila", "Manila"), OptionItem("Quezon City", "Quezon City"), OptionItem("Makati", "Makati"), OptionItem("Pasig", "Pasig")),
            "Cebu" to listOf(OptionItem("Cebu City", "Cebu City"), OptionItem("Mandaue", "Mandaue"), OptionItem("Lapu-Lapu", "Lapu-Lapu"), OptionItem("Talisay", "Talisay"))
        ),
        "id" to mapOf(
            "Jakarta" to listOf(OptionItem("Jakarta", "Jakarta"), OptionItem("South Jakarta", "South Jakarta"), OptionItem("Central Jakarta", "Central Jakarta"), OptionItem("West Jakarta", "West Jakarta")),
            "West Java" to listOf(OptionItem("Bandung", "Bandung"), OptionItem("Bekasi", "Bekasi"), OptionItem("Bogor", "Bogor"), OptionItem("Depok", "Depok"))
        ),
        "th" to mapOf(
            "Bangkok" to listOf(OptionItem("Bangkok", "Bangkok"), OptionItem("Nonthaburi", "Nonthaburi"), OptionItem("Bang Kapi", "Bang Kapi"), OptionItem("Pathum Wan", "Pathum Wan")),
            "Chiang Mai" to listOf(OptionItem("Chiang Mai", "Chiang Mai"), OptionItem("Mueang Chiang Mai", "Mueang Chiang Mai"), OptionItem("Saraphi", "Saraphi"), OptionItem("San Sai", "San Sai"))
        ),
        "my" to mapOf(
            "Selangor" to listOf(OptionItem("Shah Alam", "Shah Alam"), OptionItem("Petaling Jaya", "Petaling Jaya"), OptionItem("Subang Jaya", "Subang Jaya"), OptionItem("Klang", "Klang")),
            "Kuala Lumpur" to listOf(OptionItem("Kuala Lumpur", "Kuala Lumpur"), OptionItem("Cheras", "Cheras"), OptionItem("Bangsar", "Bangsar"), OptionItem("Setapak", "Setapak"))
        ),
        "pk" to mapOf(
            "Punjab" to listOf(OptionItem("Lahore", "Lahore"), OptionItem("Faisalabad", "Faisalabad"), OptionItem("Rawalpindi", "Rawalpindi"), OptionItem("Multan", "Multan")),
            "Sindh" to listOf(OptionItem("Karachi", "Karachi"), OptionItem("Hyderabad", "Hyderabad"), OptionItem("Sukkur", "Sukkur"), OptionItem("Larkana", "Larkana"))
        ),
        "bd" to mapOf(
            "Dhaka Division" to listOf(OptionItem("Dhaka", "Dhaka"), OptionItem("Narayanganj", "Narayanganj"), OptionItem("Gazipur", "Gazipur"), OptionItem("Tongi", "Tongi")),
            "Chattogram Division" to listOf(OptionItem("Chattogram", "Chattogram"), OptionItem("Cox's Bazar", "Cox's Bazar"), OptionItem("Comilla", "Comilla"), OptionItem("Feni", "Feni"))
        ),
        "lk" to mapOf(
            "Western Province" to listOf(OptionItem("Colombo", "Colombo"), OptionItem("Dehiwala-Mount Lavinia", "Dehiwala-Mount Lavinia"), OptionItem("Moratuwa", "Moratuwa"), OptionItem("Sri Jayawardenepura Kotte", "Sri Jayawardenepura Kotte")),
            "Central Province" to listOf(OptionItem("Kandy", "Kandy"), OptionItem("Nuwara Eliya", "Nuwara Eliya"), OptionItem("Matale", "Matale"), OptionItem("Gampola", "Gampola"))
        ),
        "np" to mapOf(
            "Bagmati" to listOf(OptionItem("Kathmandu", "Kathmandu"), OptionItem("Lalitpur", "Lalitpur"), OptionItem("Bhaktapur", "Bhaktapur"), OptionItem("Hetauda", "Hetauda")),
            "Gandaki" to listOf(OptionItem("Pokhara", "Pokhara"), OptionItem("Baglung", "Baglung"), OptionItem("Waling", "Waling"), OptionItem("Lekhnath", "Lekhnath"))
        )
    )

    private val languageByCode = languages.associateBy { it.code }
    private val countryLanguageCodes: Map<String, List<String>> = mapOf(
        "us" to listOf("en_us", "es_es"),
        "in" to listOf("en_in", "hi_in", "ta_in", "te_in", "ml_in", "kn_in", "mr_in", "bn_in"),
        "gb" to listOf("en_gb"),
        "ca" to listOf("en_ca", "fr_ca"),
        "au" to listOf("en_au"),
        "de" to listOf("de_de"),
        "fr" to listOf("fr_fr"),
        "jp" to listOf("ja_jp"),
        "it" to listOf("it_it"),
        "es" to listOf("es_es"),
        "nl" to listOf("nl_nl"),
        "se" to listOf("sv_se"),
        "no" to listOf("no_no"),
        "dk" to listOf("da_dk"),
        "ie" to listOf("en_gb"),
        "br" to listOf("pt_br"),
        "mx" to listOf("es_mx"),
        "kr" to listOf("ko_kr"),
        "ch" to listOf("de_de", "fr_fr", "it_it"),
        "sg" to listOf("en_gb", "zh_cn", "ms_my", "ta_in"),
        "nz" to listOf("en_nz"),
        "za" to listOf("en_gb"),
        "ae" to listOf("ar_ae", "en_gb"),
        "sa" to listOf("ar_sa", "en_gb"),
        "cn" to listOf("zh_cn"),
        "ph" to listOf("tl_ph", "en_us"),
        "id" to listOf("id_id"),
        "th" to listOf("th_th"),
        "my" to listOf("ms_my", "en_gb", "zh_cn", "ta_in"),
        "pk" to listOf("ur_pk", "en_in"),
        "bd" to listOf("bn_in", "en_in"),
        "lk" to listOf("si_lk", "ta_in", "en_in"),
        "np" to listOf("ne_np", "hi_in", "en_in")
    )

    fun languagesForCountry(countryCode: String): List<OptionItem> {
        val preferred = countryLanguageCodes[countryCode.lowercase()] ?: listOf("en_us")

        val ordered = preferred.mapNotNull { code -> languageByCode[code] }
        return if (ordered.isNotEmpty()) ordered else listOfNotNull(languageByCode["en_us"], languageByCode["en_gb"])
    }

    fun statesForCountry(countryCode: String): List<OptionItem> {
        val states = weatherStateCityMap[countryCode.lowercase()]?.keys.orEmpty().toList().sorted()
        return if (states.isEmpty()) listOf(OptionItem("default", "Default Region"))
        else states.map { OptionItem(it, it) }
    }

    fun citiesForState(countryCode: String, state: String): List<OptionItem> {
        val map = weatherStateCityMap[countryCode.lowercase()] ?: return listOf(OptionItem("", "Default City"))
        return map[state].orEmpty().ifEmpty { listOf(OptionItem("", "Default City")) }
    }

    fun musicGenresForCountry(countryCode: String): List<OptionItem> {
        return when (countryCode.lowercase()) {
            "in", "pk", "bd", "lk", "np", "bt", "mv" -> southAsianMusicGenres
            else -> musicGenres
        }
    }

    fun marketIndexesForCountry(countryCode: String): List<OptionItem> {
        return marketIndexesByCountry[countryCode.lowercase()] ?: marketIndexes
    }

    fun marketSymbolsForCountry(countryCode: String): List<String> {
        return marketIndexesForCountry(countryCode)
            .map { it.code }
            .distinct()
    }
}
