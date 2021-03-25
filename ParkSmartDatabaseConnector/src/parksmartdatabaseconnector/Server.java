/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parksmartdatabaseconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jorda
 */
public class Server {

    public static final int port = 1619;

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private final PrintWriter os;
        private final BufferedReader is;
        private final Map<String, ClientHandler> clients;
        private String clientId;

        public ClientHandler(Socket socket, Map<String, ClientHandler> clients) throws IOException {
            this.socket = socket;
            this.clients = clients;
            this.os = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.is = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
        }

        @Override
        public void run() {
            try {
                //client sends ID
                clientId = is.readLine();
                clients.put(clientId, this);

                for (String line = is.readLine(); line != null; line = is.readLine()) {
                    int separatorIndex = line.indexOf(':');
                    if (separatorIndex <= 0) {
                        continue;
                    }
                    String toClient = line.substring(0, separatorIndex);
                    String message = line.substring(+1);
                    ClientHandler client = clients.get(toClient);
                    if (client != null) {
                        client.sendMessage(clientId, message);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client " + clientId + " terminated");
                clients.remove(clientId);
                try {
                    socket.close();

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        public void sendMessage(String from, String message) {
            try {
                synchronized (os) {
                    os.println(from + ":" + message);
                    os.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        final Map<String, ClientHandler> clients = new ConcurrentHashMap<String, ClientHandler>();
        
        ServerSocket ss = new ServerSocket(port);
        for(Socket socket = ss.accept(); socket != null; socket = ss.accept()){
        Runnable handler = new ClientHandler(socket, clients);
        new Thread(handler).start();
    }
    }
}
