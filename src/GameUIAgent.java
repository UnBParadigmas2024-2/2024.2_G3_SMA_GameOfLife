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
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;


public class GameUIAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private GameUI gameUI;

    private boolean isGameStarted = false;
    private boolean isGamePause = false;

    private List<Point> ActiveCellsList = new ArrayList<>();
    private List<Point> InicialActiveCellsList = new ArrayList<>();
    
    public Serializable getActiveCellsListSerializable() {
        if (ActiveCellsList == null) {
            ActiveCellsList = new ArrayList<>();
        }
        return (Serializable) ActiveCellsList;
    }

    public List<Point> getActiveCellsList() {
        if (ActiveCellsList == null) {
            ActiveCellsList = new ArrayList<>();
        }
        return ActiveCellsList;
    }
    public List<Point> getInicialActiveCellsList() {
        if (InicialActiveCellsList == null) {
            InicialActiveCellsList = new ArrayList<>();
        }
        return InicialActiveCellsList;
    }

    public Serializable getInicialActiveCellsListSerializable() {
        if (InicialActiveCellsList == null) {
            InicialActiveCellsList = new ArrayList<>();
        }
        return (Serializable) InicialActiveCellsList;
    }


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
        
        //TO-DO: Inicializar o ControllerAgent
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
        //TO-DO: Mandar a ActiveCellsList para o ControllerAgent que irá criar os agentes para cada célula
    	// e iniciar os ciclos do jogo. OK
    	// O valor inicial da ActiveCellsList deve ser salva em outra lista para ser usada no reset InicialActiveCellsList OK
    	// O botão "Play" só deve ser renderizado se o jogo não tiver sido começado
    	
    	// Estrutura da mensagem
    	// Tipo: ACL.INFORM OK
    	// ContentObject: ActiveCellsList OK
    	// Destinatário: ControllerAgent (Pegar pelo DF)
        public void action() {

            if (isGameStarted) {
                System.out.println("O jogo já foi iniciado. Botão 'Play' não deve ser renderizado novamente.");
                return;
            }
    
            // Marcar o jogo como iniciado
            isGameStarted = true;
            isGamePause = false;
    

            // Copiar o conteúdo da ActiveCellsList para InicialActiveCellsList
            ((GameUIAgent) myAgent).getInicialActiveCellsList().addAll(((GameUIAgent) myAgent).getActiveCellsList());
          
            // Criar a mensagem ACL para enviar a ActiveCellsList ao ControllerAgent
            ACLMessage playMessage = new ACLMessage(ACLMessage.INFORM);
            playMessage.setOntology("startGameCycle");  // Nome da ontologia
            try {
                playMessage.setContentObject(((GameUIAgent) myAgent).getActiveCellsListSerializable());
            } catch (IOException e) {
                e.printStackTrace();
            }  // Adicionando a ActiveCellsList
            
            // Criar AID para o ControllerAgent e adicionar como receptor
            AID controllerAID = new AID("ControllerAgent", AID.ISLOCALNAME);
            playMessage.addReceiver(controllerAID);
    
            // Enviar a mensagem 
            myAgent.send(playMessage);
    
            System.out.println("Play behavior ativado!");
        }
    }
    // Behavior para o botão Pausar
    private class PauseBehavior extends OneShotBehaviour {
    	//TO-DO: Mandar mensagem para o ControllerAgent informando para pausar o comportamento ciclico
    	// que atualiza os ciclos e agentes vivos/mortos OK
        public void action() {
            isGamePause = true;
            isGameStarted = false;
            // Criando a mensagem ACL
            ACLMessage pauseMessage = new ACLMessage(ACLMessage.INFORM);
            pauseMessage.setOntology("pauseCycleUpdate");  // nome da mensagem
            // Criando o AID diretamente com o nome conhecido
            AID controllerAID = new AID("ControllerAgent", AID.ISLOCALNAME); // nome do agente
            pauseMessage.addReceiver(controllerAID);
            myAgent.send(pauseMessage);
            
            System.out.println("Pause behavior ativado!");
        }
    }

    // Behavior para o botão Reset
    private class ResetBehavior extends OneShotBehaviour {
        // TO-DO: enviar mensagem para o ControllerAgent para reiniciar os ciclos
        // e utilizar o valor de InicialActiveCellsList para criar os agentes vivos
        public void action() {
            // Criando a mensagem ACL
            ACLMessage resetMessage = new ACLMessage(ACLMessage.INFORM);
            resetMessage.setOntology("resetCycle");  // nome da mensagem
            // Criando o AID diretamente com o nome conhecido

            try {
                resetMessage.setContentObject(((GameUIAgent) myAgent).getInicialActiveCellsListSerializable());
            } catch (IOException e) {
                e.printStackTrace();
            }  // Adicionando a ActiveCellsList
            AID controllerAID = new AID("ControllerAgent", AID.ISLOCALNAME); // nome do agente
            resetMessage.addReceiver(controllerAID);
            myAgent.send(resetMessage);
            
            System.out.println("Reset behavior ativado!");
        }
    }

    // Behavior para o botão Limpar
    private class ClearGridBehavior extends OneShotBehaviour {
    	//TO-DO: O botão "limpar" só deve estar renderizado antes do jogo começar  
    	// Ao apertar ele, a ActiveCellsList deve ser zerada


        // De acordo com o jogo game of live , se ele pausar e dar reset também pode ativar
        public void action() {
            System.out.println("Clear grid behavior ativado!");

            if (isGameStarted && !isGamePause) {
                System.out.println("O jogo já foi iniciado. Botão 'Clean' não deve ser funcionar");
                return;
            }

            // Zerar a lista de células vivas
            ActiveCellsList.clear();
            InicialActiveCellsList.clear();

            // Enviar mensagem para o ControllerAgent (se necessário)
            ACLMessage clearMessage = new ACLMessage(ACLMessage.INFORM);
            clearMessage.setOntology("clearGrid"); // Nome da mensagem
            clearMessage.addReceiver(new AID("ControllerAgent", AID.ISLOCALNAME));
            myAgent.send(clearMessage);

            GameUI gameUI = (GameUI) myAgent.getArguments()[0]; 
            gameUI.clearAllCells();

            System.out.println("Grade limpa, ActiveCellsList zerada, e interface atualizada!");
        }
    }

    // Behavior para a seleção de células
    private class CellSelectionBehavior extends OneShotBehaviour {
        // TO-DO: Atualizar uma lista (ActiveCellsList) de células que estão ativadas.
        // Adicionar na lista quando for selecionada e tirar da lista quando for desselecionada
        private final int x;
        private final int y;
    
        public CellSelectionBehavior(int x, int y) {
            this.x = x;
            this.y = y;
        }
    
        public void action() {
            // Adicionar ou remover a célula da ActiveCellsList
            if (((GameUIAgent) myAgent).getActiveCellsList().contains(new Point(x, y))) {
                ((GameUIAgent) myAgent).getActiveCellsList().remove(new Point(x, y));
            } else {
                ((GameUIAgent) myAgent).getActiveCellsList().add(new Point(x, y));
            }
    
            System.out.println("Célula selecionada: (" + x + ", " + y + ")");
        }
    }
    
    
    private class UpdateUI extends CyclicBehaviour {
    	//TO-DO: Ficará em ciclo esperando uma mensagem de atualização do ControllerAgent
    	// Essa mensagem deverá conter uma lista dos agentes vivos (ActiveCellsList) e o "cicleNum", número do ciclo atual
    	// Quando receber esta mensagem, deverá atualizar a interface com os agentes vivos e o número do ciclo
        private int cycleNum = 0; // Número do ciclo atual
        private int aliveCellsCount = 0; // Contador de células vivas
    
        @Override
        public void action() {
            // Esperar por mensagens do tipo ACL.INFORM
            ACLMessage message = myAgent.receive();
            if (message != null) {
                if ("updateUI".equals(message.getOntology())) {
                    try {
                        // Deserializar o conteúdo da mensagem
                        Object contentObject = message.getContentObject();
    
                        if (contentObject instanceof Object[]) {
                            Object[] content = (Object[]) contentObject;
    
                            // Verificar e fazer cast seguro da lista de células vivas
                            if (content[0] instanceof List) {
                                @SuppressWarnings("unchecked") // Suprimir warnings de cast não verificado
                                List<Point> activeCells = (List<Point>) content[0];
                                cycleNum = (int) content[1]; // Número do ciclo atual
    
                                // Atualizar a lista ActiveCellsList
                                ActiveCellsList.clear();
                                ActiveCellsList.addAll(activeCells);
    
                                // Atualizar o contador de células vivas
                                aliveCellsCount = ActiveCellsList.size();
    
                                // Atualizar a interface (exemplo de impressão no console)
                                System.out.println("Ciclo Atual: " + cycleNum);
                                System.out.println("Células Vivas: " + aliveCellsCount);
    
                                // Se necessário, atualizar interface gráfica
                                // updateGraphicUI(ActiveCellsList, cycleNum, aliveCellsCount);
                                GameUI gameUI = (GameUI) myAgent.getArguments()[0]; 
                                gameUI.onUIUpdate(cycleNum, aliveCellsCount);

                            } else {
                                System.err.println("Erro: O conteúdo da mensagem não contém uma lista válida de células.");
                            }
                        } else {
                            System.err.println("Erro: O conteúdo da mensagem não é um array de objetos.");
                        }
                    } catch (UnreadableException e) {
                        System.err.println("Erro ao desserializar o conteúdo da mensagem: " + e.getMessage());
                    }
                } else {
                    System.out.println("Mensagem com ontologia inesperada recebida: " + message.getOntology());
                }
            } else {
                // Bloqueio para evitar consumo excessivo de recursos
                block();
            }
        }
    }
}