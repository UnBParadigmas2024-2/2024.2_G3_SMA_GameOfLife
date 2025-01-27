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
        // addBehaviour(new VerifyNeighbor());
        addBehaviour(new SetInitialState());
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

    private void deregisterFromDF() {
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + ": removido do DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private class SetInitialState extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("inicialState")) {
                System.out.println("Recebido : " + msg.getContent());

                String content = msg.getContent();
                if (content != null && !content.isEmpty()) {
                    isAlive = Boolean.parseBoolean(content);
                }
                // Atualiza registro no DF se estiver viva
                if (isAlive) {
                    registerOnDF();
                } else {
                    deregisterFromDF();
                }
            }
        }
    }

    private class VerifyNeighbor extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && "verifyIsAlive".equals(msg.getOntology())) {
                int livingNeighbors = getLivingNeighbors(); // Consulta agentes vivos ao redor
                boolean previousState = isAlive;

                // Aplicação das regras do Jogo da Vida
                if (isAlive && (livingNeighbors < 2 || livingNeighbors > 3)) {
                    isAlive = false; // Morre por isolamento ou superpopulação
                } else if (!isAlive && livingNeighbors == 3) {
                    isAlive = true; // Torna-se viva por nascimento
                } // Se está viva com 2 ou 3 vizinhos, permanece viva.

                // Atualiza o registro no DF conforme o estado
                if (previousState != isAlive) {
                    if (isAlive) {
                        registerOnDF();
                    } else {
                        deregisterFromDF();
                    }
                }

                // Envia o estado atual ao ControllerAgent
                ACLMessage response = msg.createReply();
                response.setOntology("verifyIsAliveResponse");
                response.setContent(Boolean.toString(isAlive));
                send(response);
            } else {
                block();
            }
        }

        private int getLivingNeighbors() {
            int count = 0;

            try {

                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("CellAgent");
                template.addServices(sd);

                // Recuperar todos os agentes célula
                DFAgentDescription[] result = DFService.search(myAgent, template);

                for (DFAgentDescription agentDesc : result) {
                    ServiceDescription service = (ServiceDescription) agentDesc.getAllServices().next();
                    String[] position = service.getName().split(",");

                    int x = Integer.parseInt(position[0].trim());
                    int y = Integer.parseInt(position[1].trim());

                    // Coordenadas do agente atual
                    String[] myPosition = getLocalName().split("-");
                    int myX = Integer.parseInt(myPosition[1].trim());
                    int myY = Integer.parseInt(myPosition[2].trim());

                    // Verificar se o agente está ao redor (8 vizinhos)
                    if (Math.abs(x - myX) <= 1 && Math.abs(y - myY) <= 1 && !(x == myX && y == myY)) {
                        // Enviar mensagem para verificar se o agente está vivo
                        ACLMessage query = new ACLMessage(ACLMessage.REQUEST);
                        query.addReceiver(agentDesc.getName());
                        query.setOntology("verifyIsAlive");
                        send(query);

                        // Receber resposta
                        ACLMessage reply = blockingReceive();
                        if (reply != null && "verifyIsAliveResponse".equals(reply.getOntology())) {
                            if (Boolean.parseBoolean(reply.getContent())) {
                                count++;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return count;
        }
    }
}
