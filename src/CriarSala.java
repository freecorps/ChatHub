
import ChatHub.Client;
import ChatHub.Sala;
import ChatHub.Server;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class CriarSala extends javax.swing.JFrame {
    private SeletorSala seletorSala;
    private Client client;

    public CriarSala(Client client, SeletorSala seletorSala) {
        this.client = client;
        this.seletorSala = seletorSala;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NomeDaSala = new javax.swing.JTextField();
        TopicoDaSala = new javax.swing.JTextField();
        Criar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        NomeDaSala.setText("Nome da sala");

        TopicoDaSala.setText("Tópico da sala");

        Criar.setText("Criar");
        Criar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CriarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(NomeDaSala)
                    .addComponent(TopicoDaSala, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Criar)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Criar)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(NomeDaSala, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TopicoDaSala, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CriarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CriarActionPerformed
        
        String nomeDaSala = NomeDaSala.getText();
        String topicoDaSala = TopicoDaSala.getText();

        // Verifica se o nome da sala e o tópico não estão vazios
        if (nomeDaSala.isEmpty() || topicoDaSala.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome da sala e tópico são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Sala novaSala = new Sala(nomeDaSala);

        // Adiciona a nova sala ao servidor
        Server.getInstance(null).addSala(novaSala);
        seletorSala.atualizarListaDeSalas();
        try {
            // Envia uma mensagem ao servidor para entrar na sala
            Server.getInstance(null).joinRoom(client.getClientUUID(), nomeDaSala);
        } catch (InterruptedException ex) {
            Logger.getLogger(CriarSala.class.getName()).log(Level.SEVERE, null, ex);
        }
        Map<String, String> message = new HashMap<>();
        message.put("action", "joinRoom");
        message.put("room", nomeDaSala);
        client.sendMessage(message);

        // Abre a janela de chat para a nova sala e fecha esta janela
        Chat chatWindow = new Chat(client, nomeDaSala);
        seletorSala.setVisible(false);
        chatWindow.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_CriarActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CriarSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CriarSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CriarSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CriarSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new CriarSala().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Criar;
    private javax.swing.JTextField NomeDaSala;
    private javax.swing.JTextField TopicoDaSala;
    // End of variables declaration//GEN-END:variables
}
