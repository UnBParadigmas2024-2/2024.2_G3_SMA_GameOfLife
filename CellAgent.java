import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour; 

public class CellAgent extends Agent {

	private static final long serialVersionUID = 1L;
	//TO-DO: Criar variável "isAlive" como um booleano, true se estiver vivo e false se estiver mortos
	// Essa variável começa como "false" pois todos os agentes começarão mortos
	
	protected void setup() {
		//TO-DO: Todas se cadastram no DF juntamente com sua coordenada (sd.addProperties)
		
    }
	
	private class SetInitialState extends CyclicBehaviour {
    	//TO-DO: Irá esperar a mensagem do ControllerAgent dizendo o estado inicial
		// de acordo com a mensagem, irá mudar a variável "isAlive"
		// se estiver morto, se descadastra do DF
        public void action() {
            
        }
    }
	
	private class VerifyNeighbor extends CyclicBehaviour {
    	//TO-DO: Irá esperar a mensagem do ControllerAgent pedindo para verificar se está vivo ou não
		// Irá consultar no DF quantos agentes vivos existem ao redor de sua coordenada
		// Se tiver 2 ou 3 vizinhos, ele fica vivo
		// Se não, ele morre.
		// Caso 1: Se estava vivo antes e continua vivo, não faz nada
		// Caso 2: Se estava vivo antes e agora morreu, descadastra no DF
		// Caso 3: Se estava morto antes e continua morto, não faz nada
		// Caso 4: Se estava morto antes e agora viveu, se cadastra no DF
		// Mandar mensagem para o ControllerAgent informando seu estado atual
		
		// Estrutura da mensagem
		// Tipo: ACL.INFORM
		// Ontology: "verifyIsAlive"
		// Content: "isAlive" (Irá como string e deve ser convertido como booleano, pode ser utilizado o ContentObject diretamente se for melhor)
		// Destinatário: ControllerAgent (Pegar no DF)
        public void action() {
            
        }
    }
    
}
