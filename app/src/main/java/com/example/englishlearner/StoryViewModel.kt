class StoryViewModel(application: Application) : AndroidViewModel(application) {
    private val storyDao = AppDatabase.getDatabase(application).storyDao()
    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    fun loadStories(userId: Int) {
        viewModelScope.launch {
            storyDao.getStoriesByUser(userId).observeForever { stories ->
                _stories.postValue(stories)
            }
        }
    }

    fun saveStory(story: Story) {
        viewModelScope.launch {
            if (story.id == 0) {
                storyDao.insertStory(story)
            } else {
                storyDao.updateStory(story)
            }
        }
    }

    fun deleteStory(story: Story) {
        viewModelScope.launch {
            storyDao.deleteStory(story)
        }
    }
}