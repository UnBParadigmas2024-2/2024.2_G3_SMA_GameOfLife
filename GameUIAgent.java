import javax.swing.SwingUtilities;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
            System.out.println("Play behavior ativado!");
        }
    }

    // Behavior para o botão Pausar
    private class PauseBehavior extends OneShotBehaviour {
    	//TO-DO: Mandar mensagem para o ControllerAgent informando para pausar o comportamento ciclico
    	// que atualiza os ciclos e agentes vivos/mortos
        public void action() {
            System.out.println("Pause behavior ativado!");
        }
    }

    // Behavior para o botão Reset
    private class ResetBehavior extends OneShotBehaviour {
    	//TO-DO: mandar mensagem para o controllerAgent para que seja reiniciado os ciclos
    	// e utilizado o valor do InicialActiveCellsList para criar os agentes vivos
        public void action() {
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
    	//TO-DO: Atualizar uma lista (ActiveCellsList) de células que estão ativadas. 
    	//Adicionar na lista quando for selecionada e tirar da lista quando for desselecionada
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
    
    private class UpdateUI extends CyclicBehaviour {
    	//TO-DO: Ficará em ciclo esperando uma mensagem de atualização do ControllerAgent
    	// Essa mensagem deverá conter uma lista dos agentes vivos (ActiveCellsList) e o "cicleNum", número do ciclo atual
    	// Quando receber esta mensagem, deverá atualizar a interface com os agentes vivos e o número do ciclo
        public void action() {
            
        }
    }
	
}
