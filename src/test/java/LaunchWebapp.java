import org.testatoo.container.ContainerConfiguration;
import org.testatoo.container.TestatooContainer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class LaunchWebapp {
    public static void main(String[] args) {
        ContainerConfiguration.create()
            .webappRoot("src/test/webapp")
            .port(80)
            .buildContainer(TestatooContainer.JETTY)
            .start();
    }
}
