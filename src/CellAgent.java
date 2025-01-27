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
                String content = msg.getContent();
                if (content != null && !content.isEmpty()) {
                    isAlive = Boolean.parseBoolean(content);
                }
                // Remove as mortas do DF
                if (!isAlive) {
                    deregisterFromDF();
                }
            }
        }
    }

    private class VerifyNeighbor extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getOntology() != null && msg.getOntology().equals("verifyIsAlive")) {
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
            }
        }

        private int getLivingNeighbors() {
            int count = 0;
        
            // Obtém as coordenadas atuais da célula a partir do nome do agente
            String[] coordinates = getLocalName().split("-");
            int x = Integer.parseInt(coordinates[1]);
            int y = Integer.parseInt(coordinates[2]);
        
            // Itera sobre as 8 células vizinhas
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    // Ignora a própria célula
                    if (i == x && j == y) {
                        continue;
                    }
        
                    // Cria o nome do agente vizinho
                    String neighborName = "CellAgent-" + i + "-" + j;
        
                    // Consulta o DF para verificar se o vizinho está registrado (ou seja, está vivo)
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("CellAgent");
                    sd.setName(neighborName);
                    template.addServices(sd);
        
                    try {
                        DFAgentDescription[] result = DFService.search(this.getAgent(), template);
                        if (result.length > 0) {
                            count++; // Se o vizinho está registrado no DF, está vivo
                        }
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }
                }
            }
        
            return count;
        }
    }
}
