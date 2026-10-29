"""Segmentacao binaria pelo metodo do vale em histograma bimodal.

Uso:
    python Trabalho7_Segmentacao_Vale.py caminho/da/imagem.png

Instalacao:
    python -m pip install pillow numpy matplotlib
"""

import argparse
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
from PIL import Image


def carregar_imagem(caminho):
    imagem = Image.open(caminho).convert("L")
    return np.array(imagem, dtype=np.uint8)


def encontrar_limiar_vale(imagem):
    # Conta os pixels de cada intensidade, de 0 a 255.
    histograma = np.bincount(imagem.ravel(), minlength=256)

    # Suaviza pequenas oscilacoes para identificar os picos principais.
    janela = np.ones(9) / 9
    histograma_suave = np.convolve(histograma, janela, mode="same")

    # Seleciona os dois picos mais altos, evitando picos muito proximos.
    picos = []
    for nivel in np.argsort(histograma_suave)[::-1]:
        nivel = int(nivel)
        if all(abs(nivel - pico) >= 30 for pico in picos):
            picos.append(nivel)
        if len(picos) == 2:
            break

    pico1, pico2 = sorted(picos)

    # Procura o ponto mais baixo entre os dois picos.
    trecho = histograma_suave[pico1 + 1:pico2]
    menor_frequencia = trecho.min()
    vales = np.flatnonzero(np.isclose(trecho, menor_frequencia)) + pico1 + 1

    # Em caso de empate, escolhe o vale mais proximo ao meio da escala.
    limiar = int(vales[np.argmin(np.abs(vales - 127.5))])
    return limiar, histograma


def segmentar(imagem, limiar):
    return np.where(imagem > limiar, 255, 0).astype(np.uint8)


def main():
    parser = argparse.ArgumentParser(description="Segmentacao pelo metodo do vale")
    parser.add_argument("imagem", help="Caminho da imagem de entrada")
    args = parser.parse_args()

    imagem = carregar_imagem(args.imagem)
    limiar, histograma = encontrar_limiar_vale(imagem)
    segmentada = segmentar(imagem, limiar)

    pasta = Path("results")
    pasta.mkdir(exist_ok=True)
    nome = Path(args.imagem).stem
    caminho_saida = pasta / f"{nome}_segmentada.png"
    Image.fromarray(segmentada).save(caminho_saida)

    plt.figure(figsize=(9, 4))
    plt.bar(range(256), histograma, width=1, color="gray")
    plt.axvline(limiar, color="red", linestyle="--", label=f"Limiar T = {limiar}")
    plt.title("Histograma e limiar pelo metodo do vale")
    plt.xlabel("Intensidade (0-255)")
    plt.ylabel("Quantidade de pixels")
    plt.legend()
    plt.tight_layout()
    caminho_histograma = pasta / f"{nome}_histograma.png"
    plt.savefig(caminho_histograma)
    plt.close()

    print(f"Limiar encontrado: T = {limiar}")
    print(f"Imagem segmentada: {caminho_saida}")
    print(f"Histograma: {caminho_histograma}")


if __name__ == "__main__":
    main()
