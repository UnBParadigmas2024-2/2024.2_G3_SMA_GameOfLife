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
    private final int gridSize = 5;
    private Boolean lock = true;

    public Boolean getLock() {
        return lock;
    }

    public void setLock(Boolean lock) {
        this.lock = lock;
    }

    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": inicializando ControllerAgent...");
        registerInDF();
        createCellAgents(gridSize);
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

    private void createCellAgents(int gridSize) {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                String cellName = "CellAgent-" + x + "-" + y;
                try {
                    AgentController ac = getContainerController().createNewAgent(cellName, "src.CellAgent", null);
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
            ACLMessage msg = receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("ActiveCellsList")) {
                String content = msg.getContent();
                List<String> aliveCells = parseAliveCells(content);

                System.out.println("cellAgents(Todos): " + cellAgents);
                System.out.println("aliveCells(Clicados): " + aliveCells);

                for (AID cellAID : cellAgents) {
                    ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                    informMsg.setOntology("newState");
                    informMsg.addReceiver(cellAID);
                    if (!aliveCells.contains(cellAID.getLocalName())) {
                        System.out.println("MORTO: " + cellAID.getLocalName());
                        informMsg.setContent(String.valueOf("false"));
                    } else {
                        System.out.println("VIVO: " + cellAID.getLocalName());
                        informMsg.setContent(String.valueOf("true"));
                    }
                    send(informMsg);
                }
                setLock(false);
            } else {
                block();
            }
        }

        private List<String> parseAliveCells(String content) {
            List<String> result = new ArrayList<>();
            if (content != null && !content.isEmpty()) {
                String[] tokens = content.split(",");
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
            if (!lock) {
                switch (step) {
                    case 0:
                        // Solicita para todas as celulas se vai estar viva no proximo ciclo
                        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                        req.setOntology("willStayAlive");
                        req.setContent("No proximo ciclo, vai estar viva?");

                        for (AID cell : cellAgents) {
                            req.addReceiver(cell);
                        }
                        myAgent.send(req);
                        step = 1;
                        break;

                    case 1:
                        ACLMessage reply = myAgent.receive();

                        if (reply != null && reply.getOntology() != null
                                && reply.getOntology().equals("willStayAliveResponse")) {
                            responsesReceived++;

                            System.out.println(reply.getSender().getLocalName() + " respondeu: " + reply.getContent());

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
        }

        private void handleCycleEnd() {
            if (aliveCellsInThisCycle.isEmpty()) {
                System.out.println("Todas as células estão mortas. Encerrando jogo...");
                doDelete();
            } else {
                informUIAgent();
                setNewCellStates();
                cycleNum++;
                responsesReceived = 0;
                aliveCellsInThisCycle.clear();
            }
        }

        private void setNewCellStates() {
            for (AID cell : cellAgents) {
                ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
                informMsg.setOntology("newState");
                informMsg.addReceiver(cell);
                if (aliveCellsInThisCycle.contains(cell)) {
                    informMsg.setContent("true");
                } else {
                    informMsg.setContent("false");
                }
                myAgent.send(informMsg);
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
                    if (i < aliveCellsInThisCycle.size() - 1)
                        sb.append(",");
                }
                informMsg.setContent(sb.toString());
                myAgent.send(informMsg);
                System.out.println("Enviado updateUI para GameUIAgent. Mensagem: " + informMsg.getContent());
            } else {
                System.err.println("GameUIAgent não encontrado no DF.");
            }
        }
    }
}
