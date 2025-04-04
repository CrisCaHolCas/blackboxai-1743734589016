class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: StoryViewModel
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // In a real app, get userId from shared preferences or intent
        currentUserId = 1 // Temporary for testing
        viewModel = ViewModelProvider(this)[StoryViewModel::class.java]

        setupRecyclerView()
        loadStories()

        binding.fabAddStory.setOnClickListener {
            startActivity(Intent(this, StoryEditorActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val adapter = StoryAdapter { story ->
            openStoryOptions(story)
        }
        binding.rvStories.adapter = adapter
        binding.rvStories.layoutManager = LinearLayoutManager(this)

        viewModel.stories.observe(this) { stories ->
            adapter.submitList(stories)
        }
    }

    private fun loadStories() {
        viewModel.loadStories(currentUserId)
    }

    private fun openStoryOptions(story: Story) {
        val options = arrayOf("Read Mode", "Interactive Mode", "Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle(story.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startReadingMode(story)
                    1 -> startInteractiveMode(story)
                    2 -> editStory(story)
                    3 -> deleteStory(story)
                }
            }
            .show()
    }

    private fun startReadingMode(story: Story) {
        val intent = Intent(this, ReadingModeActivity::class.java).apply {
            putExtra("story", story)
        }
        startActivity(intent)
    }

    private fun startInteractiveMode(story: Story) {
        val intent = Intent(this, InteractiveModeActivity::class.java).apply {
            putExtra("story", story)
        }
        startActivity(intent)
    }

    private fun editStory(story: Story) {
        val intent = Intent(this, StoryEditorActivity::class.java).apply {
            putExtra("story", story)
        }
        startActivity(intent)
    }

    private fun deleteStory(story: Story) {
        viewModel.deleteStory(story)
    }
}