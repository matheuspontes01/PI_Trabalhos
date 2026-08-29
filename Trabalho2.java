import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Algoritmo de rotulacao (labelling) de componentes conexas em imagens
// binarias, com leitura de imagens PGM (P2).
// Feito pelos alunos: Matheus Silva Pontes & Lucas Monteiro de Carvalho

// Execute no terminal os seguintes comandos:
// javac Trabalho2.java
// java Trabalho2 teste.pgm

public class Trabalho2 {

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

    // ---------------------------------------------------------------
    // Binarizacao: Para todo pixel da imagem f
    //              Se f(x,y) < limiar entao f'(x,y) = 0
    //              senao f'(x,y) = 255
    // ---------------------------------------------------------------

    public static int[][] binarizar(int[][] imagem, int limiar) {
        int altura = imagem.length;
        int largura = imagem[0].length;

        int[][] resultado = new int[altura][largura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                resultado[y][x] = (imagem[y][x] < limiar) ? 0 : 255;
            }
        }
        return resultado;
    }

    // ---------------------------------------------------------------
    // Estrutura de conjuntos disjuntos (union-find), usada para
    // controlar quais rotulos sao equivalentes durante a varredura,
    // conforme o passo 3 do algoritmo: "Troca-se cada label pelo seu
    // equivalente".
    // ---------------------------------------------------------------

    private static class ConjuntoDeRotulos {
        private final List<Integer> pai = new ArrayList<>();

        // Cria um novo rotulo, inicialmente equivalente a ele mesmo.
        int novoRotulo() {
            int rotulo = pai.size() + 1;
            pai.add(rotulo);
            return rotulo;
        }

        // Encontra o rotulo representante (raiz) de um rotulo, com
        // compressao de caminho.
        int encontrar(int rotulo) {
            int raiz = rotulo;
            while (pai.get(raiz - 1) != raiz) {
                raiz = pai.get(raiz - 1);
            }
            while (pai.get(rotulo - 1) != raiz) {
                int proximo = pai.get(rotulo - 1);
                pai.set(rotulo - 1, raiz);
                rotulo = proximo;
            }
            return raiz;
        }

        // Anota que dois rotulos sao equivalentes, unindo-os pelo
        // menor valor (menor rotulo "ganha").
        void unir(int rotuloA, int rotuloB) {
            int raizA = encontrar(rotuloA);
            int raizB = encontrar(rotuloB);
            if (raizA == raizB) {
                return;
            }
            if (raizA < raizB) {
                pai.set(raizB - 1, raizA);
            } else {
                pai.set(raizA - 1, raizB);
            }
        }
    }

    // ---------------------------------------------------------------
    // Algoritmo de rotulacao propriamente dito.
    //
    // Varredura pixel a pixel, da esquerda para a direita e de cima
    // para baixo. Para o pixel p em estudo:
    //   1- Se p nao pertence ao objeto (fundo), passa para o proximo.
    //   2- Se p pertence ao objeto, observam-se os vizinhos ja
    //      visitados (r = esquerda, s = acima, e tambem as diagonais
    //      superiores quando conectividade = 8):
    //      2.1- Se todos os vizinhos ja rotulados forem fundo,
    //           atribui-se um novo rotulo a p.
    //      2.2- Se exatamente um rotulo aparecer entre os vizinhos,
    //           esse rotulo e atribuido a p.
    //      2.3- Se mais de um rotulo distinto aparecer entre os
    //           vizinhos, atribui-se o menor deles a p e anota-se que
    //           os demais sao equivalentes a ele.
    //   3- Ao final da varredura, troca-se cada rotulo pelo seu
    //      equivalente (representante do conjunto).
    // ---------------------------------------------------------------

    public static int[][] rotular(int[][] imagemBinaria, int valorObjeto, int conectividade) {
        if (conectividade != 4 && conectividade != 8) {
            throw new IllegalArgumentException("Conectividade deve ser 4 ou 8.");
        }

        int altura = imagemBinaria.length;
        int largura = imagemBinaria[0].length;

        int[][] rotulos = new int[altura][largura];
        ConjuntoDeRotulos rotulosEquivalentes = new ConjuntoDeRotulos();

        // Deslocamentos (dy, dx) dos vizinhos ja visitados na varredura.
        // 4-conectividade: s (acima) e r (esquerda).
        // 8-conectividade: acrescenta as duas diagonais superiores.
        int[][] vizinhanca4 = { {-1, 0}, {0, -1} };
        int[][] vizinhanca8 = { {-1, -1}, {-1, 0}, {-1, 1}, {0, -1} };
        int[][] vizinhanca = (conectividade == 4) ? vizinhanca4 : vizinhanca8;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {

                // 1- Se p nao pertence ao objeto, move-se para o proximo pixel.
                if (imagemBinaria[y][x] != valorObjeto) {
                    continue;
                }

                // 2- Se p pertence ao objeto, analisam-se os vizinhos ja rotulados.
                int menorRotuloVizinho = Integer.MAX_VALUE;
                for (int[] deslocamento : vizinhanca) {
                    int vy = y + deslocamento[0];
                    int vx = x + deslocamento[1];

                    if (vy < 0 || vx < 0 || vx >= largura) {
                        continue;
                    }

                    int rotuloVizinho = rotulos[vy][vx];
                    if (rotuloVizinho == 0) {
                        continue;
                    }

                    if (rotuloVizinho < menorRotuloVizinho) {
                        menorRotuloVizinho = rotuloVizinho;
                    }
                    // 2.3- Se ha mais de um rotulo distinto entre os vizinhos,
                    // anota-se que eles sao equivalentes.
                    if (rotulos[y][x] != 0) {
                        rotulosEquivalentes.unir(rotulos[y][x], rotuloVizinho);
                    }
                    rotulos[y][x] = menorRotuloVizinho;
                }

                // 2.1- Nenhum vizinho rotulado: assinala-se um novo label.
                if (rotulos[y][x] == 0) {
                    rotulos[y][x] = rotulosEquivalentes.novoRotulo();
                }
                // 2.2/2.3 ja tratados no laco acima (rotulo minimo + equivalencias).
            }
        }

        // 3- Troca-se cada rotulo pelo seu equivalente (representante).
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (rotulos[y][x] != 0) {
                    rotulos[y][x] = rotulosEquivalentes.encontrar(rotulos[y][x]);
                }
            }
        }

        return comprimirRotulos(rotulos);
    }

    // Renumera os rotulos finais para uma sequencia 1, 2, 3, ... na
    // ordem em que aparecem na varredura, deixando o resultado mais
    // legivel (equivalente ao "Rotulo" da tabela mostrada em aula).
    private static int[][] comprimirRotulos(int[][] rotulos) {
        int altura = rotulos.length;
        int largura = rotulos[0].length;

        Map<Integer, Integer> mapaNovoRotulo = new LinkedHashMap<>();
        int[][] resultado = new int[altura][largura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int rotuloAntigo = rotulos[y][x];
                if (rotuloAntigo == 0) {
                    continue;
                }
                Integer rotuloNovo = mapaNovoRotulo.get(rotuloAntigo);
                if (rotuloNovo == null) {
                    rotuloNovo = mapaNovoRotulo.size() + 1;
                    mapaNovoRotulo.put(rotuloAntigo, rotuloNovo);
                }
                resultado[y][x] = rotuloNovo;
            }
        }
        return resultado;
    }

    // ---------------------------------------------------------------
    // Estatisticas por componente: rotulo, area (em pixels) e
    // centroide (X, Y), no mesmo espirito da tabela "Rotulo / Area /
    // X / Y" apresentada em aula.
    // ---------------------------------------------------------------

    private static class Componente {
        int rotulo;
        int area;
        double somaX;
        double somaY;

        double centroX() {
            return somaX / area;
        }

        double centroY() {
            return somaY / area;
        }
    }

    public static List<Componente> calcularEstatisticas(int[][] rotulos) {
        Map<Integer, Componente> componentes = new LinkedHashMap<>();

        for (int y = 0; y < rotulos.length; y++) {
            for (int x = 0; x < rotulos[0].length; x++) {
                int rotulo = rotulos[y][x];
                if (rotulo == 0) {
                    continue;
                }
                Componente componente = componentes.get(rotulo);
                if (componente == null) {
                    componente = new Componente();
                    componente.rotulo = rotulo;
                    componentes.put(rotulo, componente);
                }
                componente.area++;
                componente.somaX += x;
                componente.somaY += y;
            }
        }
        return new ArrayList<>(componentes.values());
    }

    // ---------------------------------------------------------------
    // Impressao
    // ---------------------------------------------------------------

    public static void imprimir(int[][] matriz) {
        for (int[] linha : matriz) {
            for (int valor : linha) {
                System.out.printf("%4d", valor);
            }
            System.out.println();
        }
    }

    public static void imprimirEstatisticas(List<Componente> componentes) {
        System.out.printf("%-8s%-8s%-8s%-8s%n", "Rotulo", "Area", "X", "Y");
        for (Componente componente : componentes) {
            System.out.printf("%-8d%-8d%-8.1f%-8.1f%n",
                    componente.rotulo,
                    componente.area,
                    componente.centroX(),
                    componente.centroY());
        }
    }

    // ---------------------------------------------------------------
    // main
    // ---------------------------------------------------------------

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

            // Considera-se que a imagem ja esta (ou sera) binarizada,
            // com objetos representados pelo valor 255.
            int[][] imagemBinaria = binarizar(imagem, 127);

            int[][] rotulos4 = rotular(imagemBinaria, 255, 4);
            int[][] rotulos8 = rotular(imagemBinaria, 255, 8);

            List<Componente> estatisticas4 = calcularEstatisticas(rotulos4);
            List<Componente> estatisticas8 = calcularEstatisticas(rotulos8);

            System.out.println("Imagem carregada: "
                    + imagem[0].length + "x" + imagem.length + " pixels");

            System.out.println("\nImagem binarizada:");
            imprimir(imagemBinaria);

            System.out.println("\nRotulacao com 4-conectividade:");
            imprimir(rotulos4);
            System.out.println("\nEstatisticas (4-conectividade):");
            imprimirEstatisticas(estatisticas4);
            System.out.println("Total de componentes 4-conectadas: " + estatisticas4.size());

            System.out.println("\nRotulacao com 8-conectividade:");
            imprimir(rotulos8);
            System.out.println("\nEstatisticas (8-conectividade):");
            imprimirEstatisticas(estatisticas8);
            System.out.println("Total de componentes 8-conectadas: " + estatisticas8.size());

        } catch (IOException | IllegalArgumentException erro) {
            System.err.println("Erro: " + erro.getMessage());
            System.err.println("Informe uma imagem PGM no formato P2. Exemplo:");
            System.err.println("java Trabalho2 nome-da-imagem.pgm");
        }
    }
}