
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class UsersApp {

    public static void main(String[] args) {

        ArrayList<User> users = new ArrayList<>();

        try {
            File file = new File("users.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] parts = line.trim().split("\\s+");

                if (parts.length != 2) {
                    System.out.println(
                            line + " Please enter a valid Email as username"
                    );
                    continue;
                }

                try {
                    User user = new User(parts[0], parts[1]);
                    users.add(user);

                } catch (IllegalArgumentException e) {
                    System.out.println(
                            line + " " + e.getMessage()
                    );
                }
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            return;
        }

        Collections.sort(users,
                (u1, u2) -> u1.getUsername().compareTo(u2.getUsername())
        );

        for (User user : users) {
            System.out.println(
                    user.getUsername() + " " + user.getPassword()
            );
        }
    }
}
