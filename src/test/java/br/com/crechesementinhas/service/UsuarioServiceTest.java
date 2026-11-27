package br.com.crechesementinhas.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Testes das validações do UsuarioService (sem banco de dados).
 */
class UsuarioServiceTest {

    // ===================== NOME =====================

    @Test
    void nomeVazioDeveSerRecusado() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarNome("   "));
    }

    @Test
    void nomeNuloDeveSerRecusado() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarNome(null));
    }

    @Test
    void nomeComMaisDe100CaracteresDeveSerRecusado() {
        String nomeLongo = "a".repeat(101);
        assertThrows(UsuarioException.class, () -> UsuarioService.validarNome(nomeLongo));
    }

    @Test
    void nomeValidoDeveVirSemEspacosNasPontas() throws UsuarioException {
        assertEquals("Maria Silva", UsuarioService.validarNome("  Maria Silva  "));
    }

    // ===================== LOGIN =====================

    @Test
    void loginDeveSerConvertidoParaMinusculas() throws UsuarioException {
        assertEquals("maria.silva", UsuarioService.validarLogin(" Maria.Silva "));
    }

    @Test
    void loginComEspacoNoMeioDeveSerRecusado() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarLogin("maria silva"));
    }

    @Test
    void loginComMenosDe3CaracteresDeveSerRecusado() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarLogin("ab"));
    }

    @Test
    void loginComAcentoDeveSerRecusado() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarLogin("joão"));
    }

    @Test
    void loginComNumeroPontoESublinhadoDeveSerAceito() {
        assertDoesNotThrow(() -> UsuarioService.validarLogin("secretaria_01.manha"));
    }

    // ===================== SENHA =====================

    @Test
    void senhaComMenosDe8CaracteresDeveSerRecusada() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarSenha("abc123"));
    }

    @Test
    void senhaSemNumeroDeveSerRecusada() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarSenha("somenteletras"));
    }

    @Test
    void senhaSemLetraDeveSerRecusada() {
        assertThrows(UsuarioException.class, () -> UsuarioService.validarSenha("12345678"));
    }

    @Test
    void senhaProvisoriaDoAdminDeveSerAceita() {
        assertDoesNotThrow(() -> UsuarioService.validarSenha("Sementinhas@2026"));
    }
}