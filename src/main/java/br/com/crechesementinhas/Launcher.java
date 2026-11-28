package br.com.crechesementinhas;

/**
 * Ponto de entrada do EXECUTÁVEL (.jar).
 *
 * Por que existe: quando a classe principal de um jar herda de
 * javafx.application.Application, o Java exige que o JavaFX esteja no
 * "module path" e recusa iniciar ("JavaFX runtime components are missing").
 * Esta classe NÃO herda de Application, então essa verificação não acontece,
 * e o JavaFX é carregado normalmente de dentro do jar.
 *
 * No desenvolvimento (mvn javafx:run), o App continua sendo usado direto.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}