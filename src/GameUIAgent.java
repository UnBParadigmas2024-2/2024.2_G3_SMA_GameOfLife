package src;

import javax.swing.SwingUtilities;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameUIAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private GameUI gameUI;
    private AID controllerAgentAID = null;
    private boolean isGameStarted = false;

    private List<String> ActiveCellsList = new ArrayList<>();

    public List<String> getActiveCellsList() {
        if (ActiveCellsList == null) {
            ActiveCellsList = new ArrayList<>();
        }
        return ActiveCellsList;
    }

    protected void setup() {
        // Registrar o agente no DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("game-ui-agent");
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

        // Inicializar o ControllerAgent
        initializeControllerAgent();
        controllerAgentAID = searchControllerAgentInDF();

        addBehaviour(new UpdateUI());

    }

    private void initializeControllerAgent() {
        try {
            AID controllerAgentAID = searchControllerAgentInDF();
            if (controllerAgentAID == null) {
                ContainerController container = getContainerController();
                Object[] controllerArgs = new Object[] { getAID() };
                AgentController controllerAgent = container.createNewAgent(
                        "ControllerAgent",
                        "src.ControllerAgent",
                        controllerArgs);
                controllerAgent.start();
                System.out.println("ControllerAgent inicializado com sucesso.");
            } else {
                System.out.println("ControllerAgent já está registrado no DF.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao inicializar o ControllerAgent.");
        }
    }

    private AID searchControllerAgentInDF() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("controller-agent"); // Alinha com o tipo registrado no ControllerAgent
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                return results[0].getName(); // Retorna o AID do primeiro agente encontrado
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("UI-agent " + getAID().getName() + " encerrando.");
    }

    public void addBehaviorByName(String behaviorName) {
        if ("play".equals(behaviorName)) {
            addBehaviour(new PlayBehavior());
        }
    }

    public void addBehaviorByName(String behaviorName, int x, int y) {
        if ("cell".equals(behaviorName)) {
            addBehaviour(new CellSelectionBehavior(x, y));
        }
    }

    private class PlayBehavior extends OneShotBehaviour {
        public void action() {
            if (controllerAgentAID == null) {
                controllerAgentAID = searchControllerAgentInDF();
            }
            if (controllerAgentAID != null) {
                try {
                    if (isGameStarted) {
                        System.out.println("O jogo já foi iniciado. Botão 'Play' não deve pode ser clicado novamente.");
                        return;
                    }
                    isGameStarted = true;

                    ACLMessage playMessage = new ACLMessage(ACLMessage.INFORM);
                    playMessage.setOntology("ActiveCellsList");

                    StringBuilder content = new StringBuilder();
                    for (String cell : ActiveCellsList) {
                        content.append(cell).append(",");
                    }

                    playMessage.setContent(content.toString());
                    playMessage.addReceiver(controllerAgentAID);
                    myAgent.send(playMessage);

                    System.out.println("Play behavior ativado! Mensagem enviada: " + playMessage.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("ControllerAgent não encontrado no DF.");
            }
        }
    }

    private class CellSelectionBehavior extends OneShotBehaviour {
        private final int x;
        private final int y;

        public CellSelectionBehavior(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void action() {
            String cell = "CellAgent-" + x + "-" + y;

            if (ActiveCellsList.contains(cell)) {
                ActiveCellsList.remove(cell);
            } else {
                ActiveCellsList.add(cell);
            }

            System.out.println("Célula selecionada: (" + x + ", " + y + ")");
        }
    }

    private class UpdateUI extends CyclicBehaviour {
        private int cycleNum = 0;
        private int aliveCellsCount = 0;

        @Override
        public void action() {
            ACLMessage message = myAgent.receive();
            if (message != null && "updateUI".equals(message.getOntology())) {
                try {
                    String content = message.getContent();
                    if (content != null) {
                        String[] parts = content.split(";");
                        for (String part : parts) {
                            if (part.startsWith("cycleNum=")) {
                                cycleNum = Integer.parseInt(part.split("=")[1]);
                            } else if (part.startsWith("aliveCells=")) {
                                String cells = part.split("=")[1];
                                if (!cells.isEmpty()) {
                                    String[] cellNames = cells.split(",");
                                    ActiveCellsList.clear();
                                    ActiveCellsList.addAll(Arrays.asList(cellNames));
                                } else {
                                    ActiveCellsList.clear(); // Nenhuma célula viva
                                }
                            }
                        }

                        aliveCellsCount = ActiveCellsList.size();

                        // Atualizar a interface
                        System.out.println("Ciclo Atual: " + cycleNum);
                        System.out.println("Células Vivas: " + aliveCellsCount);
                        System.out.println("Lista de Células Vivas: " + ActiveCellsList);

                        // Atualizar interface gráfica
                        gameUI.onUIUpdate(cycleNum, aliveCellsCount, ActiveCellsList);
                    } else {
                        System.err.println("Erro: Mensagem recebida com conteúdo nulo.");
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar a mensagem: " + e.getMessage());
                }
            } else {
                block();
            }
        }
    }

}