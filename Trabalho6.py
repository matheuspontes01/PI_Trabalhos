r"""
GUIA DE EXECUCAO - WINDOWS / POWERSHELL

1. Abra o PowerShell e entre na pasta do trabalho:
    cd "C:\Users\canal\OneDrive\Documentos\pi\PI_Trabalhos"

2. Instale as bibliotecas necessarias:
    python -m pip install -r requirements.txt

3. Verifique a sintaxe do programa:
    python -m py_compile Trabalho6.py

4. Execute a implementacao:
    python Trabalho6.py

Opcoes escolhidas (Aula 11 - Morfologia Matematica):
    1. Erosao e Dilatacao (imagem binaria).
    3. Extracao de Contornos (imagem binaria).

As imagens de entrada sao os exemplos da Aula 11, salvos em testes/:
    morfologia_texto.png   (exemplo pratico de erosao e dilatacao)
    morfologia_digital.png (impressao digital com ruido)
Os resultados serao salvos na pasta results/ e exibidos em uma janela
(use os botoes ou as setas do teclado para trocar de operacao).
"""

import os

from PIL import Image
import matplotlib.pyplot as plt
from matplotlib.widgets import Button
import numpy as np


def carregar_imagem_binaria(caminho, limiar=127):
    # Abre em tons de cinza e binariza: objeto = 1, fundo = 0.
    imagem = np.array(Image.open(caminho).convert("L"))
    return (imagem > limiar).astype(np.uint8)


def salvar_imagem_binaria(matriz, caminho):
    # Converte 0/1 para 0/255 e salva.
    Image.fromarray((matriz * 255).astype(np.uint8)).save(caminho)


def deslocamentos(elemento):
    # Lista os deslocamentos (dy, dx) dos 1s do elemento estruturante,
    # tomando o centro como origem.
    centro_y = elemento.shape[0] // 2
    centro_x = elemento.shape[1] // 2

    return [
        (y - centro_y, x - centro_x)
        for y in range(elemento.shape[0])
        for x in range(elemento.shape[1])
        if elemento[y, x] == 1
    ]


def transladar(imagem, dy, dx, preenchimento):
    # Retorna a imagem vista a partir do deslocamento (dy, dx):
    # resultado[y, x] = imagem[y + dy, x + dx].
    altura, largura = imagem.shape
    borda = max(abs(dy), abs(dx))

    imagem_padding = np.pad(
        imagem,
        pad_width=borda,
        mode="constant",
        constant_values=preenchimento
    )

    return imagem_padding[
        borda + dy: borda + dy + altura,
        borda + dx: borda + dx + largura
    ]


def erosao(imagem, elemento):
    # A (-) B = {x | (B)x esta contido em A}
    # O pixel fica 1 somente se TODOS os vizinhos cobertos por B forem 1.
    # A borda e preenchida com 1 para nao erodir a partir das laterais.
    resultado = np.ones_like(imagem)

    for dy, dx in deslocamentos(elemento):
        resultado &= transladar(imagem, dy, dx, preenchimento=1)

    return resultado


def dilatacao(imagem, elemento):
    # A (+) B = {x | (B^)x intercepta A}
    # O pixel fica 1 se PELO MENOS UM vizinho coberto por B^ for 1.
    resultado = np.zeros_like(imagem)

    for dy, dx in deslocamentos(elemento):
        # Usa o elemento refletido (B^): deslocamento (-dy, -dx).
        resultado |= transladar(imagem, -dy, -dx, preenchimento=0)

    return resultado


def extrair_contorno(imagem, elemento):
    # beta(A) = A - (A (-) B) = A AND NOT(A (-) B)
    return imagem & (1 - erosao(imagem, elemento))


def montar_comparacao(imagens, caminho, vertical=False):
    # Junta as imagens lado a lado (ou empilhadas), separadas por uma
    # faixa cinza.
    if vertical:
        separador = np.full((10, imagens[0].shape[1]), 128, dtype=np.uint8)
    else:
        separador = np.full((imagens[0].shape[0], 10), 128, dtype=np.uint8)

    partes = []

    for imagem in imagens:
        partes.append((imagem * 255).astype(np.uint8))
        partes.append(separador)

    juntar = np.vstack if vertical else np.hstack
    Image.fromarray(juntar(partes[:-1])).save(caminho)


def comparar_com_slide(resultado, caminho_slide, nome):
    # Conta quantos pixels diferem do resultado mostrado no slide.
    esperado = carregar_imagem_binaria(caminho_slide)
    diferencas = int((resultado != esperado).sum())

    print(f"{nome}: {diferencas} pixels diferentes do slide")


def mostrar_interface(comparacoes):
    # Janela com "Antes" e "Depois" lado a lado. Os botoes (ou as setas
    # do teclado) trocam a operacao exibida.
    figura, (eixo_antes, eixo_depois) = plt.subplots(1, 2, figsize=(12, 6))
    figura.subplots_adjust(bottom=0.15)
    pagina = {"atual": 0}

    def desenhar():
        titulo, antes, depois = comparacoes[pagina["atual"]]

        # Imagens largas (como o texto) ficam uma acima da outra.
        altura, largura = antes.shape
        if largura > 2 * altura:
            eixo_antes.set_position([0.05, 0.55, 0.9, 0.33])
            eixo_depois.set_position([0.05, 0.15, 0.9, 0.33])
        else:
            eixo_antes.set_position([0.03, 0.15, 0.45, 0.73])
            eixo_depois.set_position([0.52, 0.15, 0.45, 0.73])

        for eixo, imagem, nome in (
            (eixo_antes, antes, "Antes"),
            (eixo_depois, depois, "Depois"),
        ):
            eixo.clear()
            eixo.imshow(imagem, cmap="gray", vmin=0, vmax=1,
                        interpolation="nearest")
            eixo.set_title(nome)
            eixo.axis("off")

        figura.suptitle(
            f"{titulo}   ({pagina['atual'] + 1}/{len(comparacoes)})",
            fontsize=14
        )
        figura.canvas.draw_idle()

    def mudar(passo):
        pagina["atual"] = (pagina["atual"] + passo) % len(comparacoes)
        desenhar()

    def tecla(evento):
        if evento.key == "right":
            mudar(1)
        elif evento.key == "left":
            mudar(-1)

    botao_anterior = Button(figura.add_axes([0.3, 0.03, 0.15, 0.07]),
                            "< Anterior")
    botao_proximo = Button(figura.add_axes([0.55, 0.03, 0.15, 0.07]),
                           "Proximo >")
    botao_anterior.on_clicked(lambda _: mudar(-1))
    botao_proximo.on_clicked(lambda _: mudar(1))
    figura.canvas.mpl_connect("key_press_event", tecla)

    desenhar()
    plt.show()


def elemento_quadrado(tamanho):
    # Elemento estruturante quadrado cheio de 1s.
    return np.ones((tamanho, tamanho), dtype=np.uint8)


# Elemento estruturante em cruz (4-conexo).
elemento_cruz = np.array([
    [0, 1, 0],
    [1, 1, 1],
    [0, 1, 0]
], dtype=np.uint8)

os.makedirs("results", exist_ok=True)

# ---------------------------------------------------------------
# Exemplo 1: texto "III - ERI / UNIMEP & UNISANTA" (slides 13 e 20)
# ---------------------------------------------------------------
# Na resolucao em que a imagem foi extraida do slide, o resultado
# mostrado corresponde a um elemento estruturante quadrado 5x5.
texto = carregar_imagem_binaria("testes/morfologia_texto.png")

texto_erodido = erosao(texto, elemento_quadrado(5))
texto_dilatado = dilatacao(texto, elemento_quadrado(5))
texto_contorno = extrair_contorno(texto, elemento_quadrado(3))

salvar_imagem_binaria(texto_erodido, "results/texto_erosao.png")
salvar_imagem_binaria(texto_dilatado, "results/texto_dilatacao.png")
salvar_imagem_binaria(texto_contorno, "results/texto_contorno.png")

comparar_com_slide(
    texto_dilatado, "testes/morfologia_texto_dilatado_slide.png", "Dilatacao"
)
comparar_com_slide(
    texto_erodido, "testes/morfologia_texto_erodido_slide.png", "Erosao"
)

# Imagens empilhadas verticalmente, pois o texto e largo.
montar_comparacao(
    [texto, texto_erodido, texto_dilatado, texto_contorno],
    "results/comparacao_texto.png",
    vertical=True
)

# ---------------------------------------------------------------
# Exemplo 2: impressao digital com ruido (slide de abertura/fechamento)
# ---------------------------------------------------------------
digital = carregar_imagem_binaria("testes/morfologia_digital.png")

digital_erodida = erosao(digital, elemento_quadrado(3))
digital_dilatada = dilatacao(digital, elemento_quadrado(3))
digital_contorno_8 = extrair_contorno(digital, elemento_quadrado(3))
digital_contorno_4 = extrair_contorno(digital, elemento_cruz)

salvar_imagem_binaria(digital_erodida, "results/digital_erosao.png")
salvar_imagem_binaria(digital_dilatada, "results/digital_dilatacao.png")
salvar_imagem_binaria(digital_contorno_8, "results/digital_contorno_8.png")
salvar_imagem_binaria(digital_contorno_4, "results/digital_contorno_4.png")

montar_comparacao(
    [digital, digital_erodida, digital_dilatada],
    "results/comparacao_digital_erosao_dilatacao.png"
)
montar_comparacao(
    [digital, digital_contorno_8, digital_contorno_4],
    "results/comparacao_digital_contornos.png"
)

print("\nPixels de objeto na impressao digital:")
print(f"  original:  {digital.sum()}")
print(f"  erosao:    {digital_erodida.sum()}")
print(f"  dilatacao: {digital_dilatada.sum()}")
print(f"  contorno:  {digital_contorno_8.sum()}")

print("\nResultados salvos na pasta results/")

mostrar_interface([
    ("Texto - Erosao (5x5)", texto, texto_erodido),
    ("Texto - Dilatacao (5x5)", texto, texto_dilatado),
    ("Texto - Extracao de contorno (3x3)", texto, texto_contorno),
    ("Impressao digital - Erosao (3x3)", digital, digital_erodida),
    ("Impressao digital - Dilatacao (3x3)", digital, digital_dilatada),
    ("Impressao digital - Contorno (8-conexo)", digital, digital_contorno_8),
    ("Impressao digital - Contorno (4-conexo)", digital, digital_contorno_4),
])
