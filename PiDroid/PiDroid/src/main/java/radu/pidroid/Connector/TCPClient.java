/*
  Code taken and adapted from:
  http://myandroidsolutions.blogspot.co.uk/2012/07/android-tcp-connection-tutorial.html
*/


package radu.pidroid.Connector;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class TCPClient {

    public static String SERVER_IP;
    public static String SERVER_PORT;

    private String serverMessage;
    private boolean clientRunning = false;

    private PrintWriter    outputStream;
    private BufferedReader inputStream;

    private List<MessageReceivedListener> messageReceivedListeners;


    public TCPClient(String serverIP, String serverPort) {
        SERVER_IP = serverIP;
        SERVER_PORT = serverPort;
        this.messageReceivedListeners = new ArrayList<MessageReceivedListener>();
    } // constructor


    //**********************************************************************************************
    //                                  PUBLIC METHODS
    //**********************************************************************************************


    public void sendMessage(String message){
        if (outputStream != null && !outputStream.checkError()) {
            outputStream.println(message);
            outputStream.flush();
        } // if
    } // sendMessage


    public void stopClient() {
        clientRunning = false;
    } // stopClient


    public void startClient() throws TCPClientException {

        clientRunning = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            Log.d("TCPClient", "startClient(): connecting...");

            // Create a socket to make the connection with the server
            Socket socket = new Socket(serverAddress, Integer.parseInt(SERVER_PORT));

            try {
                // Create the output stream for writing bytes to this socket
                outputStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                // Create the input stream for receiving bytes from this socket
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d("TCPClient", "startClient(): listening for messages...");

                // In this while the client listens for the messages sent by the server
                while (clientRunning) {
                    serverMessage = inputStream.readLine();

                    // Notify any listeners that a message was received
                    if (serverMessage != null)
                        for (MessageReceivedListener listener : this.messageReceivedListeners)
                            listener.OnMessageReceived(serverMessage);

                    serverMessage = null;
                } // while
            } catch (Exception exception) {
                Log.e("TCPClient", "startClient(): (try-catch-finally) Error: ", exception);
            } finally {
                // The socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                Log.d("TCPClient", "startClient(): connection closed");
            } // try-catch-finally

        } catch (Exception exception) {
            Log.e("TCPClient", "startClient(): (try-catch) Error: ", exception);
            throw new TCPClientException(exception.getMessage());
        } // try - catch
    } // startClient


    public class TCPClientException extends Exception {

        public TCPClientException(String message) {
            super(message);
        } // constructor

    } // TCPClientException


    //**********************************************************************************************
    //                                  TCP CLIENT LISTENER(S)
    //**********************************************************************************************


    // Declare the interface. The method messageReceived(String message) must be
    // implemented inputStream the Main class at on asynckTask doInBackground
    public interface MessageReceivedListener {
        void OnMessageReceived(String message);
    } // onMessageReceived

    public void addMessageReceivedListener(MessageReceivedListener listener) {
        this.messageReceivedListeners.add(listener);
    } // setOnMessageReceived

} // TCPClient