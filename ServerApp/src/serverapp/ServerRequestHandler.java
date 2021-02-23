/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverapp;

/**
 *
 * @author praksa
 */
class ServerRequestHandler extends Thread {
    
    private static int port;

    ServerRequestHandler(int parseInt) {
         this.port= parseInt;
    }

    @Override
    public void run() {
        
    }
    
    
}
