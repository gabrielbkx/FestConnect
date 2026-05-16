package com.gabriel.party.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    // ─── Cidades: [cidade, estado, lat, lon] ───────────────────────────────────
    private static final Object[][] CIDADES = {
        {"São Paulo",       "SP", -23.5505, -46.6333},
        {"Rio de Janeiro",  "RJ", -22.9068, -43.1729},
        {"Belo Horizonte",  "MG", -19.9167, -43.9345},
        {"Curitiba",        "PR", -25.4284, -49.2733},
        {"Porto Alegre",    "RS", -30.0346, -51.2177},
        {"Brasília",        "DF", -15.7942, -47.8825},
        {"Salvador",        "BA", -12.9714, -38.5014},
        {"Recife",          "PE",  -8.0476, -34.8770},
        {"Fortaleza",       "CE",  -3.7172, -38.5434},
        {"Campinas",        "SP", -22.9056, -47.0608},
    };

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM tb_categorias", Integer.class);
            if (count != null && count > 0) {
                log.info("DataSeeder: banco já populado, pulando.");
                return;
            }
        } catch (Exception e) {
            log.warn("DataSeeder: tabelas ainda não prontas — {}", e.getMessage());
            return;
        }

        log.info("DataSeeder: iniciando seed de dados...");
        String hash = new BCryptPasswordEncoder().encode("123456");
        UUID[] cats = seedCategorias();
        seedPrestadores(cats, hash);
        seedClientes(hash);
        log.info("DataSeeder: seed concluído com sucesso!");
    }

    // ─── Categorias ────────────────────────────────────────────────────────────
    private UUID[] seedCategorias() {
        String[][] data = {
            {"DJ",                  "Disc Jóqueis profissionais para festas e eventos"},
            {"Buffet",              "Serviços de buffet e gastronomia para eventos"},
            {"Fotografia",          "Fotógrafos profissionais para registrar momentos especiais"},
            {"Decoração",           "Decoração temática e personalizada para eventos"},
            {"Espaço para Eventos", "Salões, sítios e espaços para celebrações"},
            {"Segurança",           "Serviços de segurança e controle de acesso"},
            {"Cerimonialista",      "Planejamento e coordenação profissional de eventos"},
            {"Filmagem",            "Videografia e filmagem profissional"},
            {"Iluminação e Som",    "Equipamentos e técnicos de iluminação e sonorização"},
            {"Animação Infantil",   "Recreadores e animadores para festas infantis"},
        };
        UUID[] ids = new UUID[data.length];
        for (int i = 0; i < data.length; i++) {
            ids[i] = UUID.randomUUID();
            jdbc.update("INSERT INTO tb_categorias (id_categoria, nome, descricao, ativo) VALUES (?,?,?,true)",
                    ids[i], data[i][0], data[i][1]);
        }
        log.info("DataSeeder: {} categorias inseridas.", ids.length);
        return ids;
    }

    // ─── Prestadores ──────────────────────────────────────────────────────────
    // [nome, cnpj, catIdx, cidadeIdx, descricao]
    private static final Object[][] PROVIDERS = {
        // ── DJs (cat 0) ──
        {"DJ Master Silva",          "11.111.111/0001-01", 0, 0, "DJ com 12 anos de experiência em casamentos, formaturas e eventos corporativos. Repertório eclético do sertanejo ao eletrônico."},
        {"DJ Pedro Beats",           "22.222.222/0001-02", 0, 0, "Especialista em música eletrônica, funk e pop. Equipamentos de última geração e iluminação inclusa."},
        {"DJ Carlos Groove",         "33.333.333/0001-03", 0, 1, "Residente de casas noturnas do Rio com repertório internacional e nacional. Faz eventos privados sob consulta."},
        {"DJ Renata Sound",          "44.444.444/0001-04", 0, 1, "Primeira DJ mulher da sua região, referência em festas temáticas dos anos 80 e 90."},
        {"DJ Marcos Festas",         "55.555.555/0001-05", 0, 2, "Atende Belo Horizonte e região metropolitana. Casamentos, bailes de debutante e aniversários."},
        {"DJ Fabinho MG",            "66.666.666/0001-06", 0, 3, "DJ e produtor musical com 8 anos de experiência em Curitiba. Técnica e carisma no palco."},
        {"DJ André RS",              "77.777.777/0001-07", 0, 4, "Especialista em casamentos gaúchos com música ao vivo integrada. Porto Alegre e interior do RS."},
        {"DJ Bia Brasília",          "88.888.888/0001-08", 0, 5, "DJ feminina referência no DF. Experiência em eventos governamentais, corporativos e festas privadas."},

        // ── Buffets (cat 1) ──
        {"Buffet Silva & Cia",       "99.999.999/0001-09", 1, 0, "Buffet completo com mais de 20 anos de tradição em São Paulo. Gastronomia premium para eventos de 50 a 500 pessoas."},
        {"Sabores do Brasil",        "10.100.100/0001-10", 1, 0, "Culinária brasileira autêntica com opções regionais. Ideal para eventos que valorizam a cultura nacional."},
        {"Buffet Carioca",           "11.110.110/0001-11", 1, 1, "Frutos do mar, churrasco carioca e drinks tropicais. O melhor do Rio na sua festa."},
        {"Mesa Posta RJ",            "12.120.120/0001-12", 1, 1, "Buffet executivo e social no Rio de Janeiro. Pratos gourmet com apresentação impecável."},
        {"Bom Gosto BH",             "13.130.130/0001-13", 1, 2, "Buffet mineiro com comida caseira de qualidade. Especialidade em festas de família e confraternizações."},
        {"Festim Curitiba",          "14.140.140/0001-14", 1, 3, "Gastronomia europeia e fusion para eventos exclusivos em Curitiba. Equipe uniformizada."},
        {"Sabor Gaúcho",             "15.150.150/0001-15", 1, 4, "Churrasco gaúcho tradicional, massas e saladas. Eventos de 30 a 300 pessoas em Porto Alegre."},
        {"Buffet Tropical",          "16.160.160/0001-16", 1, 6, "Culinária baiana autêntica: moquecas, vatapá e carne de sol. Eventos em Salvador e região."},
        {"Nordestino Buffet",        "17.170.170/0001-17", 1, 7, "Carne de sol, baião de dois e sobremesas típicas do Nordeste. Atende Recife e Grande Recife."},
        {"Banquetes Premium SP",     "18.180.180/0001-18", 1, 9, "Buffet de alto padrão em Campinas. Cardápio personalizado, equipe treinada e montagem inclusa."},
        {"Arte Culinária",           "19.190.190/0001-19", 1, 8, "Buffet criativo com finger foods, estações gastronômicas e confeitaria artesanal. Fortaleza e CE."},

        // ── Fotografia (cat 2) ──
        {"Foto & Arte SP",           "20.200.200/0001-20", 2, 0, "Fotógrafo especialista em casamentos com estética documental e artística. Mais de 300 eventos registrados."},
        {"Click Perfeito",           "21.210.210/0001-21", 2, 0, "Fotografia de eventos com edição criativa e entrega rápida. Cobre toda a Grande São Paulo."},
        {"Momentos RJ",              "22.220.220/0001-22", 2, 1, "Fotografia de casamentos, debutantes e eventos sociais no Rio de Janeiro. Estilo fotojornalístico."},
        {"Lente Maravilhosa",        "23.230.230/0001-23", 2, 1, "Especialista em ensaios externos e coberturas de festa no Rio. Pós-processamento artístico."},
        {"Foto BH",                  "24.240.240/0001-24", 2, 2, "Fotografia documental de casamentos e eventos em Belo Horizonte. Preço justo e qualidade profissional."},
        {"Imagens Sul",              "25.250.250/0001-25", 2, 4, "Fotógrafo premiado em Porto Alegre. Especialidade em casamentos ao ar livre e noturno."},
        {"Click DF",                 "26.260.260/0001-26", 2, 5, "Cobertura fotográfica e videográfica para eventos em Brasília. Pacotes completos."},
        {"Foco Nordeste",            "27.270.270/0001-27", 2, 7, "Fotógrafo especialista em festas juninas, casamentos e eventos corporativos em Recife."},

        // ── Decoração (cat 3) ──
        {"Flores & Arte",            "28.280.280/0001-28", 3, 0, "Decoração floral e temática para casamentos e festas em São Paulo. Projetos personalizados do início ao fim."},
        {"Decora Fest SP",           "29.290.290/0001-29", 3, 0, "Decoração completa para eventos de todos os tamanhos. Balões, tules, mesas postas e cenografias."},
        {"Rio Décor",                "30.300.300/0001-30", 3, 1, "Decoração tropical e rústica para festas no Rio. Especialista em casamentos na praia e espaços abertos."},
        {"Arte e Festa BH",          "31.310.310/0001-31", 3, 2, "Decoração criativa em Belo Horizonte. Desde festas infantis a casamentos luxuosos."},
        {"Glamour Eventos",          "32.320.320/0001-32", 3, 3, "Decoração de luxo para eventos em Curitiba. Flores naturais, mobiliário exclusivo e iluminação cênica."},
        {"Decora Sul",               "33.330.330/0001-33", 3, 4, "Decoração temática e clássica para festas em Porto Alegre. Atende RS todo."},
        {"Cor e Alegria BA",         "34.340.340/0001-34", 3, 6, "Decoração colorida e festiva para eventos em Salvador. Especialidade em festas juninas e debutantes."},
        {"Nordeste Decor",           "35.350.350/0001-35", 3, 7, "Decoração rústica e tropicalista para eventos em Recife. Projetos sob medida."},

        // ── Espaços (cat 4) ──
        {"Espaço Vila Verde",        "36.360.360/0001-36", 4, 0, "Salão de festas com área verde integrada em São Paulo. Capacidade para até 300 convidados. Estacionamento gratuito."},
        {"Quinta das Flores SP",     "37.370.370/0001-37", 4, 0, "Espaço rural a 30km de SP com lagoa, área de churrasqueira e salão climatizado. Ideal para eventos campestres."},
        {"Salão Maracanã",           "38.380.380/0001-38", 4, 1, "Espaço nobre no Rio com vista para o Cristo. Ideal para casamentos e eventos corporativos de médio porte."},
        {"Casa de Festas Carioca",   "39.390.390/0001-39", 4, 1, "Espaço multifuncional no Rio com cozinha industrial, terraço e salão principal. Ideal para 50 a 200 pessoas."},
        {"Recanto Mineiro",          "40.400.400/0001-40", 4, 2, "Espaço campestre em Belo Horizonte com churrasqueira, piscina e área kids. Eventos de 80 a 250 pessoas."},
        {"Espaço Pinheiro",          "41.410.410/0001-41", 4, 3, "Salão de festas com jardim inglês em Curitiba. Capacidade para 150 pessoas, estacionamento e copa inclusa."},
        {"Recanto Gaúcho",           "42.420.420/0001-42", 4, 4, "Espaço campestre em Porto Alegre ideal para eventos rurais, bodas de ouro e confraternizações."},

        // ── Segurança (cat 5) ──
        {"SecureEvent SP",           "43.430.430/0001-43", 5, 0, "Empresa de segurança para eventos em São Paulo. Profissionais treinados, uniformizados e documentados."},
        {"Vigilância RJ",            "44.440.440/0001-44", 5, 1, "Segurança privada para eventos no Rio de Janeiro. Controle de acesso, câmeras e rádio comunicação."},
        {"Guard Sul",                "45.450.450/0001-45", 5, 4, "Segurança profissional para eventos em Porto Alegre. Equipe masculina e feminina disponível."},
        {"DF Segurança",             "46.460.460/0001-46", 5, 5, "Serviços de segurança para eventos institucionais e privados em Brasília. Certificados e experientes."},
        {"Nordeste Guard",           "47.470.470/0001-47", 5, 7, "Empresa de segurança em Recife. Cobertura para shows, festas e eventos corporativos."},

        // ── Cerimonialistas (cat 6) ──
        {"Casa dos Sonhos SP",       "48.480.480/0001-48", 6, 0, "Cerimonialista especialista em casamentos e debutantes em São Paulo. Atendimento personalizado do início ao fim."},
        {"Eventos Perfeitos RJ",     "49.490.490/0001-49", 6, 1, "Assessoria de eventos sociais e corporativos no Rio. Mais de 200 eventos coordenados com excelência."},
        {"BH Cerimônias",            "50.500.500/0001-50", 6, 2, "Cerimonialista em Belo Horizonte. Planejamento completo de casamentos, aniversários e eventos empresariais."},
        {"Curitiba Cerimonial",      "51.510.510/0001-51", 6, 3, "Coordenação de eventos elegantes em Curitiba. Casamentos, formaturas e confraternizações."},
        {"Festas Inesquecíveis",     "52.520.520/0001-52", 6, 6, "Cerimonialista em Salvador. Eventos baianos com toda a alegria e organização profissional."},
        {"Nordeste Cerimonial",      "53.530.530/0001-53", 6, 7, "Planejamento de casamentos e formaturas em Recife. Referência no Nordeste."},
        {"Celebra Fortaleza",        "54.540.540/0001-54", 6, 8, "Coordenação de eventos em Fortaleza. Especialidade em casamentos à beira-mar e festas temáticas."},

        // ── Filmagem (cat 7) ──
        {"FilmeFest SP",             "55.550.550/0001-55", 7, 0, "Produção de vídeo para eventos em São Paulo. Drone, câmeras profissionais e edição cinematográfica."},
        {"Cine Eventos SP",          "56.560.560/0001-56", 7, 0, "Filmagem de casamentos e debutantes com estética de cinema. Trailer de casamento em 24h."},
        {"Carioca Films",            "57.570.570/0001-57", 7, 1, "Vídeo produção para eventos no Rio. Equipe de 3 câmeras, drone e edição completa."},
        {"Sul Vídeo",                "58.580.580/0001-58", 7, 4, "Filmagem profissional para eventos em Porto Alegre. Pacotes para casamentos, colações e festas."},
        {"DF Produtora",             "59.590.590/0001-59", 7, 5, "Produção audiovisual para eventos em Brasília. Transmissão ao vivo e gravação 4K."},
        {"Nordeste Vídeo",           "60.600.600/0001-60", 7, 7, "Filmagem de eventos em Recife. Edição rápida com entrega em 7 dias úteis."},
        {"Fortaleza Filmes",         "61.610.610/0001-61", 7, 8, "Vídeo para eventos em Fortaleza. Especialidade em festas de praia e casamentos ao pôr do sol."},

        // ── Iluminação e Som (cat 8) ──
        {"Light & Sound SP",         "62.620.620/0001-62", 8, 0, "Iluminação cênica e sonorização profissional para eventos em São Paulo. Técnicos experientes."},
        {"Neon Festas SP",           "63.630.630/0001-63", 8, 0, "Iluminação de efeito com LEDs, canhões de luz e fog machine. Locação e operação inclusa."},
        {"Rio Sound",                "64.640.640/0001-64", 8, 1, "Sonorização de alto padrão para eventos no Rio. Caixas de som JBL, mesas de som e microfones sem fio."},
        {"BH Áudio Visual",          "65.650.650/0001-65", 8, 2, "Locação de equipamentos de som e iluminação em Belo Horizonte. Telão e projetor disponíveis."},
        {"Curitiba Luz",             "66.660.660/0001-66", 8, 3, "Iluminação LED e sonorização para festas em Curitiba. Atende PR e SC."},
        {"Sul Som",                  "67.670.670/0001-67", 8, 4, "Sonorização e iluminação para eventos em Porto Alegre. Técnicos certificados e pontualidade garantida."},
        {"Tropical Som BA",          "68.680.680/0001-68", 8, 6, "Som e iluminação para eventos em Salvador. Especialidade em axé, pagode e música ao vivo."},
        {"Nordeste Light",           "69.690.690/0001-69", 8, 7, "Iluminação e sonorização para eventos em Recife e Fortaleza. Equipamentos modernos."},

        // ── Animação Infantil (cat 9) ──
        {"Turma da Alegria SP",      "70.700.700/0001-70", 9, 0, "Animação infantil em São Paulo com personagens, gincanas e recreação. Equipe certificada em primeiros socorros."},
        {"Palhaço Bonitão",          "71.710.710/0001-71", 9, 0, "Mágicas, malabares e animação para crianças de 2 a 12 anos. Atende toda a Grande SP."},
        {"Festinha RJ",              "72.720.720/0001-72", 9, 1, "Animação infantil no Rio com personagens exclusivos, pintura facial e brincadeiras temáticas."},
        {"Mini Eventos BH",          "73.730.730/0001-73", 9, 2, "Recreação e animação infantil em Belo Horizonte. Pacotes personalizados para festas de 1 a 10 anos."},
        {"Alegria Sul",              "74.740.740/0001-74", 9, 4, "Animação infantil em Porto Alegre. Personagens, mágicas, pinturas e muito mais para crianças."},
        {"Kids Party DF",            "75.750.750/0001-75", 9, 5, "Animação infantil em Brasília. Temas: Super-heróis, Frozen, Circo, Safari e mais."},
        {"Carnaval Kids BA",         "76.760.760/0001-76", 9, 6, "Animação com frevo, capoeira e alegria baiana para festas infantis em Salvador."},
    };

    // Foto de perfil dos prestadores por categoria
    private static final String[] FOTOS_PRESTADOR = {
        "https://picsum.photos/seed/dj-profile/300/300",
        "https://picsum.photos/seed/buffet-profile/300/300",
        "https://picsum.photos/seed/photo-profile/300/300",
        "https://picsum.photos/seed/decor-profile/300/300",
        "https://picsum.photos/seed/venue-profile/300/300",
        "https://picsum.photos/seed/security-profile/300/300",
        "https://picsum.photos/seed/ceremony-profile/300/300",
        "https://picsum.photos/seed/film-profile/300/300",
        "https://picsum.photos/seed/sound-profile/300/300",
        "https://picsum.photos/seed/kids-profile/300/300",
    };

    private void seedPrestadores(UUID[] cats, String senhaHash) {
        int count = 0;
        for (int i = 0; i < PROVIDERS.length; i++) {
            Object[] p = PROVIDERS[i];
            String nome      = (String) p[0];
            String doc       = (String) p[1];
            int catIdx       = (int) p[2];
            int cidadeIdx    = (int) p[3];
            String descricao = (String) p[4];

            String[] cidade = (String[]) new Object[]{
                (String) CIDADES[cidadeIdx][0],
                (String) CIDADES[cidadeIdx][1],
            };
            double lat = (double) CIDADES[cidadeIdx][2] + (Math.random() * 0.05 - 0.025);
            double lon = (double) CIDADES[cidadeIdx][3] + (Math.random() * 0.05 - 0.025);

            // Usuário
            UUID userId = UUID.randomUUID();
            String email = "prestador" + (i + 1) + "@festconnect.com";
            jdbc.update("""
                INSERT INTO tb_usuarios (id_usuario, email, senha, role, ativo, data_criacao)
                VALUES (?, ?, ?, 'ROLE_PRESTADOR', true, ?)
            """, userId, email, senhaHash, LocalDateTime.now());

            // Prestador
            UUID prestadorId = UUID.randomUUID();
            String fotoUrl = FOTOS_PRESTADOR[catIdx] + "?i=" + i;
            jdbc.update("""
                INSERT INTO tb_prestador
                  (id, nome_completo, cnpj_ou_cpf, descricao, whatsapp, foto_perfil_url,
                   cidade, estado, latitude, longitude, ativo, categoria_principal_id, usuario_id)
                VALUES (?,?,?,?,?,?,?,?,?,?,true,?,?)
            """,
                prestadorId, nome, doc, descricao,
                "11" + String.format("%09d", i + 1),
                fotoUrl,
                CIDADES[cidadeIdx][0], CIDADES[cidadeIdx][1], lat, lon,
                cats[catIdx], userId
            );

            // Itens do catálogo
            seedItens(prestadorId, catIdx, cats[catIdx], i);
            count++;
        }
        log.info("DataSeeder: {} prestadores inseridos.", count);
    }

    // ─── Itens por categoria ──────────────────────────────────────────────────
    private void seedItens(UUID prestadorId, int catIdx, UUID categoriaId, int seed) {
        switch (catIdx) {
            case 0 -> { // DJ
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "DJ Set Básico – 4 horas",
                        "Set de 4 horas com equipamento de som profissional, playlist personalizada e interação com os convidados.",
                        900 + seed * 20);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/dj" + seed + "a/800/600", 1);
                insMidia(id1, "https://picsum.photos/seed/dj" + seed + "b/800/600", 2);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "DJ Set Premium – 6 horas + Iluminação",
                        "Set completo de 6 horas com kit de iluminação LED, fog machine e backup de equipamentos.",
                        1800 + seed * 30);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/dj" + seed + "c/800/600", 1);
            }
            case 1 -> { // Buffet
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Buffet Completo – por pessoa",
                        "Cardápio completo com entrada, prato principal, sobremesa e bebidas. Equipe de garçons inclusa. Preço por convidado.",
                        120 + seed * 5);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/buffet" + seed + "a/800/600", 1);
                insMidia(id1, "https://picsum.photos/seed/buffet" + seed + "b/800/600", 2);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Buffet Finger Food e Drinks",
                        "Finger foods variados, canapés e coquetel de bebidas para eventos de 2 a 4 horas. Ideal para confraternizações.",
                        70 + seed * 3);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/fingerfood" + seed + "/800/600", 1);

                UUID id3 = insItem(prestadorId, categoriaId, "PRODUTO",
                        "Mesa de Frios e Charcutaria",
                        "Monte sua mesa de frios com queijos especiais, frios nobres, frutas e acompanhamentos. Montagem inclusa.",
                        650 + seed * 10);
                insProduto(id3);
                insMidia(id3, "https://picsum.photos/seed/frios" + seed + "/800/600", 1);
            }
            case 2 -> { // Fotografia
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Ensaio Fotográfico – 2 horas",
                        "Ensaio externo ou em estúdio com 2 horas de sessão. Entrega de 80 fotos editadas em alta resolução.",
                        500 + seed * 15);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/photo" + seed + "a/800/600", 1);
                insMidia(id1, "https://picsum.photos/seed/photo" + seed + "b/800/600", 2);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Cobertura Fotográfica Completa",
                        "Cobertura do dia todo (cerimônia + recepção). Entrega de álbum digital com 300+ fotos e 1 álbum impresso 30x30.",
                        2200 + seed * 50);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/wedding" + seed + "/800/600", 1);
            }
            case 3 -> { // Decoração
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Decoração Temática Completa",
                        "Projeto completo de decoração com flores, balões, tules e mobiliário temático. Montagem e desmontagem inclusa.",
                        1800 + seed * 40);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/decor" + seed + "a/800/600", 1);
                insMidia(id1, "https://picsum.photos/seed/decor" + seed + "b/800/600", 2);

                UUID id2 = insItem(prestadorId, categoriaId, "PRODUTO",
                        "Kit Mesa Posta – 10 lugares",
                        "Locação de mesa posta completa: toalha, sousplat, louças, talheres e taças para 10 convidados.",
                        380 + seed * 8);
                insProduto(id2);
                insMidia(id2, "https://picsum.photos/seed/mesa" + seed + "/800/600", 1);

                UUID id3 = insItem(prestadorId, categoriaId, "PRODUTO",
                        "Arco de Balões Personalizados",
                        "Arco de balões na cor da sua festa com até 3 cores. Inclui entrega e montagem no local do evento.",
                        250 + seed * 5);
                insProduto(id3);
                insMidia(id3, "https://picsum.photos/seed/balloon" + seed + "/800/600", 1);
            }
            case 4 -> { // Espaço para Eventos
                UUID id1 = UUID.randomUUID();
                jdbc.update("""
                    INSERT INTO tb_item_catalogo (id, titulo, descricao, preco_base, tipo, ativo, prestador_id, categoria_id)
                    VALUES (?,?,?,?,'LOCAL',true,?,?)
                """,
                    id1,
                    "Salão Principal – até " + (100 + seed * 5) + " pessoas",
                    "Salão climatizado com piso de madeira, banheiros, copa equipada, estacionamento gratuito e área de lazer externa. Aluguel por turno.",
                    3500 + seed * 100,
                    prestadorId, categoriaId
                );
                jdbc.update("""
                    INSERT INTO tb_local (id, capacidade_maxima, metragem, permite_som, tem_estacionamento, tipo_espaco)
                    VALUES (?, ?, ?, true, true, 'FECHADO')
                """, id1, 100 + seed * 5, 200 + seed * 20.0);
                insMidia(id1, "https://picsum.photos/seed/venue" + seed + "a/800/600", 1);
                insMidia(id1, "https://picsum.photos/seed/venue" + seed + "b/800/600", 2);

                UUID id2 = UUID.randomUUID();
                jdbc.update("""
                    INSERT INTO tb_item_catalogo (id, titulo, descricao, preco_base, tipo, ativo, prestador_id, categoria_id)
                    VALUES (?,?,?,?,'LOCAL',true,?,?)
                """,
                    id2,
                    "Área Gourmet e Jardim",
                    "Espaço ao ar livre com churrasqueira, jardim iluminado, deck de madeira e capacidade para eventos informais.",
                    1800 + seed * 50,
                    prestadorId, categoriaId
                );
                jdbc.update("""
                    INSERT INTO tb_local (id, capacidade_maxima, metragem, permite_som, tem_estacionamento, tipo_espaco)
                    VALUES (?, ?, ?, true, true, 'ABERTO')
                """, id2, 60 + seed * 3, 150 + seed * 10.0);
                insMidia(id2, "https://picsum.photos/seed/garden" + seed + "/800/600", 1);
            }
            case 5 -> { // Segurança
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Segurança Evento – 1 Profissional 4h",
                        "Um agente de segurança treinado e uniformizado para eventos de até 4 horas. Inclui controle de acesso.",
                        350 + seed * 10);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/security" + seed + "/800/600", 1);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Segurança Completa – Equipe 8h",
                        "Equipe de 2 agentes por 8 horas com rádio comunicação, controle de acesso e ronda periódica.",
                        900 + seed * 20);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/guard" + seed + "/800/600", 1);
            }
            case 6 -> { // Cerimonialista
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Assessoria Parcial de Evento",
                        "Acompanhamento nos 30 dias que antecedem o evento + presença no dia. Coordenação de fornecedores e cronograma.",
                        900 + seed * 25);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/ceremony" + seed + "a/800/600", 1);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Cerimonial Premium – Planejamento Completo",
                        "Planejamento completo do evento do início ao fim. Gestão de fornecedores, lista de convidados, dia D e pós-evento.",
                        2800 + seed * 60);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/ceremony" + seed + "b/800/600", 1);
                insMidia(id2, "https://picsum.photos/seed/wedding" + (seed + 5) + "/800/600", 2);
            }
            case 7 -> { // Filmagem
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Filmagem de Evento – 4 horas",
                        "Filmagem com câmera profissional 4K, edição completa e entrega de vídeo editado em até 30 dias.",
                        900 + seed * 20);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/film" + seed + "a/800/600", 1);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Filmagem Completa + Drone + Trailer",
                        "Cobertura completa com drone, múltiplos ângulos, trilha personalizada e trailer de 3 minutos em 24h.",
                        2500 + seed * 50);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/film" + seed + "b/800/600", 1);
                insMidia(id2, "https://picsum.photos/seed/drone" + seed + "/800/600", 2);
            }
            case 8 -> { // Iluminação e Som
                UUID id1 = insItem(prestadorId, categoriaId, "PRODUTO",
                        "Kit Iluminação LED para Festa",
                        "Locação de kit com canhões LED, par lights coloridos e controlador. Inclui montagem e operação.",
                        600 + seed * 15);
                insProduto(id1);
                insMidia(id1, "https://picsum.photos/seed/light" + seed + "a/800/600", 1);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Sonorização + Iluminação Completa",
                        "Pacote completo: caixas de som ativas JBL, mesa de som digital, microfones sem fio + kit iluminação com técnico.",
                        1400 + seed * 30);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/sound" + seed + "/800/600", 1);
                insMidia(id2, "https://picsum.photos/seed/light" + seed + "b/800/600", 2);
            }
            case 9 -> { // Animação Infantil
                UUID id1 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Animação Infantil – 2 horas",
                        "Animador(a) com fantasia, brincadeiras, pintura facial e distribuição de brindes. Para crianças de 2 a 10 anos.",
                        380 + seed * 8);
                insServico(id1);
                insMidia(id1, "https://picsum.photos/seed/kids" + seed + "a/800/600", 1);

                UUID id2 = insItem(prestadorId, categoriaId, "SERVICO",
                        "Recreação Completa – 4 horas + Personagem",
                        "Equipe de 2 animadores, personagem temático, cama elástica (se espaço permitir) e show de mágica.",
                        850 + seed * 15);
                insServico(id2);
                insMidia(id2, "https://picsum.photos/seed/kids" + seed + "b/800/600", 1);
                insMidia(id2, "https://picsum.photos/seed/party" + seed + "/800/600", 2);
            }
        }
    }

    // ─── Clientes ─────────────────────────────────────────────────────────────
    private static final String[][] CLIENTES = {
        {"Ana Beatriz Santos",   "SP", "0"}, {"Carlos Eduardo Lima",  "RJ", "1"},
        {"Fernanda Oliveira",    "MG", "2"}, {"Ricardo Mendes",       "PR", "3"},
        {"Juliana Costa",       "RS", "4"}, {"Thiago Almeida",       "DF", "5"},
        {"Mariana Ferreira",    "BA", "6"}, {"Lucas Rodrigues",      "PE", "7"},
        {"Camila Pereira",      "CE", "8"}, {"Rafael Souza",         "SP", "0"},
        {"Aline Nascimento",    "RJ", "1"}, {"Bruno Torres",         "MG", "2"},
        {"Patrícia Gomes",      "PR", "3"}, {"Gustavo Araújo",       "RS", "4"},
        {"Isabella Cardoso",    "SP", "9"},
    };

    private void seedClientes(String senhaHash) {
        for (int i = 0; i < CLIENTES.length; i++) {
            String nome      = CLIENTES[i][0];
            int cidadeIdx    = Integer.parseInt(CLIENTES[i][2]);

            UUID userId = UUID.randomUUID();
            String email = "cliente" + (i + 1) + "@festconnect.com";
            jdbc.update("""
                INSERT INTO tb_usuarios (id_usuario, email, senha, role, ativo, data_criacao)
                VALUES (?, ?, ?, 'ROLE_CLIENTE', true, ?)
            """, userId, email, senhaHash, LocalDateTime.now());

            UUID clienteId = UUID.randomUUID();
            jdbc.update("""
                INSERT INTO tb_cliente (id, nome, whatsapp, cidade, estado, ativo, usuario_id)
                VALUES (?,?,?,?,?,true,?)
            """,
                clienteId, nome,
                "21" + String.format("%09d", i + 1),
                CIDADES[cidadeIdx][0], CIDADES[cidadeIdx][1],
                userId
            );
        }
        log.info("DataSeeder: {} clientes inseridos.", CLIENTES.length);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private UUID insItem(UUID prestadorId, UUID categoriaId, String tipo, String titulo, String descricao, double preco) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO tb_item_catalogo (id, titulo, descricao, preco_base, tipo, ativo, prestador_id, categoria_id)
            VALUES (?,?,?,?,?,true,?,?)
        """, id, titulo, descricao, preco, tipo, prestadorId, categoriaId);
        return id;
    }

    private void insServico(UUID id) {
        jdbc.update("INSERT INTO tb_servico (id) VALUES (?)", id);
    }

    private void insProduto(UUID id) {
        jdbc.update("INSERT INTO tb_produto (id) VALUES (?)", id);
    }

    private void insMidia(UUID itemId, String url, int ordem) {
        jdbc.update("""
            INSERT INTO tb_midia (id, midia_url, tipo_midia, ordem_exibicao, item_catalogo_id)
            VALUES (?,?,?,?,?)
        """, UUID.randomUUID(), url, "FOTO", ordem, itemId);
    }
}
