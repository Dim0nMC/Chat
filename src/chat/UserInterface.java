package chat;

public interface UserInterface {
    void write(String message);
    String readString();
    String readString(String promptText);
    int readInt();
    int readInt(String promptText);
}
