package src;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameUI extends JFrame {
    private JButton startButton, pauseButton, resetButton, clearButton;
    private JLabel cycleLabel, aliveCellsLabel;
    private JPanel gridPanel;
    private boolean[][] cellStates;
    private final int gridSize = 25; // Tamanho da grade
    private GameUIAgent agent; // Referência ao agente

    public GameUI() {
        setTitle("Game of Life");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Inicializa o estado das células
        cellStates = new boolean[gridSize][gridSize];

        // Painel de controle
        JPanel controlPanel = new JPanel();
        startButton = new JButton("Play");
        pauseButton = new JButton("Pausar");
        resetButton = new JButton("Reset");
        clearButton = new JButton("Limpar");
        cycleLabel = new JLabel("Ciclo: 0");
        aliveCellsLabel = new JLabel("Células Vivas: 0");

        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resetButton);
        controlPanel.add(clearButton);
        controlPanel.add(cycleLabel);
        controlPanel.add(aliveCellsLabel);

        add(controlPanel, BorderLayout.NORTH);

        // Painel da grade
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(gridSize, gridSize));
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JButton cellButton = new JButton();
                cellButton.setBackground(Color.WHITE);
                final int x = i, y = j;
                cellButton.addActionListener(e -> handleCellSelection(x, y, cellButton));
                gridPanel.add(cellButton);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        // Adicionar ações aos botões
        startButton.addActionListener(e -> triggerBehavior("play"));
        pauseButton.addActionListener(e -> triggerBehavior("pause"));
        resetButton.addActionListener(e -> triggerBehavior("reset"));
        clearButton.addActionListener(e -> triggerBehavior("clear"));
    }

    public void setAgent(GameUIAgent agent) {

        this.agent = agent;
    }

    private void triggerBehavior(String behaviorName) {
        if (agent != null) {
            agent.addBehaviorByName(behaviorName);
        } else {
            System.err.println("Agente não está definido!");
        }
    }

    private void handleCellSelection(int x, int y, JButton button) {
        cellStates[x][y] = !cellStates[x][y];
        button.setBackground(cellStates[x][y] ? Color.BLACK : Color.WHITE);

        // Envia comando para o agente informando a célula clicada
        if (agent != null) {
            agent.addBehaviorByName("cell", x, y);
        } else {
            System.err.println("Agente não está definido!");
        }
    }

    void clearAllCells() {
        if (gridPanel == null || cellStates == null) {
            System.err.println("Erro: gridPanel ou cellStates não foram inicializados!");
            return;
        }
    
        // Zerar todas as células
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cellStates[i][j] = false;
                JButton cellButton = (JButton) gridPanel.getComponent(i * gridSize + j);
                if (cellButton != null) {
                    cellButton.setBackground(Color.WHITE);  // Limpa a célula para branco
                }
            }
        }
    }

    public void onUIUpdate(int newCycleNum, int newAliveCellsCount, List<String> activeCellsList) {
        if (activeCellsList == null) {
            activeCellsList = new ArrayList<>();
        }
        updateInterface(newCycleNum, newAliveCellsCount);
        updateActiveCells(activeCellsList);
    }
    

    private void updateInterface(int cycleNum, int aliveCellsCount) {
        cycleLabel.setText("Ciclo: " + cycleNum);
        aliveCellsLabel.setText("Células Vivas: " + aliveCellsCount);
    }
    
    private void updateActiveCells(List<String> activeCellsList) {
        if (activeCellsList == null || gridPanel == null) {
            return;
        }
    
        // Zerar o estado de todas as células
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JButton cellButton = (JButton) gridPanel.getComponent(i * gridSize + j);
                cellButton.setBackground(Color.WHITE);  
            }
        }
    
        // Marcar as células ativas
        for (String cellName : activeCellsList) {
            try {
                String[] parts = cellName.split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
    
                if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
                    JButton cellButton = (JButton) gridPanel.getComponent(x * gridSize + y);
                    cellButton.setBackground(Color.BLACK); 
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar célula: " + cellName);
            }
        }
    }
    
}
