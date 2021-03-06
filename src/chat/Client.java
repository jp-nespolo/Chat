/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.GregorianCalendar;
import java.util.Locale;

/**
 *
 * @author jp
 */
public class Client
{
    private Ichat remote_ref;
    private view.ChatWindow view_ref;
    private ClientServices client_services_ref;
    private String user_name;
    private String id;
    private ArrayList<String> clients;
    
    private Thread th;
    private ClientServices th_run;
    
    
    public static void main(String args[])
    {
        new view.Login().setVisible(true);
    }
   
    
    public Client(view.ChatWindow view_ref, String user_name) throws Exception
    {
        this.view_ref = view_ref;
        this.user_name = user_name;
    }
   
    
    public void init()
    {
        try
        {
            // Connect to server
            remote_ref = (Ichat) Naming.lookup("rmi://localhost:1099/joe-chat");
            id = remote_ref.connect(user_name);
            
            // Make interface to the server communicate with the client
            th_run = new ClientServices(this, id);
            th = new Thread(th_run);
            th.start();
            
            // Inform server about its interface
            Thread.sleep(100);
            remote_ref.activateClient(id);

            // Get list of all clients
            clients = remote_ref.listOfClients();
            view_ref.writeAllUsers(clients);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    
    public void updateListOfClients() throws RemoteException
    {
        clients = remote_ref.listOfClients();
        view_ref.writeAllUsers(clients);
    }
    
    
    public void destroy() throws RemoteException
    {
        this.remote_ref.disconnect(user_name);
        th_run.die();
    }
    
    
    public void sendMessage(String receiver, String textSubmit) throws RemoteException 
    {
 
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        String date = dtf.format(now);
        
        remote_ref.managerMessage(this.id, receiver, textSubmit,date);
    }
    
    
    public void recvMsgFromServer(String senderId, String msgContent)
    {
        view_ref.updateChatContent(msgContent);
    }
    
    
    public void getHistoryOfChats(String other) throws RemoteException
    {
        ArrayList<Chatter> history = remote_ref.getHistory(other, this.user_name);
        view_ref.writeChat(history);

    }
}
