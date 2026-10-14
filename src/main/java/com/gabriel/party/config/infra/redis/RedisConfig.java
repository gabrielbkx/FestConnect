package com.gabriel.party.config.infra.redis;

import io.lettuce.core.ClientOptions;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    /**
     * Com o Redis fora do ar, o Lettuce guarda os comandos numa fila esperando a conexão voltar,
     * e cada um só desiste no timeout (2 s). Um login faz até três chamadas, então esperaria
     * até 6 s antes de cair no plano B. Rejeitando na hora, o plano B entra sem espera.
     */
    @Bean
    public LettuceClientOptionsBuilderCustomizer rejeitarComandosSemConexao() {
        return builder -> builder.disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
    }
}
