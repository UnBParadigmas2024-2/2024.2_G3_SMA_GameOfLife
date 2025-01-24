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
    private boolean isAlive = false; // Todas as células começam mortas

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " inicializado.");
        registerOnDF(); // Cadastra no DF

        // Adiciona os comportamentos da célula
        addBehaviour(new SetInitialState());
        addBehaviour(new VerifyNeighbor());
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
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void deregisterFromDF() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class SetInitialState extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && "setInitialState".equals(msg.getOntology())) {
                isAlive = Boolean.parseBoolean(msg.getContent());
                if (!isAlive) {
                    deregisterFromDF();
                }
                System.out.println(getLocalName() + " estado inicial: " + isAlive);
            } else {
                block();
            }
        }
    }

    private class VerifyNeighbor extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && "verifyIsAlive".equals(msg.getOntology())) {
                int livingNeighbors = getLivingNeighbors();
                boolean previousState = isAlive;
                isAlive = (livingNeighbors == 2 || livingNeighbors == 3);

                boolean previousState = isAlive;
                if (!isAlive && livingNeighbors == 3) {
                    isAlive = true; // Nascimento
                } else if (isAlive && livingNeighbors < 2) {
                    isAlive = false; // Morte por isolamento
                } else if (isAlive && livingNeighbors > 3) {
                    isAlive = false; // Morte por superpopulação
                } else if (isAlive && (livingNeighbors == 2 || livingNeighbors == 3)) {
                    isAlive = true; // A célula permanece viva
                }

                if (previousState != isAlive) {
                    if (isAlive) {
                        registerOnDF();
                    } else {
                        deregisterFromDF();
                    }
                }

                ACLMessage response = msg.createReply();
                response.setOntology("verifyIsAliveResponse");
                response.setContent(Boolean.toString(isAlive));
                send(response);
            } else {
                block();
            }
        }

        private int getLivingNeighbors() {
            return (int) (Math.random() * 4); // Exemplo de contagem simulada de vizinhos vivos
        }
    }
}
