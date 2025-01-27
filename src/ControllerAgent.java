package src;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;

public class ControllerAgent extends Agent {

    private static final long serialVersionUID = 1L;

    private int cycleNum = 0;
    private List<AID> cellAgents = new ArrayList<>();
    private AID gameUIAgentAID = null;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": inicializando ControllerAgent...");
        registerInDF();
        createCellAgents(25, 25);
        gameUIAgentAID = searchGameUIAgentInDF();

        addBehaviour(new SetAliveCells());
        addBehaviour(new UpdateGameCycle());
        System.out.println(getLocalName() + ": pronto!");
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
                    Object[] args = {x, y};
                    AgentController ac = getContainerController().createNewAgent(cellName, "src.CellAgent", args);
                    ac.start();
                    cellAgents.add(new AID(cellName, AID.ISLOCALNAME));
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                    System.err.println("Erro ao criar agente célula: " + cellName);
                }
            }
        }
    }

    private AID searchGameUIAgentInDF() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("game-ui-agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            if (results.length > 0) {
                return results[0].getName();
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class SetAliveCells extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("ActiveCellsList")) {
                String content = msg.getContent(); 
                List<String> aliveCells = parseAliveCells(content);

                System.out.println("Recebido ActiveCellsList: " + aliveCells);
                // System.out.println("Cell agents: " + cellAgents);


                for (AID cellAID : cellAgents) {
                    System.out.println("Enviando inicialState para " + cellAID.getLocalName());
                    if (!aliveCells.contains(cellAID.getLocalName())) {
                        ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                        informMsg.setOntology("inicialState");
                        informMsg.addReceiver(cellAID);
                        informMsg.setContent(String.valueOf("false"));
                        
                        myAgent.send(informMsg);
                    } else {
                        ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                        informMsg.setOntology("inicialState");
                        informMsg.addReceiver(cellAID);
                        informMsg.setContent(String.valueOf("true"));
                        
                        myAgent.send(informMsg);
                        System.out.println("Enviado inicialState true para " + cellAID.getLocalName());
                    }
                }
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
                            handleCycleEnd();
                            step = 0;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        private void requestAliveStatus() {
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.setOntology("verifyIsAlive");
            req.setContent("Are you alive?");
            for (AID cell : cellAgents) {
                req.addReceiver(cell);
            }
            myAgent.send(req);
        }

        private void handleCycleEnd() {
            if (aliveCellsInThisCycle.isEmpty()) {
                System.out.println("Todas as células estão mortas. Encerrando jogo...");
                doDelete();
            } else {
                informUIAgent();
                cycleNum++;
                responsesReceived = 0;
                aliveCellsInThisCycle.clear();
            }
        }

        private void informUIAgent() {
            if (gameUIAgentAID == null) {
                gameUIAgentAID = searchGameUIAgentInDF();
            }
            if (gameUIAgentAID != null) {
                ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                informMsg.setOntology("updateUI");
                informMsg.addReceiver(gameUIAgentAID);
        
                StringBuilder sb = new StringBuilder();
                sb.append("cycleNum=").append(cycleNum).append(";aliveCells=");
                for (int i = 0; i < aliveCellsInThisCycle.size(); i++) {
                    sb.append(aliveCellsInThisCycle.get(i).getLocalName());
                    if (i < aliveCellsInThisCycle.size() - 1) sb.append(",");
                }
                informMsg.setContent(sb.toString());
                myAgent.send(informMsg);
                System.out.println("Enviado updateUI para GameUIAgent. cycleNum=" + cycleNum);
            } else {
                System.err.println("GameUIAgent não encontrado no DF.");
            }
        }
    }
}
