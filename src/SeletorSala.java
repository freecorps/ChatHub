import ChatHub.Client;
import ChatHub.Sala;
import ChatHub.Server;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class SeletorSala extends javax.swing.JFrame {
    
    private Client client;

    public SeletorSala(Client client) {
        this.client = client;
        initComponents();
        atualizarListaDeSalas();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CriarSala = new javax.swing.JToggleButton();
        Conectar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        ListaDeSalas = new javax.swing.JList<>();
        Atualizar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        CriarSala.setText("Criar Sala");
        CriarSala.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CriarSalaActionPerformed(evt);
            }
        });

        Conectar.setText("Conectar");
        Conectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConectarActionPerformed(evt);
            }
        });

        ListaDeSalas.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(ListaDeSalas);

        Atualizar.setText("Atualizar");
        Atualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AtualizarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(CriarSala, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Conectar, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Atualizar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(CriarSala)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Atualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Conectar)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CriarSalaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CriarSalaActionPerformed
        CriarSala criarSala = new CriarSala(this.client, this);
        criarSala.setVisible(true);
    }//GEN-LAST:event_CriarSalaActionPerformed

    public void atualizarListaDeSalas() {
        List<Sala> salas = Server.getInstance(client.getServerPort()).getSalas();
        if (salas != null) {
            String[] nomesDasSalas = salas.stream().map(Sala::getNome).toArray(String[]::new);

            // Agendamos a atualização para a thread do despachante de eventos
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ListaDeSalas.setModel(new javax.swing.AbstractListModel<String>() {
                        public int getSize() { return nomesDasSalas.length; }
                        public String getElementAt(int i) { return nomesDasSalas[i]; }
                    });
                }
            });
        } else {
            System.out.println("A lista de salas ainda não está disponível.");
        }
    }

  
    
    private void ConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConectarActionPerformed
        String selectedRoom = ListaDeSalas.getSelectedValue();
        if (selectedRoom == null) {
            // Mostra uma mensagem se nenhuma sala estiver selecionada
            JOptionPane.showMessageDialog(this, "Selecione uma sala para entrar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, String> message = new HashMap<>();
        message.put("type", "join_room");
        message.put("room", selectedRoom);
        client.sendMessage(message);
        
        atualizarListaDeSalas();

        Chat chatWindow = new Chat(client, selectedRoom);
        chatWindow.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_ConectarActionPerformed

    private void AtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AtualizarActionPerformed
        atualizarListaDeSalas();
    }//GEN-LAST:event_AtualizarActionPerformed

    public static void main(String args[]) {

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
            java.util.logging.Logger.getLogger(SeletorSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SeletorSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SeletorSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SeletorSala.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               // new SeletorSala().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Atualizar;
    private javax.swing.JButton Conectar;
    private javax.swing.JToggleButton CriarSala;
    private javax.swing.JList<String> ListaDeSalas;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
