"""
Equalização de histograma em imagens PNG em escala de cinza.

Implementação baseada em:
1 - Montagem do histograma
2 - Histograma normalizado
3 - Cálculo da frequência acumulada
4 - Criação da LUT
5 - Transformação da imagem

Execute:
python Trabalho4.py testes/images.png
"""


import argparse
from PIL import Image


MAX_PIXEL = 255


# Carrega a imagem, converte para escala de cinza
# e transforma os pixels em lista.
def ler_imagem(caminho):

    imagem = Image.open(caminho).convert("L")

    largura, altura = imagem.size

    pixels = list(imagem.getdata())

    return largura, altura, pixels



# Calcula o histograma da imagem.
# Cada posição representa um nível de cinza.
def calcular_histograma(imagem):

    largura, altura, pixels = imagem

    histograma = [0] * 256


    for pixel in pixels:

        histograma[pixel] += 1


    return histograma



# Normaliza o histograma:
# p(rk) = nk / n
def normalizar_histograma(histograma, total_pixels):

    normalizado = []


    for quantidade in histograma:

        probabilidade = quantidade / total_pixels

        normalizado.append(probabilidade)


    return normalizado



# Calcula a frequência acumulada (CDF)
def calcular_cdf(histograma_normalizado):

    cdf = []

    soma = 0


    for valor in histograma_normalizado:

        soma += valor

        cdf.append(soma)


    return cdf



# Cria a tabela LUT
# T(rk) = (L-1) * CDF
def criar_lut(cdf):

    lut = []


    for valor in cdf:

        novo_valor = round(MAX_PIXEL * valor)

        lut.append(novo_valor)


    return lut



# Aplica a LUT em cada pixel da imagem
def aplicar_lut(imagem, lut):

    largura, altura, pixels = imagem


    novos_pixels = []


    for pixel in pixels:

        novos_pixels.append(
            lut[pixel]
        )


    return largura, altura, novos_pixels



# Salva a imagem resultante
def salvar_imagem(imagem, caminho):

    largura, altura, pixels = imagem


    imagem_saida = Image.new(
        "L",
        (largura, altura)
    )


    imagem_saida.putdata(pixels)

    imagem_saida.save(caminho)



# Mostra a matriz de pixels
def imprimir(imagem):

    largura, altura, pixels = imagem


    print(
        f"\nImagem {largura}x{altura}:"
    )


    for inicio in range(
        0,
        largura * altura,
        largura
    ):

        print(
            *pixels[inicio:inicio+largura]
        )



def main():

    parser = argparse.ArgumentParser(
        description="Equalização de histograma"
    )


    parser.add_argument(
        "imagem",
        help="Imagem PNG de entrada"
    )


    argumentos = parser.parse_args()


    try:

        imagem = ler_imagem(
            argumentos.imagem
        )


        largura, altura, pixels = imagem


        # 1 - Histograma
        histograma = calcular_histograma(
            imagem
        )


        # 2 - Histograma normalizado
        hist_normalizado = normalizar_histograma(
            histograma,
            largura * altura
        )


        # 3 - Frequência acumulada
        cdf = calcular_cdf(
            hist_normalizado
        )


        # 4 - LUT
        lut = criar_lut(
            cdf
        )


        # 5 - Transformação da imagem
        resultado = aplicar_lut(
            imagem,
            lut
        )


        imprimir(resultado)


        salvar_imagem(
            resultado,
            "results/imagem_equalizada.png"
        )


        print(
            "\nEqualização concluída!"
        )


    except OSError as erro:

        parser.error(
            str(erro)
        )



if __name__ == "__main__":
    main()