import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// Algoritmos de vizinho mais próximo e interpolação bilinear (ampliação e redução) com imagens PGM (P2).
// Feitos pelos alunos: Matheus Silva Pontes & Lucas Monteiro de Carvalho

// Execute no terminal os seguintes comandos:
// javac Trabalho1.java
// java Trabalho1 teste.pgm

public class Trabalho1 {

    public static int[][] carregarImagem(String caminho) throws IOException {
        try (Scanner entrada = new Scanner(
                new BufferedInputStream(new FileInputStream(caminho)),
                StandardCharsets.US_ASCII)) {
            if (!"P2".equals(proximoToken(entrada))) {
                throw new IOException("O arquivo precisa estar no formato PGM P2.");
            }

            int largura = Integer.parseInt(proximoToken(entrada));
            int altura = Integer.parseInt(proximoToken(entrada));
            int valorMaximo = Integer.parseInt(proximoToken(entrada));

            if (largura < 1 || altura < 1 || valorMaximo < 1) {
                throw new IOException("Cabecalho PGM invalido.");
            }

            int[][] imagem = new int[altura][largura];
            for (int y = 0; y < altura; y++) {
                for (int x = 0; x < largura; x++) {
                    int pixel = Integer.parseInt(proximoToken(entrada));
                    imagem[y][x] = pixel * 255 / valorMaximo;
                }
            }
            return imagem;
        } catch (NumberFormatException erro) {
            throw new IOException("Valores invalidos no arquivo PGM.", erro);
        }
    }

    private static String proximoToken(Scanner entrada) throws IOException {
        while (entrada.hasNext()) {
            String token = entrada.next();
            if (token.startsWith("#")) {
                if (entrada.hasNextLine()) {
                    entrada.nextLine();
                }
                continue;
            }
            return token;
        }
        throw new IOException("Arquivo PGM incompleto.");
    }

    public static int[][] vizinhoMaisProximo(
            int[][] imagem,
            int novaAltura,
            int novaLargura) {

        int altura = imagem.length;
        int largura = imagem[0].length;

        int[][] resultado = new int[novaAltura][novaLargura];

        double escalaY = (double) altura / novaAltura;
        double escalaX = (double) largura / novaLargura;

        for (int y = 0; y < novaAltura; y++) {

            for (int x = 0; x < novaLargura; x++) {
                double yOrigem = (y + 0.5) * escalaY - 0.5;
                double xOrigem = (x + 0.5) * escalaX - 0.5;

                int origemY = (int) Math.round(yOrigem);
                int origemX = (int) Math.round(xOrigem);

                origemY = Math.max(0, Math.min(origemY, altura - 1));
                origemX = Math.max(0, Math.min(origemX, largura - 1));

                resultado[y][x] = imagem[origemY][origemX];
            }
        }
        return resultado;
    }


    public static int[][] reduzirBilinear(int[][] imagem) {
        int altura = imagem.length;
        int largura = imagem[0].length;

        // Redução da imagem pela metade
        int novaAltura = altura / 2;
        int novaLargura = largura / 2;

        int[][] resultado = new int[novaAltura][novaLargura];

        for (int y = 0; y < novaAltura; y++) {

            for (int x = 0; x < novaLargura; x++) {

                // Cada pixel de saída, seleciona bloco 2x2 da imagem original
                int origemY = y * 2;
                int origemX = x * 2;

                // Obtem os 4 pixels para calcular a média
                int p1 = imagem[origemY][origemX];
                int p2 = imagem[origemY][origemX + 1];
                int p3 = imagem[origemY + 1][origemX];
                int p4 = imagem[origemY + 1][origemX + 1];

                resultado[y][x] = (p1 + p2 + p3 + p4) / 4;
            }
        }

        return resultado;
    }

    public static int[][] ampliarBilinear(int[][] imagem) {
        int altura = imagem.length;
        int largura = imagem[0].length;

        // Dimensão da nova imagem
        // Inserção de pixel entre os pixels originais, sem duplicar a ultima coluna e ultima linha
        int novaAltura = altura * 2 - 1;
        int novaLargura = largura * 2 - 1;

        int[][] resultado = new int[novaAltura][novaLargura];

        // Preservação dos pixels originais nas posições pares na nova matriz
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {

                resultado[y * 2][x * 2] = imagem[y][x];
            }
        }
        // Interpolação horizontal: media entre dois pixels vizinhos na mesma linha
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura - 1; x++) {

                int esquerda = imagem[y][x];
                int direita = imagem[y][x + 1];

                resultado[y * 2][x * 2 + 1]
                        = (esquerda + direita) / 2;
            }
        }
        // Interpolação vertical: media entre dois pixels vizinhos na mesma coluna
        for (int y = 0; y < altura - 1; y++) {
            for (int x = 0; x < largura; x++) {

                int cima = imagem[y][x];
                int baixo = imagem[y + 1][x];

                resultado[y * 2 + 1][x * 2]
                        = (cima + baixo) / 2;
            }
        }
        // Interpolação diagonal: media entre quatro pixels vizinhos
        for (int y = 0; y < altura - 1; y++) {
            for (int x = 0; x < largura - 1; x++) {

                int p1 = imagem[y][x];
                int p2 = imagem[y][x + 1];
                int p3 = imagem[y + 1][x];
                int p4 = imagem[y + 1][x + 1];

                resultado[y * 2 + 1][x * 2 + 1]
                        = (p1 + p2 + p3 + p4) / 4;
            }
        }
        return resultado;
    }

    public static void imprimir(int[][] imagem) {
        for (int[] linha : imagem) {
            for (int pixel : linha) {
                System.out.printf("%4d", pixel);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        try {
            File pastaProjeto = new File(".");
            File pastaOriginais = new File(pastaProjeto, "testes");

            if (!pastaOriginais.isDirectory()) {
            throw new IOException(
                        "A pasta testes deve existir dentro de PI_Trabalhos.");
            }

            if (args.length == 0) {
            throw new IllegalArgumentException(
                        "Informe o nome da imagem que esta em testes.");
            }

            File entrada = new File(pastaOriginais, args[0]);
            int[][] imagem = carregarImagem(entrada.getPath());

            if (imagem.length < 2 || imagem[0].length < 2) {
                throw new IllegalArgumentException(
                        "A imagem precisa ter pelo menos 2x2 pixels.");
            }

            int[][] vizinhoAmpliada = vizinhoMaisProximo(
                    imagem, imagem.length * 2, imagem[0].length * 2);
            int[][] vizinhoReduzida = vizinhoMaisProximo(
                    imagem, imagem.length / 2, imagem[0].length / 2);
            int[][] bilinearAmpliada = ampliarBilinear(imagem);
            int[][] bilinearReduzida = reduzirBilinear(imagem);

            System.out.println("Imagem carregada: "
                    + imagem[0].length + "x" + imagem.length + " pixels");
                System.out.println("\nVizinho mais proximo - ampliacao:");
                imprimir(vizinhoAmpliada);
                System.out.println("\nVizinho mais proximo - reducao:");
                imprimir(vizinhoReduzida);
                System.out.println("\nInterpolacao bilinear - ampliacao:");
                imprimir(bilinearAmpliada);
                System.out.println("\nInterpolacao bilinear - reducao:");
                imprimir(bilinearReduzida);
        } catch (IOException | IllegalArgumentException erro) {
            System.err.println("Erro: " + erro.getMessage());
            System.err.println("Informe uma imagem PGM no formato P2. Exemplo:");
            System.err.println("java Trabalho1 nome-da-imagem.pgm");
        }
    }
}
