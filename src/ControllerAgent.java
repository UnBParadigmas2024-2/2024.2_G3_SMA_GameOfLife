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
        addBehaviour(new UpdateGameCycle());
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

        private int step = 0;

        private int responsesReceived = 0;

        private List<AID> aliveCellsInThisCycle = new ArrayList<>();
    
        @Override
        public void action() {
            switch (step) {
                case 0:
                    requestAliveStatus();
                    step = 1;
                    break;
    
                case 1:
                    ACLMessage reply = myAgent.receive();
                    if (reply != null && "verifyIsAlive-response".equals(reply.getOntology())) {
                        responsesReceived++;
    
                        if ("true".equals(reply.getContent())) {
                            aliveCellsInThisCycle.add(reply.getSender());
                        }
    
                        if (responsesReceived == cellAgents.size()) {
                            if (aliveCellsInThisCycle.isEmpty()) {
                                System.out.println("Todas as células estão mortas. Encerrando jogo...");
                                myAgent.doDelete();
                                return;
                            }
                            informUIAgent();
    
                            cycleNum++;
                            responsesReceived = 0;
                            aliveCellsInThisCycle.clear();
                            step = 0; 
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }
    
        private void requestAliveStatus() {
            for (AID cellAID : cellAgents) {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.setOntology("verifyIsAlive");
                req.setContent("Are you alive?");
                req.addReceiver(cellAID);
                myAgent.send(req);
            }
        }
    
        private void informUIAgent() {
            AID uiAgent = searchGameUIAgentInDF(); 
            if (uiAgent != null) {
                ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                informMsg.setOntology("updateUI");
                informMsg.addReceiver(uiAgent);
    
                StringBuilder sb = new StringBuilder();
                sb.append("cycleNum=").append(cycleNum).append(";");
                sb.append("aliveCells=");
                for (int i = 0; i < aliveCellsInThisCycle.size(); i++) {
                    sb.append(aliveCellsInThisCycle.get(i).getLocalName());
                    if (i < aliveCellsInThisCycle.size() - 1) {
                        sb.append(",");
                    }
                }
                informMsg.setContent(sb.toString());
                myAgent.send(informMsg);
    
                System.out.println("Enviando updateUI para GameUIAgent: cycleNum=" + cycleNum);
            }
        }
    }
}
