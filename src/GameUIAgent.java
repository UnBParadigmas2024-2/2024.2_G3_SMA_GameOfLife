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
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;


public class GameUIAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private GameUI gameUI;

    private List<Point> activeCellsList;
    private List<Point> InicialActiveCellsList;
    // Método para obter a lista de células ativas
    public List<Point> getActiveCellsList() {
        if (activeCellsList == null) {
            activeCellsList = new ArrayList<>();
        }
        return activeCellsList;
    }
    public List<Point> getInicialActiveCellsList() {
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
    	// e iniciar os ciclos do jogo.
    	// O valor inicial da ActiveCellsList deve ser salva em outra lista para ser usada no reset.
    	// InicialActiveCellsList
    	// O botão "Play" só deve ser renderizado se o jogo não tiver sido começado
    	
    	// Estrutura da mensagem
    	// Tipo: ACL.INFORM
    	// ContentObject: ActiveCellsList
    	// Destinatário: ControllerAgent (Pegar pelo DF)
        public void action() {
            // Copiar o conteúdo da ActiveCellsList para InicialActiveCellsList
            ((GameUIAgent) myAgent).getInicialActiveCellsList().addAll(((GameUIAgent) myAgent).getActiveCellsList());
    
            // Criar a mensagem ACL para enviar a ActiveCellsList ao ControllerAgent
            ACLMessage playMessage = new ACLMessage(ACLMessage.INFORM);
            playMessage.setOntology("startGameCycle");  // Nome da ontologia
            playMessage.setContentObject(((GameUIAgent) myAgent).getActiveCellsList());  // Adicionando a ActiveCellsList
    
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
    	// que atualiza os ciclos e agentes vivos/mortos
        public void action() {
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
        public void action() {
            System.out.println("Clear grid behavior ativado!");
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
        public void action() {
            
        }
    }
	
}
