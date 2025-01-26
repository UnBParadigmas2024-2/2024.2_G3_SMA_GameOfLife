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
    private boolean isGamePause = false;

    private List<String> ActiveCellsList = new ArrayList<>();
    private List<String> InicialActiveCellsList = new ArrayList<>();
    
    public List<String> getActiveCellsList() {
        if (ActiveCellsList == null) {
            ActiveCellsList = new ArrayList<>();
        }
        return ActiveCellsList;
    }
    public List<String> getInicialActiveCellsList() {
        if (InicialActiveCellsList == null) {
            InicialActiveCellsList = new ArrayList<>();
        }
        return InicialActiveCellsList;
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
        
        //Inicializar o ControllerAgent
        initializeControllerAgent();
        controllerAgentAID = searchControllerAgentInDF();

        addBehaviour(new UpdateUI());

    }

    private void initializeControllerAgent() {
        try {
            // Verifica se o ControllerAgent já está registrado no DF
            AID controllerAgentAID = searchControllerAgentInDF();
            
            if (controllerAgentAID == null) {
                // Se o ControllerAgent não foi encontrado no DF, cria um novo
                ContainerController container = getContainerController(); 
                Object[] controllerArgs = new Object[] { getAID() }; 
                AgentController controllerAgent = container.createNewAgent(
                    "ControllerAgent",
                    "src.ControllerAgent",
                    controllerArgs
                );
                controllerAgent.start();
                System.out.println("ControllerAgent inicializado com sucesso.");
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


            if (controllerAgentAID == null) {
                controllerAgentAID = searchControllerAgentInDF();
            }
            if (controllerAgentAID != null) {

                try{

                
                    if (isGameStarted) {
                        System.out.println("O jogo já foi iniciado. Botão 'Play' não deve ser renderizado novamente.");
                        return;
                    }
            
                    // Marcar o jogo como iniciado
                    isGameStarted = true;
                    isGamePause = false;
            
                    // Copiar o conteúdo da ActiveCellsList para InicialActiveCellsList
                    ((GameUIAgent) myAgent).getInicialActiveCellsList().addAll(((GameUIAgent) myAgent).getActiveCellsList());
                

                    ACLMessage playMessage = new ACLMessage(ACLMessage.INFORM);
                    playMessage.setOntology("ActiveCellsList");  // Nome da ontologia atualizado
            
                    // Obter a lista de células ativas e convertê-la para o formato esperado pelo ControllerAgent
                    List<String> activeCellsList = ((GameUIAgent) myAgent).getActiveCellsList();
                    
                    StringBuilder content = new StringBuilder();
                    for (String cell : activeCellsList) {
                        content.append(cell).append(";");
                    }
            
                    if (content.length() > 0) {
                        content.deleteCharAt(content.length() - 1);
                    }
            
                    playMessage.setContent(content.toString());
                    playMessage.addReceiver(controllerAgentAID);  // Usando a variável global controllerAgentAID
                    myAgent.send(playMessage);
            
                    System.out.println("Play behavior ativado! ActiveCellsList enviada para o ControllerAgent.");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Behavior para o botão Pausar
    private class PauseBehavior extends OneShotBehaviour {
        public void action() {

            if (controllerAgentAID == null) {
                controllerAgentAID = searchControllerAgentInDF();
            }
            if (controllerAgentAID != null) {

                try{

                    isGamePause = true;
                    isGameStarted = false;
                    ACLMessage pauseMessage = new ACLMessage(ACLMessage.INFORM);
                    pauseMessage.setOntology("pauseCycleUpdate");  // nome da mensagem - Não achamos como Controller Agent lida com isso
                    pauseMessage.addReceiver(controllerAgentAID);
                    myAgent.send(pauseMessage);
                    
                    System.out.println("Pause behavior ativado!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

           
        }
    }

    // Behavior para o botão Reset
    private class ResetBehavior extends OneShotBehaviour {
        public void action() {
            if (controllerAgentAID == null) {
                controllerAgentAID = searchControllerAgentInDF();
            }
            
            if (controllerAgentAID != null) {
                try {
                    // Criar a mensagem ACL
                    ACLMessage resetMessage = new ACLMessage(ACLMessage.INFORM);
                    resetMessage.setOntology("ActiveCellsList");  // colocamos ActiveCellsList, pois não achamos um pra reset em Controller Agent
                    
                    List<String> activeCells = ((GameUIAgent) myAgent).getInicialActiveCellsList();
                    ActiveCellsList = ((GameUIAgent) myAgent).getActiveCellsList();

                    
                    // Converter a lista de células para o formato esperado pela função do ControllerAgent
                    StringBuilder contentBuilder = new StringBuilder();
                    for (String cell : activeCells) {
                        contentBuilder.append(cell).append(";");
                    }
                    
                    resetMessage.setContent(contentBuilder.toString());
                    resetMessage.addReceiver(controllerAgentAID);
                    myAgent.send(resetMessage);
                    
                    System.out.println("Reset behavior ativado!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("ControllerAgent não encontrado no DF.");
            }
        }
    }

    // Behavior para o botão Limpar
    private class ClearGridBehavior extends OneShotBehaviour {
        public void action() {
            System.out.println("Clear grid behavior ativado!");
    
            // Verificar se o jogo já foi iniciado e se não está pausado
            if (isGameStarted && !isGamePause) {
                System.out.println("O jogo já foi iniciado. Botão 'Clean' não deve funcionar.");
                return;
            }
    
            // Zerar a lista de células vivas
            ActiveCellsList.clear();
            InicialActiveCellsList.clear();
    
            try {
                gameUI.clearAllCells();
            } catch (Exception e) {
                System.err.println("Erro ao limpar as células: " + e.getMessage());
                e.printStackTrace();
            }
    
            System.out.println("Grade limpa, ActiveCellsList zerada, e interface atualizada!");
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
            String cell = x + "," + y;
    
            List<String> activeCellsList = ((GameUIAgent) myAgent).getActiveCellsList();
            if (activeCellsList.contains(cell)) {
                activeCellsList.remove(cell);
            } else {
                activeCellsList.add(cell);
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
            if (message != null) {
                if ("updateUI".equals(message.getOntology())) {
                    try {
                        String content = message.getContent();
                        if (content != null) {
                            // Parsear o conteúdo no formato "cycleNum=<num>;aliveCells=<nome1,nome2,...>"
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
                    System.out.println("Mensagem com ontologia inesperada recebida: " + message.getOntology());
                }
            } else {
                block();
            }
        }
    }

}