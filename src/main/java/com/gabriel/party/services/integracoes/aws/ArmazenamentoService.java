package com.gabriel.party.services.integracoes.aws;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
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

    private S3Client s3Client;
    private static final String URL_PREFIXO = "https://";
    private static final String URL_SUFIXO = ".s3.amazonaws.com/";

     public ArmazenamentoService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket-name}")
    private String nomeBucket;

    public String salvarMidias(MultipartFile arquivo) {

        // Precisamos saber o tipo de arquivo se é uma imagem ou video
        String tipoArquivo = arquivo.getContentType();

        if (tipoArquivo == null) {
            throw new AppException(ErrorCode.FORMATO_INVALIDO, "Tipo de arquivo desconhecido");
        }

        //se dois prestadores diferentes enviarem uma imagem chamada foto-perfil.jpg,
        // o S3 vai sobrescrever a primeira foto com a segunda, pois os nomes são idênticos.
        //Para isso, vamos gerar um nome único para cada arquivo usando UUID,
        // garantindo que mesmo que os arquivos tenham o mesmo nome original, eles serão armazenados com nomes únicos no S3.
        String nomeArquivoCompleto = UUID.randomUUID() + "-" + arquivo.getOriginalFilename();



        if (tipoArquivo.startsWith("image/")) {
            salvarImagem(arquivo, nomeArquivoCompleto);
        } else if (tipoArquivo.startsWith("video/")) {
            salvarVideo(arquivo, nomeArquivoCompleto);
        } else {
            throw new AppException(ErrorCode.FORMATO_INVALIDO, "Formato não suportado: " + tipoArquivo);
        }

        return URL_PREFIXO + nomeBucket + URL_SUFIXO + nomeArquivoCompleto;
    }


    public void deletaMidia(String url) {

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
