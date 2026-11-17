package br.com.crechesementinhas.creche_sementinhas.security;

import br.com.crechesementinhas.creche_sementinhas.model.Usuario;
import br.com.crechesementinhas.creche_sementinhas.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Busca o usuário no banco para o Spring Security conferir o login.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Procura o usuário pelo login digitado.
     *
     * @param login o login informado na tela de entrada
     * @return os dados que o Spring Security usa para conferir a senha
     * @throws UsernameNotFoundException se o login não existir
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado: " + login));

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .disabled(!usuario.isAtivo())
                .roles("USUARIO")
                .build();
    }
}