package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenDeLogin;
import com.gabriel.party.config.infra.security.TokensRevogados;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessaoService {

    private final UsuarioRepository usuarioRepository;
    private final TokensRevogados tokensRevogados;

    public SessaoService(UsuarioRepository usuarioRepository, TokensRevogados tokensRevogados) {
        this.usuarioRepository = usuarioRepository;
        this.tokensRevogados = tokensRevogados;
    }

    /**
     * Sair só deste dispositivo: cancela o token desta requisição até a hora em que ele venceria.
     * Os outros aparelhos continuam logados.
     */
    public void encerrarEsteDispositivo(TokenDeLogin tokenDeLogin) {
        tokensRevogados.revogar(tokenDeLogin);
    }

    /**
     * Sair de todos os dispositivos: todo token emitido até agora deixa de valer, inclusive o
     * que fez esta chamada. Busca o usuário de novo para alterar a entidade gerenciada pela
     * transação (o principal do Spring Security foi carregado fora dela).
     */
    @Transactional
    public void encerrarTodasAsSessoes(Usuario usuarioLogado) {
        Usuario usuario = usuarioRepository.findById(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL, usuarioLogado.getEmail()));
        usuario.encerrarTodasAsSessoes();
    }
}
