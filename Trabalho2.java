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

// Rotulacao de componentes conexas em imagem binaria.
// Le e processa imagens PGM (P2) e identifica regioes conectadas.
// Feito por: Matheus Silva Pontes & Lucas Monteiro de Carvalho

// Execucao:
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

    // Converte a imagem em binaria usando um limiar.
    // Pixel abaixo do limiar vira 0 (fundo); acima vira 255 (objeto).
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

    // Union-Find: guarda quais rotulos pertencem ao mesmo objeto.
    private static class ConjuntoDeRotulos {
        private final List<Integer> pai = new ArrayList<>();

        // Cria um novo rotulo e o considera como conjunto isolado.
        int novoRotulo() {
            int rotulo = pai.size() + 1;
            pai.add(rotulo);
            return rotulo;
        }

        // Retorna o representante do conjunto do rotulo.
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

        // Une dois rotulos em um mesmo conjunto.
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

    // Percorre a imagem e atribui rotulos aos pixels do objeto.
    // A ideia principal: olhar os vizinhos ja processados e decidir:
    // criar novo rotulo, reaproveitar um existente ou unir rotulos equivalentes.
    public static int[][] rotular(int[][] imagemBinaria, int valorObjeto, int conectividade) {
        if (conectividade != 4 && conectividade != 8) {
            throw new IllegalArgumentException("Conectividade deve ser 4 ou 8.");
        }

        int altura = imagemBinaria.length;
        int largura = imagemBinaria[0].length;

        // Matriz de saida: 0 significa fundo; valores > 0 sao rotulos.
        int[][] rotulos = new int[altura][largura];
        ConjuntoDeRotulos rotulosEquivalentes = new ConjuntoDeRotulos();

        // Vizinhos analisados na varredura.
        int[][] vizinhanca4 = { {-1, 0}, {0, -1} };
        int[][] vizinhanca8 = { {-1, -1}, {-1, 0}, {-1, 1}, {0, -1} };
        int[][] vizinhanca = (conectividade == 4) ? vizinhanca4 : vizinhanca8;

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {

                // Ignora fundo.
                if (imagemBinaria[y][x] != valorObjeto) {
                    continue;
                }

                // Guarda o menor rótulo encontrado entre os vizinhos processados.
                int menorRotuloVizinho = Integer.MAX_VALUE;
                for (int[] deslocamento : vizinhanca) {
                    int vy = y + deslocamento[0];
                    int vx = x + deslocamento[1];

                    if (vy < 0 || vy >= altura || vx < 0 || vx >= largura) {
                        continue;
                    }

                    int rotuloVizinho = rotulos[vy][vx];
                    if (rotuloVizinho == 0) {
                        continue;
                    }

                    if (rotuloVizinho < menorRotuloVizinho) {
                        menorRotuloVizinho = rotuloVizinho;
                    }

                    // Se ja existia rotulo no pixel atual e o vizinho tambem tinha outro, une os conjuntos.
                    if (rotulos[y][x] != 0) {
                        rotulosEquivalentes.unir(rotulos[y][x], rotuloVizinho);
                    }

                    // Atribui o menor rotulo encontrado por enquanto.
                    rotulos[y][x] = menorRotuloVizinho;
                }

                // Se nenhum vizinho tinha rotulo, cria um novo.
                if (rotulos[y][x] == 0) {
                    rotulos[y][x] = rotulosEquivalentes.novoRotulo();
                }
            }
        }

        // Resolve todas as equivalencias e deixa cada componente com um representante unico.
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (rotulos[y][x] != 0) {
                    rotulos[y][x] = rotulosEquivalentes.encontrar(rotulos[y][x]);
                }
            }
        }

        return comprimirRotulos(rotulos);
    }

    // Reorganiza os rotulos finais para ficar em sequencia 1, 2, 3, ...
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

    // Guarda area e centro de cada componente rotulada.
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

    // Soma os pixels de cada rotulo para obter area e centroide.
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

    // Imprime uma matriz em formato tabular.
    public static void imprimir(int[][] matriz) {
        for (int[] linha : matriz) {
            for (int valor : linha) {
                System.out.printf("%4d", valor);
            }
            System.out.println();
        }
    }

    // Exibe rotulo, area e centroide de cada componente.
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