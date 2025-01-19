import javax.swing.*;
import java.awt.*;

public class GameUI extends JFrame {
    private JButton startButton, pauseButton, resetButton, clearButton;
    private JLabel cycleLabel, aliveCellsLabel;
    private JPanel gridPanel;
    private boolean[][] cellStates;
    private final int gridSize = 25; // Tamanho da grade

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
                cellButton.addActionListener(e -> toggleCellState(x, y, cellButton));
                gridPanel.add(cellButton);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        // Adicionar ações aos botões
        startButton.addActionListener(e -> startGame());
        pauseButton.addActionListener(e -> pauseGame());
        resetButton.addActionListener(e -> resetGame());
        clearButton.addActionListener(e -> clearGrid());
    }

    private void toggleCellState(int x, int y, JButton button) {
        cellStates[x][y] = !cellStates[x][y];
        button.setBackground(cellStates[x][y] ? Color.BLACK : Color.WHITE);
    }

    private void startGame() {
        // Enviar comando para o agente controlador iniciar a simulação
    }

    private void pauseGame() {
        // Enviar comando para o agente controlador pausar
    }

    private void resetGame() {
        // Reiniciar os estados para o inicial
    }

    private void clearGrid() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cellStates[i][j] = false;
                ((JButton) gridPanel.getComponent(i * gridSize + j)).setBackground(Color.WHITE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameUI().setVisible(true));
    }
}
