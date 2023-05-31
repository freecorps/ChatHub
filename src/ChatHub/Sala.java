package ChatHub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sala {
    private String nome;
    private Map<String, Integer> jogadores;

    public Sala(String nome) {
        this.nome = nome;
        this.jogadores = new HashMap<>();
    }

    public String getNome() {
        return nome;
    }

    public Map<String, Integer> getJogadores() {
        return jogadores;
    }

    public void addJogador(String nomeJogador, int pontuacao) {
        this.jogadores.put(nomeJogador, pontuacao);
    }
    
    // Retorna uma lista de jogadores na sala
    public List<String> getListaJogadores() {
        return new ArrayList<>(jogadores.keySet());
    }

}