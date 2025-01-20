import javax.swing.SwingUtilities;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class GameUIAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private GameUI gameUI;

    protected void setup() {
        // Registrar o agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("UIAgent");
        sd.setName("GameUIAgent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println("GameUIAgent iniciado.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Inicializar a interface gráfica
        SwingUtilities.invokeLater(() -> {
            gameUI = new GameUI();
            gameUI.setVisible(true);
            gameUI.setAgent(this); // Passa uma referência do agente para a UI
        });
    }

    protected void takeDown() {
        // Remover registro do agente no DF
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("UI-agent " + getAID().getName() + " encerrando.");
    }

    public void addBehaviorByName(String behaviorName) {
        switch (behaviorName) {
            case "play":
                addBehaviour(new PlayBehavior());
                break;
            case "pause":
                addBehaviour(new PauseBehavior());
                break;
            case "reset":
                addBehaviour(new ResetBehavior());
                break;
            case "clear":
                addBehaviour(new ClearGridBehavior());
                break;
        }
    }

    public void addBehaviorByName(String behaviorName, int x, int y) {
        if ("cell".equals(behaviorName)) {
            addBehaviour(new CellSelectionBehavior(x, y));
        }
    }

    // Behavior para o botão Play
    private class PlayBehavior extends OneShotBehaviour {
        public void action() {
            System.out.println("Play behavior ativado!");
        }
    }

    // Behavior para o botão Pausar
    private class PauseBehavior extends OneShotBehaviour {
        public void action() {
            System.out.println("Pause behavior ativado!");
        }
    }

    // Behavior para o botão Reset
    private class ResetBehavior extends OneShotBehaviour {
        public void action() {
            System.out.println("Reset behavior ativado!");
        }
    }

    // Behavior para o botão Limpar
    private class ClearGridBehavior extends OneShotBehaviour {
        public void action() {
            System.out.println("Clear grid behavior ativado!");
        }
    }

    // Behavior para a seleção de células
    private class CellSelectionBehavior extends OneShotBehaviour {
        private final int x;
        private final int y;

        public CellSelectionBehavior(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void action() {
            System.out.println("Célula selecionada: (" + x + ", " + y + ")");
        }
    }
}
