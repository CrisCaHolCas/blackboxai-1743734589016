class InteractiveModeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInteractiveModeBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var currentStory: Story? = null
    private val englishWords = mutableListOf<String>()
    private var currentWordIndex = 0
    private val userAnswers = mutableListOf<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInteractiveModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAudioPermission()
        setupSpeechRecognizer()
        currentStory = intent.getParcelableExtra("story")
        currentStory?.let {
            englishWords.addAll(Gson().fromJson(it.wordsToLearn, Array<String>::class.java).toList())
            prepareStoryWithBlanks()
        }

        binding.btnVoiceInput.setOnClickListener {
            startVoiceInput()
        }

        binding.btnTextInput.setOnClickListener {
            showTextInputDialog()
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.get(0)?.let { processVoiceInput(it) }
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(this@InteractiveModeActivity, 
                    "Error: ${getErrorText(error)}", Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun prepareStoryWithBlanks() {
        val storyText = currentStory?.content ?: return
        val words = storyText.split(" ")
        val displayText = StringBuilder()

        for (word in words) {
            if (englishWords.contains(word)) {
                displayText.append("____ ")
            } else {
                displayText.append("$word ")
            }
        }

        binding.tvStoryWithBlanks.text = displayText.toString()
    }

    private fun startVoiceInput() {
        try {
            speechRecognizer.startListening(speechIntent)
            binding.btnVoiceInput.isEnabled = false
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Voice recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processVoiceInput(input: String) {
        binding.btnVoiceInput.isEnabled = true
        val currentWord = englishWords.getOrNull(currentWordIndex) ?: return
        
        val isCorrect = input.equals(currentWord, ignoreCase = true)
        userAnswers.add(isCorrect)
        
        if (isCorrect) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
            currentWordIndex++
            if (currentWordIndex >= englishWords.size) {
                showResults()
            }
        } else {
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTextInputDialog() {
        val currentWord = englishWords.getOrNull(currentWordIndex) ?: return
        
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter the English word for:")
            .setMessage(currentWord)
            .setView(input)
            .setPositiveButton("Submit") { _, _ ->
                val answer = input.text.toString()
                val isCorrect = answer.equals(currentWord, ignoreCase = true)
                userAnswers.add(isCorrect)
                
                if (isCorrect) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                    currentWordIndex++
                    if (currentWordIndex >= englishWords.size) {
                        showResults()
                    }
                } else {
                    Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResults() {
        val correct = userAnswers.count { it }
        val total = userAnswers.size
        val percentage = (correct.toFloat() / total) * 100
        
        AlertDialog.Builder(this)
            .setTitle("Exercise Results")
            .setMessage("You got $correct out of $total correct ($percentage%)")
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO = 101
    }
}