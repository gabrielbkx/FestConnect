package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.dtos.autenticacao.DadosDeValidacaoDeCodigoRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRedefinicaoDeSenhaDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.model.autenticacao.CodigoRecuperacao;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.autenticacao.CodigoRecuperacaoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.cliente.ClienteService;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.prestador.PrestadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PrestadorRepository prestadorRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    UsuarioMapper mapper;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    PrestadorService prestadorService;
    @Autowired
    ClienteService clienteService;
    @Autowired
    CodigoRecuperacaoRepository codigoRecuperacaoRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    EmailVerificacaoService emailVerificacaoService;

    static final int MAX_TENTATIVAS_PIN = 5;
    static final int VALIDADE_CODIGO_MINUTOS = 15;
    static final int INTERVALO_MINIMO_REENVIO_SEGUNDOS = 60;

    // SecureRandom: o PIN é um segredo, e o java.util.Random é previsível.
    // Uma instância só, reaproveitada (criar uma por chamada é caro e desnecessário).
    private final SecureRandom geradorSeguro = new SecureRandom();


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // CORREÇÃO 1: Usando a exceção correta do Spring Security
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL, "email"));
    }

    @Transactional
    public CadastroResponseDTO cadastrarCliente(CadastroClienteDTO dto) {
        validarEmailExistente(dto.email());

        Usuario usuario = mapper.toUsuarioCliente(dto);
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole(Role.ROLE_CLIENTE);

        usuario = usuarioRepository.save(usuario);

        var cliente = clienteService.criarPerfilCliente(dto, usuario, null);
        String tokenJwt = tokenService.gerarToken(usuario, cliente.getId(), dto.nomeCompleto());
        emailVerificacaoService.enviarVerificacaoEmail(usuario);

        return new CadastroResponseDTO(
                usuario.getId(),
                dto.nomeCompleto(),
                usuario.getEmail(),
                tokenJwt);
    }

    @Transactional
    public CadastroResponseDTO cadastrarPrestador(CadastroPrestadorDTO dto) {
        // Removi o MultipartFile da assinatura
        validarEmailExistente(dto.email());
        validarCpfOuCnpjExistente(dto.cnpjOuCpf());

        Usuario usuario = mapper.toUsuarioPrestador(dto);
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole(Role.ROLE_PRESTADOR);

        prestadorService.criarPerfilPrestador(dto, usuario, null);
        emailVerificacaoService.enviarVerificacaoEmail(usuario);

        var prestador = prestadorRepository.findByUsuarioId(usuario.getId()).orElse(null);
        UUID profileId = prestador != null ? prestador.getId() : null;
        String tokenJwt = tokenService.gerarToken(usuario, profileId, dto.nomeCompleto());

        return new CadastroResponseDTO(
                usuario.getId(),
                dto.nomeCompleto(),
                usuario.getEmail(),
                tokenJwt);
    }

    private void validarEmailExistente(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USUARIO_JA_EXISTE_POR_EMAIL, email);
        }
    }

    private void validarCpfOuCnpjExistente(String cpfOuCnpj) {
        boolean cnpjExiste = prestadorRepository.existsByCnpjOuCpf(cpfOuCnpj);
        if (cnpjExiste){
            throw new AppException(ErrorCode.JA_EXISTE_POR_CNPJ,
                    "Já existe um cadastro com esse CNPJ", cpfOuCnpj);
        }
    }

    @Transactional
    public void enviarCodigoRecuperacao(DadosRecuperacaoDTO dados) {
        var email = dados.email();
        usuarioRepository.findByEmail(email).ifPresent(user -> {
            LocalDateTime agora = LocalDateTime.now();

            // Reenvio muito rápido: ignora em silêncio. A resposta continua 200,
            // para não revelar se o e-mail existe nem se já há código ativo.
            boolean reenvioCedoDemais = codigoRecuperacaoRepository.findByUsuarioEmail(email)
                    .map(anterior -> anterior.getDataCriacao().isAfter(agora.minusSeconds(INTERVALO_MINIMO_REENVIO_SEGUNDOS)))
                    .orElse(false);
            if (reenvioCedoDemais) {
                return;
            }

            // Um código por usuário: o novo invalida o anterior
            codigoRecuperacaoRepository.deleteByUsuario(user);

            String pin = String.format("%06d", geradorSeguro.nextInt(1_000_000));

            CodigoRecuperacao codigoRecuperacao = new CodigoRecuperacao();
            codigoRecuperacao.setCodigoPin(pin);
            codigoRecuperacao.setDataCriacao(agora);
            codigoRecuperacao.setDataExpiracao(agora.plusMinutes(VALIDADE_CODIGO_MINUTOS));
            codigoRecuperacao.setTentativas(0);
            codigoRecuperacao.setUsuario(user);
            codigoRecuperacaoRepository.save(codigoRecuperacao);

            String assunto = "Recuperação de senha - Resenha";
            String corpoEmail = String.format(
                    "Olá, %s!\n\n" +
                            "Recebemos uma solicitação para redefinir a sua senha.\n" +
                            "Use o código abaixo para prosseguir:\n\n" +
                            "CÓDIGO: %s\n\n" +
                            "Este código é válido por 15 minutos. Se não foi você quem solicitou, " +
                            "apenas ignore este e-mail por segurança.\n\n" +
                            "Atenciosamente,\nEquipe de Suporte.", user.getEmail(), pin
            );

            // Após o commit e fora da thread da requisição: o e-mail só sai se o código
            // foi gravado, e o tempo de resposta não denuncia se o e-mail existe.
            emailService.enviarAposCommit(user.getEmail(), assunto, corpoEmail);
        });
    }

    /**
     * noRollbackFor: o PIN errado lança AppException, mas o incremento das tentativas
     * (ou a remoção do código) precisa ser gravado mesmo assim. Sem isso, o rollback
     * desfaria o contador e o limite de tentativas não funcionaria.
     */
    @Transactional(noRollbackFor = AppException.class)
    public String validarCodigoRecuperacao(DadosDeValidacaoDeCodigoRecuperacaoDTO dados) {
        // Mesma resposta para "não existe código" e "PIN errado": quem ataca não
        // descobre se o e-mail tem um código ativo
        var codigo = codigoRecuperacaoRepository.findByUsuarioEmail(dados.email())
                .orElseThrow(() -> new AppException(ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO));

        if (codigo.getDataExpiracao().isBefore(LocalDateTime.now())) {
            codigoRecuperacaoRepository.delete(codigo);
            throw new AppException(ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO);
        }

        if (!pinConfere(codigo.getCodigoPin(), dados.codigoPin())) {
            codigo.setTentativas(codigo.getTentativas() + 1);
            if (codigo.getTentativas() >= MAX_TENTATIVAS_PIN) {
                codigoRecuperacaoRepository.delete(codigo);
            } else {
                codigoRecuperacaoRepository.save(codigo);
            }
            throw new AppException(ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO);
        }

        codigoRecuperacaoRepository.delete(codigo);
        return tokenService.gerarTokenParaRecuperacaoDeSenha(codigo.getUsuario());
    }

    /**
     * Comparação em tempo constante: o tempo de resposta não depende de quantos
     * dígitos iniciais estão certos (proteção contra timing attack).
     */
    private static boolean pinConfere(String pinCorreto, String pinInformado) {
        if (pinInformado == null) {
            return false;
        }
        return MessageDigest.isEqual(
                pinCorreto.getBytes(StandardCharsets.UTF_8),
                pinInformado.getBytes(StandardCharsets.UTF_8));
    }

    public void verificarEmail(String token) {
        emailVerificacaoService.verificarToken(token);
    }

    public void reenviarVerificacaoEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL, email));
        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) return;
        emailVerificacaoService.enviarVerificacaoEmail(usuario);
    }

    @Transactional
    public void redefinirSenha(DadosRedefinicaoDeSenhaDTO dados){
        String email = tokenService.validarTokenDeRecuperacaoDeSenha(dados.token());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL,
                        "Usuário não encontrado para o email do token", email));

        usuario.setSenha(encoder.encode(dados.novaSenha()));
        // Quem trocou a senha pode estar expulsando um invasor: os tokens antigos não podem continuar valendo
        usuario.encerrarTodasAsSessoes();
        usuarioRepository.save(usuario);
    }
}