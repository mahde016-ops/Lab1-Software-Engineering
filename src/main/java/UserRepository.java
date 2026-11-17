
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class UserRepository {

    private final ArrayList<User> users = new ArrayList<>();

    public UserRepository() throws FileNotFoundException {
        loadUsers();
    }

    private void loadUsers() throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File("users.txt"))) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.trim().split("\\s+");

                if (parts.length != 2) {
                    continue;
                }

                try {
                    users.add(new User(parts[0], parts[1]));
                } catch (IllegalArgumentException e) {
                    // Skip invalid users from the input file.
                }
            }
        }
    }

    public boolean authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean emailExists(String email) {
        for (User user : users) {
            if (user.getUsername().equals(email)) {
                return true;
            }
        }
        return false;
    }

}
