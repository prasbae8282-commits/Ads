package com.example.data.model

data class AdCampaign(
    val id: String,
    val title: String,
    val sponsor: String,
    val category: String,
    val description: String,
    val tagline: String,
    val promoBadge: String,
    val callToAction: String,
    val brandColorHex: Long,
    val rating: Float,
    val reviewsCount: String,
    val rewardAmount: Long = 500L,
    val durationSeconds: Int = 15
)

object SampleAdCampaigns {
    val all = listOf(
        AdCampaign(
            id = "ad_shopee",
            title = "Shopee Mega Sale 2026",
            sponsor = "Shopee Indonesia",
            category = "Belanja Online",
            description = "Nikmati Gratis Ongkir Rp 0 se-Indonesia, Flash Sale Rp 1 setiap hari, dan voucher diskon belanja hingga 90%!",
            tagline = "Beli Semua di Shopee!",
            promoBadge = "GRATIS ONGKIR RP 0",
            callToAction = "Install & Belanja Sekarang",
            brandColorHex = 0xFFEE4D2D,
            rating = 4.8f,
            reviewsCount = "12 jt+ Ulasan"
        ),
        AdCampaign(
            id = "ad_gofood",
            title = "GoFood Super Hemat",
            sponsor = "GoJek Indonesia",
            category = "Makanan & Minuman",
            description = "Lapar melanda? Pesan makanan favoritmu sekarang dengan diskon kilat 60% dan voucher ongkos kirim hemat!",
            tagline = "Pasti Ada Jalan, Pasti Kenyang!",
            promoBadge = "DISKON 60%",
            callToAction = "Pesan Makan Sekarang",
            brandColorHex = 0xFF00AA13,
            rating = 4.7f,
            reviewsCount = "8 jt+ Ulasan"
        ),
        AdCampaign(
            id = "ad_traveloka",
            title = "Traveloka Staycation & Liburan",
            sponsor = "Traveloka",
            category = "Travel & Hotel",
            description = "Waktunya healing! Dapatkan diskon tiket pesawat, kereta api, dan voucher hotel bintang 5 hingga Rp 1.500.000.",
            tagline = "Liburan Impian Jadi Nyata!",
            promoBadge = "CASHBACK RP 1.5JT",
            callToAction = "Cek Promo Liburan",
            brandColorHex = 0xFF0284C7,
            rating = 4.9f,
            reviewsCount = "5 jt+ Ulasan"
        ),
        AdCampaign(
            id = "ad_tokopedia",
            title = "Tokopedia Waktu Indonesia Belanja",
            sponsor = "Tokopedia",
            category = "E-Commerce",
            description = "Promo WIB hadir setiap akhir bulan. Kejar diskon spesial, cashback kilat, dan bebas ongkir tanpa minimum belanja!",
            tagline = "Selalu Ada Selalu Bisa!",
            promoBadge = "CASHBACK HINGGA 90%",
            callToAction = "Buka Tokopedia",
            brandColorHex = 0xFF03AC0E,
            rating = 4.8f,
            reviewsCount = "10 jt+ Ulasan"
        ),
        AdCampaign(
            id = "ad_mlbb",
            title = "Mobile Legends: 5v5 MOBA",
            sponsor = "Moonton Games",
            category = "Game Mobile",
            description = "Bergabunglah dalam pertarungan legendaris 5 lawan 5! Pilih hero favoritmu dan raih rank Mythic sekarang juga.",
            tagline = "Kompak Main, Raih Kemenangan!",
            promoBadge = "HERO & SKIN GRATIS",
            callToAction = "Mainkan Sekarang",
            brandColorHex = 0xFFF59E0B,
            rating = 4.6f,
            reviewsCount = "15 jt+ Ulasan"
        ),
        AdCampaign(
            id = "ad_dana",
            title = "DANA Dompet Digital Indonesia",
            sponsor = "DANA Indonesia",
            category = "Finansial & Pembayaran",
            description = "Bebas transfer antar bank gratis 10x per bulan, bayar tagihan listrik, pulsa, dan QRIS instan tanpa repot!",
            tagline = "Bukan Dompet Biasa!",
            promoBadge = "GRATIS TRANSFER BANK",
            callToAction = "Buka Akun DANA",
            brandColorHex = 0xFF118EEA,
            rating = 4.7f,
            reviewsCount = "9 jt+ Ulasan"
        )
    )

    fun getRandom(currentId: String? = null): AdCampaign {
        val pool = if (currentId != null) all.filter { it.id != currentId } else all
        return pool.randomOrNull() ?: all.first()
    }
}
