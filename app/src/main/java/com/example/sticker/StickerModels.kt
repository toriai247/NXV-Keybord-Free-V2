package com.example.sticker

data class StickerItem(
    val id: String,
    val packId: String,
    val assetPath: String,
    val mimeType: String,
    val displayName: String
)

data class StickerPack(
    val id: String,
    val name: String,
    val category: String,
    val previewAssetPath: String,
    val previewEmoji: String,
    val stickers: List<StickerItem>
)
