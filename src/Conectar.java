import ChatHub.Client;
import ChatHub.Server;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class Conectar extends javax.swing.JFrame {
    
    public Conectar() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        Hostear = new javax.swing.JButton();
        Ip = new javax.swing.JTextField();
        Porta = new javax.swing.JTextField();
        Conectar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36)); // NOI18N
        jLabel1.setText("ChatHub");

        Hostear.setText("Hostear");
        Hostear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HostearActionPerformed(evt);
            }
        });

        Ip.setText("IP");
        Ip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IpActionPerformed(evt);
            }
        });

        Porta.setText("Porta");

        Conectar.setText("Conectar");
        Conectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConectarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(Ip, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(Porta, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addComponent(jLabel1)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(Conectar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Hostear)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Ip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Porta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Hostear)
                    .addComponent(Conectar))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void HostearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HostearActionPerformed
        Thread serverThread = new Thread(() -> {
        int serverPort = Integer.parseInt(Porta.getText());
        Server server = Server.getInstance(serverPort);
        server.start();
    });
    serverThread.start();

    JOptionPane.showMessageDialog(this, "Servidor iniciado com sucesso na porta " + Porta.getText(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_HostearActionPerformed

    private void IpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IpActionPerformed

    private void ConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConectarActionPerformed
    String serverAddress = Ip.getText();
    int serverPort = Integer.parseInt(Porta.getText());

    SwingWorker<Client, Void> worker = new SwingWorker<Client, Void>() {
        @Override
        protected Client doInBackground() throws Exception {
            Client client = new Client(serverAddress, serverPort);

            Map<String, String> message = new HashMap<>();
            message.put("type", "example");
            message.put("content", "Hello, server!");
            client.sendMessage(message);

            return client;
        }

        @Override
        protected void done() {
            try {
                Client client = get();
                SeletorSala selecionarSalaFrame = new SeletorSala(client);
                selecionarSalaFrame.setVisible(true);
                // E feche a tela atual
                setVisible(false);
            } catch (InterruptedException | ExecutionException e) {
                JOptionPane.showMessageDialog(Conectar.this, "Erro ao conectar ao servidor: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    };

    worker.execute();
    }//GEN-LAST:event_ConectarActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Conectar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Conectar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Conectar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Conectar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Conectar().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Conectar;
    private javax.swing.JButton Hostear;
    private javax.swing.JTextField Ip;
    private javax.swing.JTextField Porta;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
