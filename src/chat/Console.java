package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void write(String message) {
        System.out.println(message);
    }

    public static String readString() {
        while (true) {
            try {
                return br.readLine();
            }
            catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }

    public static String readString(String promptText) {
        System.out.print(promptText);
        return readString();
    }

    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }

    public static int readInt(String promptText) {
        System.out.print(promptText);
        return readInt();
    }
}
