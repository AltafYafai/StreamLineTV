package com.streamline.tv

data class MediaItem(
    val id: String,
    val type: String, // "movie" or "series"
    val title: String,
    val description: String,
    val metadata: String,
    val videoUrl: String,
    val posterUrl: String,
    val bannerUrl: String,
    val episodes: List<EpisodeItem> = emptyList()
)

data class EpisodeItem(
    val id: String,
    val title: String,
    val season: Int,
    val episode: Int,
    val thumbnail: String?
)

object SampleData {
    val movies = listOf(
        MediaItem(
            id = "1",
            type = "movie",
            title = "Big Buck Bunny",
            description = "A giant rabbit with a heart of gold is harassed by three small squirrels.",
            metadata = "Animation • 2008 • 9m 56s",
            videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c5/Big_Buck_Bunny_Poster_2008.png",
            bannerUrl = "https://peach.blender.org/wp-content/uploads/title_an_v06.jpg"
        )
    )
}
