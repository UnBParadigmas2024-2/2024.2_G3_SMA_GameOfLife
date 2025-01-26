import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class SetupAgent {
    public static void main(String[] args) {
        // Inicializando o runtime do JADE
        Runtime runtime = Runtime.instance();

        // Configurando o perfil padrão
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");

        // Habilitando a interface gráfica do JADE
        profile.setParameter(Profile.GUI, "true");

        // Criando o container principal
        ContainerController container = runtime.createMainContainer(profile);

        try {
            // Criando agentes
            AgentController game = container.createNewAgent("game", "src.GameUIAgent", null);

            // Iniciando agentes
            game.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
