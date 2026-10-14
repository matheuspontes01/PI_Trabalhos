r"""
GUIA DE EXECUCAO - WINDOWS / POWERSHELL

1. Abra o PowerShell e entre na pasta do trabalho:
    cd "C:\Users\canal\OneDrive\Documentos\pi\PI_Trabalhos"

2. Instale as bibliotecas necessarias:
    python -m pip install -r requirements.txt

3. Verifique a sintaxe do programa:
    python -m py_compile Trabalho5.py

4. Execute a implementacao:
    python Trabalho5.py

O programa espera encontrar a imagem de entrada em:
    testes/images2.png

Os resultados serao salvos na pasta:
    results/

Entre eles estara comparacao_laplaciano.png, que organiza os quatro
resultados Laplacianos em uma imagem 2x2.

Observacao: Python e uma linguagem interpretada. O comando py_compile
verifica a sintaxe, mas a execucao e feita pelo comando python Trabalho5.py.
"""

from PIL import Image
import numpy as np


def carregar_imagem(caminho):
    # Abre a imagem, converte para tons de cinza e cria uma matriz de pixels.
    imagem = Image.open(caminho).convert("L")
    return np.array(imagem, dtype=np.float64)


def salvar_imagem(matriz, caminho):
    # Limita os pixels ao intervalo valido e salva a matriz como imagem.
    matriz = np.clip(matriz, 0, 255)
    matriz = matriz.astype(np.uint8)

    Image.fromarray(matriz).save(caminho)


def normalizar(imagem):
    # Ajusta os valores da imagem para o intervalo de 0 a 255.
    minimo = imagem.min()
    maximo = imagem.max()

    if maximo == minimo:
        return np.zeros_like(imagem)

    return ((imagem - minimo) / (maximo - minimo)) * 255


def aplicar_mascara(imagem, mascara):
    # Aplica uma mascara 3x3 em cada pixel e nos seus vizinhos.
    altura, largura = imagem.shape

    # Trata as bordas preenchendo os pixels externos com zeros.
    imagem_padding = np.pad(
        imagem,
        pad_width=1,
        mode="constant",
        constant_values=0
    )

    resultado = np.zeros_like(imagem)

    for y in range(altura):
        for x in range(largura):

            soma = 0

            for dy in range(3):
                for dx in range(3):

                    pixel = imagem_padding[y + dy, x + dx]
                    peso = mascara[dy, dx]

                    soma += pixel * peso

            resultado[y, x] = soma

    return resultado


def comparar_resultados(resultados, caminho):
    # Organiza os quatro resultados em uma montagem 2x2 para comparacao visual.
    primeira_linha = np.hstack((resultados[0], resultados[1]))
    segunda_linha = np.hstack((resultados[2], resultados[3]))
    comparacao = np.vstack((primeira_linha, segunda_linha))

    salvar_imagem(comparacao, caminho)

    # Mostra uma medida simples da intensidade media de cada resultado.
    for i, resultado in enumerate(resultados, start=1):
        print(
            f"Laplaciano {i}: media={resultado.mean():.2f}, "
            f"maximo={resultado.max():.2f}"
        )

#Aplica um filtro de suavizacao para a imagem reduzindo os ruidos.
def filtro_media(imagem):
    # Calcula a media dos pixels vizinhos para suavizar a imagem.
    mascara = np.array([
        [1/9, 1/9, 1/9],
        [1/9, 1/9, 1/9],
        [1/9, 1/9, 1/9]
    ])

    return aplicar_mascara(imagem, mascara)

#Cria duas mascaras ,uma para detectar variacoes verticais e outra horizontais
def filtro_sobel(imagem):
    # Detecta bordas nas direcoes horizontal e vertical.
    sobel_x = np.array([
        [-1, -2, -1],
        [ 0,  0,  0],
        [ 1,  2,  1]
    ])

    sobel_y = np.array([
        [-1, 0, 1],
        [-2, 0, 2],
        [-1, 0, 1]
    ])

    gx = aplicar_mascara(imagem, sobel_x)
    gy = aplicar_mascara(imagem, sobel_y)

    # Combina as duas direcoes para calcular a intensidade das bordas.
    magnitude = np.sqrt(gx**2 + gy**2)

    return normalizar(magnitude)


# -------------------------
# Imagem
# -------------------------

imagem = carregar_imagem("testes/images2.png")


# -------------------------
# Filtro da Média
# -------------------------

media = filtro_media(imagem)

salvar_imagem(
    media,
    "results/resultado_media.png"
)


# -------------------------
# Laplaciano
# -------------------------

mascaras_laplaciano = [

    np.array([
        [0,  1, 0],
        [1, -4, 1],
        [0,  1, 0]
    ]),

    np.array([
        [1,  1, 1],
        [1, -8, 1],
        [1,  1, 1]
    ]),

    np.array([
        [ 0, -1,  0],
        [-1,  4, -1],
        [ 0, -1,  0]
    ]),

    np.array([
        [-1, -1, -1],
        [-1,  8, -1],
        [-1, -1, -1]
    ])
]


resultados_laplaciano = []

for i, mascara in enumerate(mascaras_laplaciano):

    resultado = aplicar_mascara(
        imagem,
        mascara
    )

    resultado = normalizar(resultado)
    resultados_laplaciano.append(resultado)

    salvar_imagem(
        resultado,
        f"results/laplaciano_{i + 1}.png"
    )


# Junta os quatro resultados para facilitar a comparacao visual.
comparar_resultados(
    resultados_laplaciano,
    "results/comparacao_laplaciano.png"
)


# -------------------------
# Sobel
# -------------------------

sobel = filtro_sobel(imagem)

salvar_imagem(
    sobel,
    "results/resultado_sobel.png"
)