class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    viewModel.registerUser(username, password)
                    observeRegistrationResult()
                } else {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeRegistrationResult() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
            }
        }
    }
}