package chat2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat2 {

    private static ArrayList<user> clientes = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        System.out.println("The chat server is running... ");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (Exception e) {
        }

    }
 
    private static class Handler implements Runnable{
        private String nombre;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        
        public Handler(Socket socket){
            this.socket= socket;
        }
        
       
        
        public void run(){
            try{
                in= new Scanner(socket.getInputStream());
                out= new PrintWriter(socket.getOutputStream(), true);
                
                while (true) {                    
                    out.println("SUBMITNAME");
                    nombre= in.nextLine();
                    if(nombre==null || nombre.toLowerCase().startsWith("quit") || nombre.length()<4){
                        continue;
                    }
                    synchronized(clientes){
                        if(!clientes.contains(nombre)){     
                            out.println("NAMEACCEPTED " + nombre);
                            for(user usuarios : clientes){
                                usuarios.getEscritor().println("MESSAGE " + nombre + " has joined");
                            }
                            clientes.add(new user(nombre, out, ""));
                            break;
                        }
                    }
                }
                
              
                
                
                while (true) {                    
                    String input= in.nextLine();
                    String bloqueados= "";
                    for(user usuarios : clientes){
                        if(usuarios.getNombre().equals(nombre))
                            bloqueados= usuarios.getBloqueados();
                    }
                    if(input.startsWith("/")){
                    
                        if(input.toLowerCase().startsWith("/quit")){
                        return;
                   
                    
                    }else if(input.toLowerCase().startsWith("/bloquear")){
                        String bloqueado= input.substring(10);
                        if(bloqueado.equals(nombre)){
                            for(user usuarios : clientes){
                                if(usuarios.getNombre().equals(nombre))
                                    usuarios.getEscritor().println("No puedes bloquear....");
                            }
                            continue;
                        }
                        
                        for(user usuarios : clientes){
                            if(usuarios.getNombre().equals(nombre)){
                                if(usuarios.getBloqueados().isEmpty()){
                                    usuarios.setBloqueados(bloqueado + "↨");
                                }else{
                                    usuarios.setBloqueados(usuarios.getBloqueados() + bloqueado + "↨");
                                }
                            }
                        }
                   
                    
                    }else if(input.toLowerCase().startsWith("/desbloquear")){
                        String desbloquear= input.substring(13);
                        for(user usuarios : clientes){
                            if(usuarios.getNombre().equals(nombre)){
                                if(usuarios.getBloqueados().contains(desbloquear)){
                                    usuarios.setBloqueados(usuarios.getBloqueados().replace(desbloquear + "↨", ""));
                                }
                            }
                        }
                    }else{
                       
                        try{
                        int separador= input.substring(1).indexOf(" ");
                        String address =  input.substring(1, separador + 1);
                        String mess =  input.substring(1).substring(separador + 1);
                        
                        if(address.equals(nombre)){
                            for(user usuarios : clientes){
                                if(usuarios.getNombre().equals(nombre))
                                    usuarios.getEscritor().println("No enviado...");
                            }
                            continue;
                        }
                        
                       
                        for(user usuarios : clientes){
                            if(usuarios.getNombre().equals(address)){
                                System.out.println("(" + usuarios.getNombre() + ") - (" + address + ")");
                                usuarios.getEscritor().println("MESSAGE " + nombre + " a " + address + ": " + mess);
                            }
                            if(usuarios.getNombre().equals(nombre)){
                                System.out.println("(" + usuarios.getNombre() + ") - (" + address + ")");
                                usuarios.getEscritor().println("MESSAGE " + nombre + " a " + address + ": " + mess);
                            }
                        }
                        }catch (Exception e){System.err.println(e);}
                    }
                }else{
                        for(user usuarios : clientes){
                            if(!bloqueados.contains(usuarios.getNombre())){
                             usuarios.getEscritor().println("MESSAGE " + nombre + ": " + input);
                            }
                        }
                    }
                }
                
            
            
            
            
            } catch (Exception e){
                System.err.println(e);
            } finally {
                if(out!=null && nombre!=null){
                    System.err.println(nombre + " is leaving");
                    clientes.remove(nombre);
                    for(user usuarios : clientes){
                        usuarios.getEscritor().println("MESSAGE " + nombre + " has left");
                    }
                }
                try{ socket.close(); } catch (IOException e){}
            }
        }
    }
    
}
