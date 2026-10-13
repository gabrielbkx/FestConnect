package com.gabriel.party.services.integracoes.aws;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ArmazenamentoServiceTest {

    private static final String BUCKET = "festconnect-midias";
    private static final String URL_PUBLICA = "http://localhost:8333/festconnect-midias";
    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Mock private S3Client s3Client;

    private ArmazenamentoService service;

    @BeforeEach
    void setUp() {
        service = new ArmazenamentoService(s3Client, BUCKET, URL_PUBLICA);
    }

    private static byte[] pngDeVerdade() throws Exception {
        var imagem = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        var saida = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", saida);
        return saida.toByteArray();
    }

    private PutObjectRequest requisicaoEnviada() {
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        return captor.getValue();
    }

    @Test
    @DisplayName("imagem vira JPG, vai para o bucket configurado e a URL usa o endereço público configurado")
    void imagem_salvaComoJpgEDevolveUrlPublica() throws Exception {
        var arquivo = new MockMultipartFile("arquivo", "minha foto #1.png", "image/png", pngDeVerdade());

        String url = service.salvarMidias(arquivo);

        PutObjectRequest enviado = requisicaoEnviada();
        assertThat(enviado.bucket()).isEqualTo(BUCKET);
        assertThat(enviado.key()).matches(UUID_REGEX + "\\.jpg");
        assertThat(enviado.contentType()).isEqualTo("image/jpeg");
        assertThat(url).isEqualTo(URL_PUBLICA + "/" + enviado.key());
    }

    @Test
    @DisplayName("o nome original do arquivo não entra na URL (espaço, # e acento quebrariam o link)")
    void nomeOriginal_naoVaiParaUrl() throws Exception {
        var arquivo = new MockMultipartFile("arquivo", "minha foto #1 ação.png", "image/png", pngDeVerdade());

        String url = service.salvarMidias(arquivo);

        assertThat(url).doesNotContain("minha", " ", "#", "ação");
    }

    @Test
    @DisplayName("vídeo mantém o tipo e uma extensão limpa")
    void video_mantemTipoEExtensao() {
        var arquivo = new MockMultipartFile("arquivo", "Festa Final.MP4", "video/mp4", new byte[]{1, 2, 3});

        String url = service.salvarMidias(arquivo);

        PutObjectRequest enviado = requisicaoEnviada();
        assertThat(enviado.key()).matches(UUID_REGEX + "\\.mp4");
        assertThat(enviado.contentType()).isEqualTo("video/mp4");
        assertThat(url).endsWith(enviado.key());
    }

    @Test
    @DisplayName("vídeo com extensão estranha fica sem extensão")
    void video_extensaoEstranha_semExtensao() {
        var arquivo = new MockMultipartFile("arquivo", "video.mp4?x=1", "video/mp4", new byte[]{1});

        service.salvarMidias(arquivo);

        assertThat(requisicaoEnviada().key()).matches(UUID_REGEX);
    }

    @Test
    @DisplayName("formato que não é imagem nem vídeo é recusado sem enviar nada")
    void formatoNaoSuportado_recusado() {
        var arquivo = new MockMultipartFile("arquivo", "documento.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> service.salvarMidias(arquivo))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORMATO_INVALIDO);
        verifyNoInteractions(s3Client);
    }

    @Test
    @DisplayName("apagar usa o último trecho da URL como chave no bucket configurado")
    @SuppressWarnings("unchecked")
    void deletaMidia_usaChaveDaUrl() {
        service.deletaMidia(URL_PUBLICA + "/abc-123.jpg");

        ArgumentCaptor<Consumer<DeleteObjectRequest.Builder>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(s3Client).deleteObject(captor.capture());
        var builder = DeleteObjectRequest.builder();
        captor.getValue().accept(builder);
        DeleteObjectRequest pedido = builder.build();

        assertThat(pedido.bucket()).isEqualTo(BUCKET);
        assertThat(pedido.key()).isEqualTo("abc-123.jpg");
    }

    @Test
    @DisplayName("barra no fim da URL pública configurada não gera barra dupla")
    void urlPublicaComBarraNoFim_semBarraDupla() throws Exception {
        service = new ArmazenamentoService(s3Client, BUCKET, URL_PUBLICA + "/");

        String url = service.salvarMidias(new MockMultipartFile("arquivo", "a.png", "image/png", pngDeVerdade()));

        assertThat(url).startsWith(URL_PUBLICA + "/").doesNotContain("midias//");
    }
}
