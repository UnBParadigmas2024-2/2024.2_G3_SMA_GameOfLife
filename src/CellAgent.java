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
        System.out.println(getLocalName() + ": inicializando CellAgent...");

        // Recupera as coordenadas do agente
        Object[] args = getArguments();
        if (args == null || args.length < 2) {
            System.err.println("Erro: Coordenadas não foram passadas para " + getLocalName());
            // Finaliza o agente
            doDelete(); 
            return;
        }
        int x = Integer.parseInt(args[0].toString());
        int y = Integer.parseInt(args[1].toString());

        registerOnDF(x, y);

        System.out.println(getLocalName() + " iniciado nas coordenadas (" + x + ", " + y + "). Estado inicial: " + isAlive);
        addBehaviour(new VerifyNeighbor());
    }

    private void registerOnDF(int x, int y) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("CellAgent");
        sd.setName(x + "," + y);
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
                    	// Coordenadas do agente atual
                        String[] position = getLocalName().split("-");
                        int myX = Integer.parseInt(position[1].trim());
                        int myY = Integer.parseInt(position[2].trim());
                        registerOnDF(myX, myY);
                    } else {
                        deregisterFromDF();
                    }
                }

                // Envia o estado atual ao ControllerAgent
                ACLMessage response = msg.createReply();
                response.setOntology("verifyIsAliveResponse");
                response.setContent(Boolean.toString(isAlive));
                send(response);
                // System.out.println(getLocalName() + " próximo estado: " + isAlive); // Debuga o estado enviado
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
