package src;

import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;

public class ControllerAgent extends Agent {

	private static final long serialVersionUID = 1L;

    private int cycleNum = 0;

    private List<AID> cellAgents = new ArrayList<>();

	protected void setup() {
		System.out.println(getLocalName() + ": inicializando ControllerAgent...");

        registerInDF();

        createCellAgents(5, 5);

        addBehaviour(new SetAliveCells());
    }
	
	private void registerInDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            
            ServiceDescription sd = new ServiceDescription();
            sd.setType("controller-agent");
            sd.setName(getLocalName() + "-controller");
            
            dfd.addServices(sd);
    
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void createCellAgents(int width, int height) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                String cellName = "CellAgent-" + x + "-" + y;
                try {
                    AgentController ac =
                    getContainerController().createNewAgent(cellName, "src.CellAgent", null);
                    ac.start();
                    
                    // Adiciona o AID na lista
                    AID aid = new AID(cellName, AID.ISLOCALNAME);
                    cellAgents.add(aid);
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	private class SetAliveCells extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null && "ActiveCellsList".equals(msg.getOntology())) {
                String content = msg.getContent(); 
                
                List<String> aliveCells = parseAliveCells(content);
    
                for (AID cellAID : cellAgents) {
                    boolean isAlive = aliveCells.contains(cellAID.getLocalName());
                    
                    ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                    informMsg.setOntology("inicialState");
                    informMsg.setContent(String.valueOf(isAlive));
                    informMsg.addReceiver(cellAID);
    
                    myAgent.send(informMsg);
                }
            } else {
                block();
            }
        }
    
        private List<String> parseAliveCells(String content) {
            List<String> result = new ArrayList<>();
            if (content != null && !content.isEmpty()) {
                String[] tokens = content.split(";");
                for (String t : tokens) {
                    result.add(t.trim());
                }
            }
            return result;
        }
    }
	
	private class UpdateGameCycle extends CyclicBehaviour {
    	//TO-DO: Será iniciado depois do SetAliveCells, irá começar no ciclo 0.
		// Passo 1: Enviará uma mensagem para todos os agentes de células perguntando se continuam vivos ou mortos
		// Passo 2: Então irá esperar todos os agentes de células retornarem com uma mensagem dizendo se estão vivos/mortos
		// Deverá criar uma nova lista de agentes vivos e mandá-la de volta para o GameUIAgent que irá atualizar a interface
		// Se todos os agentes estiverem mortos, o jogo deve ser encerrado.
		// Passo 3: Junto com a nova lista, deve ser mandado o valor da variável "cycleNum" incrementada que também estará na interface
		// Volta para Passo 1
		
		// Estrutura da mensagem do Passo 1
		// Tipo: ACL.REQUEST
		// Ontology: "verifyIsAlive"
		// Destinatário: cada uma das células
		
		// Estrutura da mensagem do Passo 3
		// Tipo: ACL.INFORM
		// Ontology: "updateUI"
		// ContentObject: ActiveCellsList, cycleNum
		// Destinatário: GameUIAgent (Pegar no DF)
		public void action() {
            
        }
    }
}
