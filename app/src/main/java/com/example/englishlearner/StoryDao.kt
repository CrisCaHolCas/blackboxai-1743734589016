@Dao
interface StoryDao {
    @Insert
    suspend fun insertStory(story: Story)

    @Update
    suspend fun updateStory(story: Story)

    @Delete
    suspend fun deleteStory(story: Story)

    @Query("SELECT * FROM stories WHERE userId = :userId ORDER BY createdAt DESC")
    fun getStoriesByUser(userId: Int): LiveData<List<Story>>

    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    fun getStoryById(storyId: Int): LiveData<Story?>
}