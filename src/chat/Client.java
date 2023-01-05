package chat;

import java.io.IOException;
import java.net.Socket;


public class Client {
    private Connection connection;
    volatile private boolean connected = false;
    private final UserInterface userInterface;

    public Client(UserInterface userInterface) {
        this.userInterface = userInterface;
    }


    public class ClientThread extends Thread {

        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try {
                connection = new Connection(new Socket(address, port));
                clientPrepare();
                clientProcess();
            } catch (IOException | ClassNotFoundException e) {
                connectionStatusChanged(false);
            }
        }

        protected void clientPrepare() throws IOException, ClassNotFoundException {
            while (true) {
                Message msg = connection.receive();
                if (msg.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }

                if (msg.getType() == MessageType.NAME_ACCEPTED) {
                    connectionStatusChanged(true);
                    return;
                }

                if (msg.getType() != MessageType.NAME_REQUEST &&
                        msg.getType() != MessageType.NAME_ACCEPTED) {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientProcess() throws IOException, ClassNotFoundException {
            while (true) {
                if (Thread.currentThread().isInterrupted()) break;
                Message msg = connection.receive();
                if (msg.getType() == null) throw new IOException("Unexpected MessageType");
                switch (msg.getType()) {
                    case TEXT: {
                        processIncomingMessage(msg.getData());
                        break;
                    }
                    case USER_ADDED: {
                        informAboutAddingNewUser(msg.getData());
                        break;
                    }
                    case USER_REMOVED: {
                        informAboutDeletingUser(msg.getData());
                        break;
                    }
                    default: {
                        throw new IOException("Unexpected MessageType");
                    }
                }
            }
        }

        protected void processIncomingMessage(String message) {
            userInterface.write(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            userInterface.write("участник с именем " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingUser(String userName) {
            userInterface.write("участник с именем " + userName + " покинул чат.");
        }

        protected void connectionStatusChanged(boolean connected) {
            Client.this.connected = connected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }
    }

    public void run() {
        ClientThread t = new ClientThread();;
        t.setDaemon(true);
        t.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                userInterface.write("Ошибка подключения");
                return;
            }
        }
        if (connected) userInterface.write("Соединение установлено.\nДля выхода наберите команду 'exit'.");
            else userInterface.write("Произошла ошибка во время работы клиента.");
        while (connected) {
            String text = userInterface.readString();
            if (text.equals("exit")) break;
            sendTextMessage(text);
        }
    }

    protected String getServerAddress() {
        return userInterface.readString("Введите ip-адрес сервера: ");
    }

    protected int getServerPort() {
        return userInterface.readInt("Введите порт сервера: ");
    }

    protected String getUserName() {
        return userInterface.readString("Введите имя участника: ");
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            connected = false;
            userInterface.write("Произошла ошибка при отправке сообщения");
        }
    }

    public static void main(String[] args) {
        UserInterface userInterface = null;
        if (args.length == 0 || args[0].equalsIgnoreCase("con")) {
            userInterface = new Console();
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
//            userInterface = new GUI();
        }
        if (userInterface == null) userInterface = new Console();
        new Client(userInterface).run();

    }
}
