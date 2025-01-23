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
    	//TO-DO: Esse behaviour deve esperar o ActiveCellsList enviado pelo GameUIAgent
		// Quando receber a lista, deve mandar uma mensagem para cada um dos CellAgent com o "isAlive"
		// para que eles possam atualizar os próprios estados
		
		// Estrutura da mensagem (Para cada um das células)
		// Tipo: ACL.INFORM
		// Ontology: "inicialState"
		// Content: "isAlive" (Irá como string e deve ser convertido como booleano, pode ser utilizado o ContentObject diretamente se for melhor)
		// Destinatário: cada uma das células
        public void action() {
            
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
