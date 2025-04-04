class StoryEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryEditorBinding
    private lateinit var viewModel: StoryViewModel
    private var currentStory: Story? = null
    private val selectedWords = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[StoryViewModel::class.java]
        currentStory = intent.getParcelableExtra("story")

        setupUI()
        setupWordSelection()
    }

    private fun setupUI() {
        currentStory?.let { story ->
            binding.etTitle.setText(story.title)
            binding.etStoryContent.setText(story.content)
            selectedWords.addAll(Gson().fromJson(story.wordsToLearn, Array<String>::class.java).toList())
        }

        binding.btnSave.setOnClickListener {
            saveStory()
        }
    }

    private fun setupWordSelection() {
        binding.etStoryContent.setOnClickListener {
            val selection = binding.etStoryContent.selectionStart
            val text = binding.etStoryContent.text.toString()
            val word = getWordAtPosition(text, selection)
            
            word?.let {
                if (selectedWords.contains(it)) {
                    selectedWords.remove(it)
                    Toast.makeText(this, "Word unselected: $it", Toast.LENGTH_SHORT).show()
                } else {
                    selectedWords.add(it)
                    Toast.makeText(this, "Word selected for learning: $it", Toast.LENGTH_SHORT).show()
                }
                highlightSelectedWords()
            }
        }
    }

    private fun getWordAtPosition(text: String, position: Int): String? {
        if (position < 0 || position >= text.length) return null
        
        val start = text.substring(0, position).takeLastWhile { it.isLetter() }
        val end = text.substring(position).takeWhile { it.isLetter() }
        
        return if (start.isNotEmpty() || end.isNotEmpty()) {
            start + end
        } else {
            null
        }
    }

    private fun highlightSelectedWords() {
        val text = binding.etStoryContent.text.toString()
        val spannable = SpannableString(text)
        
        selectedWords.forEach { word ->
            var index = text.indexOf(word)
            while (index >= 0) {
                spannable.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    index,
                    index + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = text.indexOf(word, index + word.length)
            }
        }
        
        binding.etStoryContent.setText(spannable)
    }

    private fun saveStory() {
        val title = binding.etTitle.text.toString()
        val content = binding.etStoryContent.text.toString()
        val wordsJson = Gson().toJson(selectedWords)

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val story = currentStory?.copy(
            title = title,
            content = content,
            wordsToLearn = wordsJson
        ) ?: Story(
            userId = getCurrentUserId(), // Implement this method
            title = title,
            content = content,
            wordsToLearn = wordsJson
        )

        viewModel.saveStory(story)
        finish()
    }

    private fun getCurrentUserId(): Int {
        // In a real app, get from shared preferences or database
        return 1 // Temporary for testing
    }
}