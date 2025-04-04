class ReadingModeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityReadingModeBinding
    private lateinit var tts: TextToSpeech
    private var currentStory: Story? = null
    private val englishWords = mutableListOf<String>()
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)
        currentStory = intent.getParcelableExtra("story")
        currentStory?.let {
            englishWords.addAll(Gson().fromJson(it.wordsToLearn, Array<String>::class.java).toList())
            displayStoryWithHighlights()
        }

        binding.btnRead.setOnClickListener {
            if (isSpeaking) {
                stopSpeaking()
            } else {
                readStory()
            }
        }
    }

    private fun displayStoryWithHighlights() {
        val text = currentStory?.content ?: return
        val spannable = SpannableString(text)

        englishWords.forEach { word ->
            var index = text.indexOf(word)
            while (index >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.BLUE),
                    index,
                    index + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    index,
                    index + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = text.indexOf(word, index + word.length)
            }
        }

        binding.tvStoryContent.text = spannable
    }

    private fun readStory() {
        val text = currentStory?.content ?: return
        val words = text.split(" ")
        isSpeaking = true
        binding.btnRead.text = "Stop"

        for (i in words.indices) {
            val word = words[i]
            if (englishWords.contains(word)) {
                // Speak English word
                tts.language = Locale.US
                tts.speak(word, TextToSpeech.QUEUE_ADD, null, "word_$i")
            } else {
                // Speak Spanish text
                tts.language = Locale("es", "ES")
                tts.speak(word, TextToSpeech.QUEUE_ADD, null, "word_$i")
            }
        }

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String?) {
                if (utteranceId == "word_${words.size - 1}") {
                    runOnUiThread {
                        isSpeaking = false
                        binding.btnRead.text = "Read"
                    }
                }
            }
            override fun onError(utteranceId: String?) {}
            override fun onStart(utteranceId: String?) {}
        })
    }

    private fun stopSpeaking() {
        tts.stop()
        isSpeaking = false
        binding.btnRead.text = "Read"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
        } else {
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        tts.shutdown()
        super.onDestroy()
    }
}