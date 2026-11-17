package br.com.crechesementinhas.creche_sementinhas.repository;

import br.com.crechesementinhas.creche_sementinhas.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso à tabela "usuario".
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca um usuário pelo login.
     *
     * @param login o login digitado na tela de entrada
     * @return o usuário, ou vazio se o login não existir
     */
    Optional<Usuario> findByLogin(String login);
}