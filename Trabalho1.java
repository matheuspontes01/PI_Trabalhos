public class Trabalho1 {

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

        int[][] imagem = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        System.out.println("Imagem original:");
        imprimir(imagem);

        int[][] ampliada = vizinhoMaisProximo(imagem, 6, 6);

        System.out.println("\nImagem ampliada:");
        imprimir(ampliada);

        int[][] reduzida = vizinhoMaisProximo(imagem, 2, 2);

        System.out.println("\nImagem reduzida:");
        imprimir(reduzida);

        System.out.println("\nBilinear - Ampliação:");
        int[][] bilinearAmpliada =
                ampliarBilinear(imagem);

        imprimir(bilinearAmpliada);


        System.out.println("\nBilinear - Redução:");
        int[][] bilinearReduzida =
                reduzirBilinear(imagem);
        imprimir(bilinearReduzida);

    }
}
