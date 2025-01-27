package src;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class CellAgent extends Agent {

    private static final long serialVersionUID = 1L;
    private boolean isAlive = false;

    @Override
    protected void setup() {
        registerOnDF();
        addBehaviour(new SetState());
        addBehaviour(new CheckNextState());
        addBehaviour(new CheckCurrentState());
    }

    private void registerOnDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("CellAgent");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + ": registrado no DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
            doDelete();
        }
    }

    private synchronized void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public synchronized boolean isAlive() {
        return this.isAlive;
    }

    private int[] getCoordinates() {
        try {
            String[] coordinates = getLocalName().replace("CellAgent-", "").split("-");
            int x = Integer.parseInt(coordinates[0]);
            int y = Integer.parseInt(coordinates[1]);
            return new int[]{x, y};
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{-1, -1}; // Retorna coordenadas inválidas em caso de erro
        }
    }

    private class SetState extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("newState")) {
                String content = msg.getContent();
                System.out.println(getLocalName() + ": Estado recebido (" + content + ")");
                if (content != null && !content.isEmpty()) {
                    System.out.println(getLocalName() + ": Estado alterado para (" + content + ")");
                    setAlive(Boolean.parseBoolean(content));
                }
            }
        }
    }

    private class CheckNextState extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("willStayAlive")) {
                int livingNeighbors = getLivingNeighbors();
                boolean nextState = false;
                if (isAlive() && (livingNeighbors < 2 || livingNeighbors > 3)) {
                    nextState = false;
                } else if (!isAlive() && livingNeighbors == 3) {
                    nextState = true;
                }

                ACLMessage response = msg.createReply();
                response.setOntology("willStayAliveResponse");
                response.setContent(Boolean.toString(nextState));
                send(response);
            }
        }

        private int getLivingNeighbors() {
            int count = 0;

            // Obtém as coordenadas atuais da célula
            int[] coordinates = getCoordinates();
            int x = coordinates[0];
            int y = coordinates[1];

            // Verifica se as coordenadas são válidas
            if (x == -1 || y == -1) {
                return count;
            }

            // Itera sobre as 8 células vizinhas
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    // Ignora a própria célula
                    if (i == x && j == y) {
                        continue;
                    }

                    // Cria o nome do agente vizinho
                    String neighborName = "CellAgent-" + i + "-" + j;

                    // Busca o agente vizinho no DF
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("CellAgent");
                    sd.setName(neighborName);
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] results = DFService.search(myAgent, template);
                        if (results.length > 0) {
                            // Envia uma mensagem para o agente vizinho perguntando se está vivo
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                            msg.addReceiver(results[0].getName());
                            msg.setOntology("isAliveQuery");
                            send(msg);

                            // Espera pela resposta
                            ACLMessage reply = blockingReceive();
                            if (reply != null && reply.getOntology().equals("isAliveResponse")) {
                                boolean isNeighborAlive = Boolean.parseBoolean(reply.getContent());
                                if (isNeighborAlive) {
                                    count++;
                                }
                            }
                        }
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }
                }
            }
            return count;
        }
    }

    private class CheckCurrentState extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("isAliveQuery")) {
                ACLMessage reply = msg.createReply();
                reply.setOntology("isAliveResponse");
                reply.setContent(Boolean.toString(isAlive()));
                send(reply);
            }
        }
    }
}