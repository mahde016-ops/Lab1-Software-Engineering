
public class User {

    private final String username;
    private final String password;

    public User(String username, String password) {
        validateEmail(username);
        validatePassword(password);

        this.username = username;
        this.password = password;
    }

    private static void validateEmail(String username) {
        if (username == null) {
            throw new IllegalArgumentException(
                    "Please enter a valid Email as username"
            );
        }

        if (username.length() > 50) {
            throw new IllegalArgumentException(
                    "Username is too long, try something shorter"
            );
        }

        String emailPattern =
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}$";

        if (!username.matches(emailPattern)) {
            throw new IllegalArgumentException(
                    "Please enter a valid Email as username"
            );
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Your password is too short, add more characters"
            );
        }

        if (password.length() > 12) {
            throw new IllegalArgumentException(
                    "Your password is too long, try a shorter one"
            );
        }

        String passwordPattern =
                "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*()])" +
                        "[A-Za-z0-9!@#$%^&*()]{8,12}$";

        if (!password.matches(passwordPattern)) {
            throw new IllegalArgumentException(
                    "Please enter a valid password"
            );
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
