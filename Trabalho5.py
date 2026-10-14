r"""
GUIA DE EXECUCAO - WINDOWS / POWERSHELL

1. Abra o PowerShell e entre na pasta onde este arquivo foi baixado/extraido
   (substitua pelo caminho real na sua maquina):
    cd "CAMINHO\PARA\PI_Trabalhos"

2. Instale as bibliotecas necessarias:
    python -m pip install -r requirements.txt

3. Verifique a sintaxe do programa:
    python -m py_compile Trabalho5.py

4. Execute a implementacao:
    python Trabalho5.py

A imagem de entrada deve estar em testes/images2.png, dentro da mesma pasta.
Os resultados serao salvos na pasta results/.
"""

import os

from PIL import Image
import numpy as np


def carregar_imagem(caminho):
    # Abre a imagem em tons de cinza.
    imagem = Image.open(caminho).convert("L")
    return np.array(imagem, dtype=np.float64)


def salvar_imagem(matriz, caminho):
    # Limita os pixels a [0, 255] e salva.
    matriz = np.clip(matriz, 0, 255)
    matriz = matriz.astype(np.uint8)

    Image.fromarray(matriz).save(caminho)


def normalizar(imagem):
    # Escala os valores para [0, 255].
    minimo = imagem.min()
    maximo = imagem.max()

    if maximo == minimo:
        return np.zeros_like(imagem)

    return ((imagem - minimo) / (maximo - minimo)) * 255


def zerar_negativos(imagem):
    # Valores negativos recebem 0.
    return np.maximum(imagem, 0)


def tratar_negativos(imagem, modo):
    # Escolhe entre zerar negativos ou escalar.
    if modo == "zero":
        return zerar_negativos(imagem)

    if modo == "escala":
        return normalizar(imagem)

    raise ValueError(f"Modo invalido: {modo}")


def aplicar_mascara(imagem, mascara):
    # Aplica uma mascara 3x3 com bordas preenchidas com zero.
    altura, largura = imagem.shape

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


def montar_comparacao(resultados, caminho):
    # Junta os quatro resultados em uma imagem 2x2.
    primeira_linha = np.hstack((resultados[0], resultados[1]))
    segunda_linha = np.hstack((resultados[2], resultados[3]))
    comparacao = np.vstack((primeira_linha, segunda_linha))

    salvar_imagem(comparacao, caminho)


def comparar_resultados(respostas_brutas):
    # Mostra estatisticas de cada Laplaciano.
    for i, resposta in enumerate(respostas_brutas, start=1):
        print(
            f"Laplaciano {i}: min={resposta.min():.2f}, "
            f"max={resposta.max():.2f}, "
            f"media |valor|={np.abs(resposta).mean():.2f}"
        )

    print("Laplaciano 3 == -Laplaciano 1:",
          np.allclose(respostas_brutas[2], -respostas_brutas[0]))
    print("Laplaciano 4 == -Laplaciano 2:",
          np.allclose(respostas_brutas[3], -respostas_brutas[1]))


def filtro_media(imagem):
    # Suaviza a imagem pela media dos vizinhos.
    mascara = np.array([
        [1/9, 1/9, 1/9],
        [1/9, 1/9, 1/9],
        [1/9, 1/9, 1/9]
    ])

    return aplicar_mascara(imagem, mascara)


def filtro_sobel(imagem):
    # Calcula as derivadas em x e y e a magnitude do gradiente.
    sobel_x = np.array([
        [-1, 0, 1],
        [-2, 0, 2],
        [-1, 0, 1]
    ])

    sobel_y = np.array([
        [-1, -2, -1],
        [ 0,  0,  0],
        [ 1,  2,  1]
    ])

    gx = aplicar_mascara(imagem, sobel_x)
    gy = aplicar_mascara(imagem, sobel_y)
    magnitude = np.sqrt(gx**2 + gy**2)

    return gx, gy, magnitude


os.makedirs("results", exist_ok=True)

imagem = carregar_imagem("testes/images2.png")

media = filtro_media(imagem)
salvar_imagem(media, "results/resultado_media.png")

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

respostas_laplaciano = []
resultados_zero = []
resultados_escala = []
resultados_realce = []

for i, mascara in enumerate(mascaras_laplaciano):

    resposta = aplicar_mascara(imagem, mascara)
    respostas_laplaciano.append(resposta)

    resultado_zero = tratar_negativos(resposta, "zero")
    resultados_zero.append(resultado_zero)
    salvar_imagem(resultado_zero, f"results/laplaciano_{i + 1}_zero.png")

    resultado_escala = tratar_negativos(resposta, "escala")
    resultados_escala.append(resultado_escala)
    salvar_imagem(resultado_escala, f"results/laplaciano_{i + 1}_escala.png")

    if mascara[1, 1] < 0:
        realce = imagem - resposta
    else:
        realce = imagem + resposta

    resultados_realce.append(realce)
    salvar_imagem(realce, f"results/laplaciano_{i + 1}_realce.png")

montar_comparacao(resultados_zero, "results/comparacao_laplaciano_zero.png")
montar_comparacao(resultados_escala, "results/comparacao_laplaciano_escala.png")
montar_comparacao(resultados_realce, "results/comparacao_laplaciano_realce.png")

comparar_resultados(respostas_laplaciano)

gx, gy, magnitude = filtro_sobel(imagem)

for nome, derivada in (("gx", gx), ("gy", gy)):
    for modo in ("zero", "escala"):
        salvar_imagem(
            tratar_negativos(derivada, modo),
            f"results/sobel_{nome}_{modo}.png"
        )

salvar_imagem(normalizar(magnitude), "results/resultado_sobel.png")

print("Resultados salvos na pasta results/")
