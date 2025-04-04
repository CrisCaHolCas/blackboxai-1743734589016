@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val wordsToLearn: String,
    val createdAt: Long = System.currentTimeMillis()
)