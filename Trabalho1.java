import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Trabalho1 {

    public static int[][] carregarImagem(String caminho) throws IOException {
        BufferedImage arquivo = ImageIO.read(new File(caminho));

        if (arquivo == null) {
            throw new IOException("Formato de imagem não suportado: " + caminho);
        }

        int altura = arquivo.getHeight();
        int largura = arquivo.getWidth();
        int[][] imagem = new int[altura][largura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int rgb = arquivo.getRGB(x, y);
                int vermelho = (rgb >> 16) & 0xFF;
                int verde = (rgb >> 8) & 0xFF;
                int azul = rgb & 0xFF;

                // Luminância do pixel: 0 representa preto e 255 representa branco.
                imagem[y][x] = (int) Math.round(
                        0.299 * vermelho + 0.587 * verde + 0.114 * azul);
            }
        }

        return imagem;
    }

    public static void salvarImagem(int[][] imagem, String caminho)
            throws IOException {
        int altura = imagem.length;
        int largura = imagem[0].length;
        BufferedImage arquivo = new BufferedImage(
                largura, altura, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int cinza = Math.max(0, Math.min(255, imagem[y][x]));
                int rgb = (cinza << 16) | (cinza << 8) | cinza;
                arquivo.setRGB(x, y, rgb);
            }
        }

        ImageIO.write(arquivo, "png", new File(caminho));
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

        int novaAltura = altura / 2;
        int novaLargura = largura / 2;

        int[][] resultado = new int[novaAltura][novaLargura];

        for (int y = 0; y < novaAltura; y++) {

            for (int x = 0; x < novaLargura; x++) {

                int origemY = y * 2;
                int origemX = x * 2;

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
        int novaAltura = altura * 2 - 1;
        int novaLargura = largura * 2 - 1;

        int[][] resultado = new int[novaAltura][novaLargura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {

                resultado[y * 2][x * 2] = imagem[y][x];
            }
        }
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura - 1; x++) {

                int esquerda = imagem[y][x];
                int direita = imagem[y][x + 1];

                resultado[y * 2][x * 2 + 1]
                        = (esquerda + direita) / 2;
            }
        }
        for (int y = 0; y < altura - 1; y++) {
            for (int x = 0; x < largura; x++) {

                int cima = imagem[y][x];
                int baixo = imagem[y + 1][x];

                resultado[y * 2 + 1][x * 2]
                        = (cima + baixo) / 2;
            }
        }
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
        for (int i = 0; i < imagem.length; i++) {
            for (int j = 0; j < imagem[i].length; j++) {
                System.out.print(imagem[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        try {
            File pastaProjeto = new File(".");
            File pastaOriginais = new File(pastaProjeto, "originais");
            File pastaAlteradas = new File(pastaProjeto, "alteradas");

            if (!pastaOriginais.isDirectory() || !pastaAlteradas.isDirectory()) {
            throw new IOException(
                        "As pastas originais e alteradas devem existir dentro de PI_Trabalhos.");
            }

            if (args.length == 0) {
            throw new IllegalArgumentException(
                        "Informe o nome da imagem que esta em originais.");
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

            salvarImagem(vizinhoAmpliada,
                    new File(pastaAlteradas, "vizinho-ampliada.png").getPath());
            salvarImagem(vizinhoReduzida,
                    new File(pastaAlteradas, "vizinho-reduzida.png").getPath());
            salvarImagem(bilinearAmpliada,
                    new File(pastaAlteradas, "bilinear-ampliada.png").getPath());
            salvarImagem(bilinearReduzida,
                    new File(pastaAlteradas, "bilinear-reduzida.png").getPath());

            System.out.println("Imagem carregada: "
                    + imagem[0].length + "x" + imagem.length + " pixels");
            System.out.println("Alteradas em: " + pastaAlteradas.getPath());
        } catch (IOException | IllegalArgumentException erro) {
            System.err.println("Erro: " + erro.getMessage());
            System.err.println("Informe uma imagem JPG ou PNG. Exemplo:");
            System.err.println("java Trabalho1 nome-da-imagem.jpg");
        }
    }
}
