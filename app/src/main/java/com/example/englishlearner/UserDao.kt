@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun getUser(username: String, password: String): LiveData<User?>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun checkUsernameExists(username: String): LiveData<User?>
}