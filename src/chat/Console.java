package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console implements UserInterface {
    private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void write(String message) {
        System.out.println(message);
    }

    @Override
    public String readString() {
        while (true) {
            try {
                return br.readLine();
            }
            catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }

    @Override
    public String readString(String promptText) {
        System.out.print(promptText);
        return readString();
    }

    @Override
    public int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }

    @Override
    public int readInt(String promptText) {
        System.out.print(promptText);
        return readInt();
    }
}
