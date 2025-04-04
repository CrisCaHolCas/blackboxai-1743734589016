class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    fun registerUser(username: String, password: String) {
        viewModelScope.launch {
            if (userDao.checkUsernameExists(username).value == null) {
                val hashedPassword = hashPassword(password)
                val user = User(username = username, password = hashedPassword)
                userDao.insertUser(user)
                _loginResult.postValue(true)
            } else {
                _loginResult.postValue(false)
            }
        }
    }

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            val hashedPassword = hashPassword(password)
            val user = userDao.getUser(username, hashedPassword).value
            _loginResult.postValue(user != null)
        }
    }

    private fun hashPassword(password: String): String {
        // In production, use proper hashing like BCrypt
        return password.hashCode().toString()
    }
}