package com.gabriel.party.services.integracoes.aws;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;


@Service
public class ArmazenamentoService {

    private final S3Client s3Client;

    private final String nomeBucket;

    // Endereço de onde o navegador lê as mídias (ex.: http://localhost:8333/festconnect-midias em dev,
    // https://<bucket>.s3.amazonaws.com na AWS). Antes era fixo no código, preso à AWS.
    private final String urlPublica;

    public ArmazenamentoService(S3Client s3Client,
                                @Value("${storage.s3.bucket}") String nomeBucket,
                                @Value("${storage.url-publica}") String urlPublica) {
        this.s3Client = s3Client;
        this.nomeBucket = nomeBucket;
        this.urlPublica = urlPublica.endsWith("/") ? urlPublica.substring(0, urlPublica.length() - 1) : urlPublica;
    }

    @WithSpan("s3.salvarMidia")
    public String salvarMidias(@SpanAttribute("arquivo.contentType") MultipartFile arquivo) {

        // Precisamos saber o tipo de arquivo se é uma imagem ou video
        String tipoArquivo = arquivo.getContentType();

        if (tipoArquivo == null) {
            throw new AppException(ErrorCode.FORMATO_INVALIDO, "Tipo de arquivo desconhecido");
        }

        // Nome único (UUID) para dois arquivos "foto-perfil.jpg" não se sobrescreverem. O nome original
        // do usuário não entra na URL: espaço, acento, "#" ou "?" quebrariam o link da imagem.
        String nomeArquivoCompleto;

        if (tipoArquivo.startsWith("image/")) {
            nomeArquivoCompleto = UUID.randomUUID() + ".jpg"; // toda imagem é convertida para JPG
            salvarImagem(arquivo, nomeArquivoCompleto);
        } else if (tipoArquivo.startsWith("video/")) {
            nomeArquivoCompleto = UUID.randomUUID() + extensaoDoVideo(arquivo.getOriginalFilename());
            salvarVideo(arquivo, nomeArquivoCompleto);
        } else {
            throw new AppException(ErrorCode.FORMATO_INVALIDO, "Formato não suportado: " + tipoArquivo);
        }

        return urlPublica + "/" + nomeArquivoCompleto;
    }

    // Mantém a extensão do vídeo (.mp4, .webm...) só se ela for "limpa"; senão, fica sem extensão.
    private static String extensaoDoVideo(String nomeOriginal) {
        if (nomeOriginal == null) {
            return "";
        }
        int ponto = nomeOriginal.lastIndexOf('.');
        if (ponto < 0) {
            return "";
        }
        String extensao = nomeOriginal.substring(ponto + 1).toLowerCase();
        return extensao.matches("[a-z0-9]{1,5}") ? "." + extensao : "";
    }


    @WithSpan("s3.deletaMidia")
    public void deletaMidia(@SpanAttribute("arquivo.url") String url) {

        if (url == null || !url.contains("/")) {
            throw new AppException(ErrorCode.URL_INVALIDA, url);
        }

        int posicaoUltimaBarra = url.lastIndexOf("/");
        String nomeArquivo = url.substring(posicaoUltimaBarra + 1);

        if (nomeArquivo.isEmpty()) {
            throw new AppException(ErrorCode.URL_INVALIDA, url);
        }

        s3Client.deleteObject(builder -> builder.bucket(nomeBucket).key(nomeArquivo));

    }

    private void salvarImagem(MultipartFile arquivo, String nomeArquivoCompleto) {
        ByteArrayOutputStream bytesSaida = new ByteArrayOutputStream();

        try {
            Thumbnails.of(arquivo.getInputStream())
                    .size(800, 800)
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(bytesSaida);
        } catch (RuntimeException | IOException e) {
            throw new AppException(ErrorCode.ERRO_AO_PROCESAR_IMAGEM, "Erro ao processar a imagem: "
                    + arquivo.getOriginalFilename());
        }

        byte[] imagemProcessada = bytesSaida.toByteArray();

        PutObjectRequest s3Request = PutObjectRequest.builder()
                .bucket(nomeBucket)
                .key(nomeArquivoCompleto)
                .contentType("image/jpeg") // Avisa ao S3 que o resultado final é sempre um JPG
                .build();

        s3Client.putObject(s3Request, RequestBody.fromBytes(imagemProcessada));
    }

    private void salvarVideo(MultipartFile arquivo, String nomeArquivoCompleto) {
        try {
            byte[] bytesVideo = arquivo.getBytes();

            PutObjectRequest s3Request = PutObjectRequest.builder()
                    .bucket(nomeBucket)
                    .key(nomeArquivoCompleto)
                    .contentType(arquivo.getContentType()) // Mantém o formato original do vídeo (ex: video/mp4)
                    .build();

            s3Client.putObject(s3Request, RequestBody.fromBytes(bytesVideo));
        } catch (IOException e) {
            throw new AppException(ErrorCode.ERRO_AO_PROCESSAR_VIDEO, "Erro ao processar o vídeo: "
                    + arquivo.getOriginalFilename());
        }
    }
}
