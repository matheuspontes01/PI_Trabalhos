from PIL import Image
import numpy as np


def carregar_imagem(caminho):
    imagem = Image.open(caminho).convert("L")
    return np.array(imagem, dtype=np.float64)


def salvar_imagem(matriz, caminho):
    matriz = np.clip(matriz, 0, 255)
    matriz = matriz.astype(np.uint8)

    Image.fromarray(matriz).save(caminho)


def normalizar(imagem):
    minimo = imagem.min()
    maximo = imagem.max()

    if maximo == minimo:
        return np.zeros_like(imagem)

    return ((imagem - minimo) / (maximo - minimo)) * 255


def aplicar_mascara(imagem, mascara):
    altura, largura = imagem.shape

    # tratamento das bordas: padding com zeros
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


def filtro_media(imagem):

    mascara = np.array([
        [1/9, 1/9, 1/9],
        [1/9, 1/9, 1/9],
        [1/9, 1/9, 1/9]
    ])

    return aplicar_mascara(imagem, mascara)


def filtro_sobel(imagem):

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


for i, mascara in enumerate(mascaras_laplaciano):

    resultado = aplicar_mascara(
        imagem,
        mascara
    )

    resultado = normalizar(resultado)

    salvar_imagem(
        resultado,
        f"results/laplaciano_{i + 1}.png"
    )


# -------------------------
# Sobel
# -------------------------

sobel = filtro_sobel(imagem)

salvar_imagem(
    sobel,
    "results/resultado_sobel.png"
)