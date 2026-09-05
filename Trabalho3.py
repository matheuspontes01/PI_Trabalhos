"""Adicao, multiplicacao e espelhamento pixel a pixel em imagens PNG."""

import argparse
from PIL import Image


MAX_PIXEL = 255


def ler_imagem(caminho):
	imagem = Image.open(caminho).convert("L")
	largura, altura = imagem.size
	pixels = list(imagem.getdata())
	return largura, altura, pixels


def validar_tamanho(imagem_a, imagem_b):
	if imagem_a[:2] != imagem_b[:2]:
		raise ValueError("As imagens precisam ter o mesmo tamanho.")


def adicionar(imagem_a, imagem_b):
	validar_tamanho(imagem_a, imagem_b)
	largura, altura, pixels_a = imagem_a
	_, _, pixels_b = imagem_b
	pixels = [min(MAX_PIXEL, a + b) for a, b in zip(pixels_a, pixels_b)]
	return largura, altura, pixels


def multiplicar(imagem_a, imagem_b):
	validar_tamanho(imagem_a, imagem_b)
	largura, altura, pixels_a = imagem_a
	_, _, pixels_b = imagem_b
	pixels = [round(a * b / MAX_PIXEL) for a, b in zip(pixels_a, pixels_b)]
	return largura, altura, pixels


def espelhar_horizontal(imagem):
	largura, altura, pixels = imagem
	resultado = []

	for y in range(altura):
		inicio = y * largura
		fim = inicio + largura
		linha = pixels[inicio:fim]
		linha.reverse()
		resultado.extend(linha)

	return largura, altura, resultado

def negativo(imagem):
	largura, altura, pixels = imagem
	pixels = [MAX_PIXEL - pixel for pixel in pixels]
	return largura, altura, pixels


def salvar_imagem(imagem, caminho):
	largura, altura, pixels = imagem
	imagem_saida = Image.new("L", (largura, altura))
	imagem_saida.putdata(pixels)
	imagem_saida.save(caminho)


def imprimir(imagem, nome_operacao):
	largura, altura, pixels = imagem
	print(f"\nResultado da {nome_operacao} ({largura}x{altura} pixels):")
	for inicio in range(0, largura * altura, largura):
		print(*pixels[inicio:inicio + largura])


def main():
	parser = argparse.ArgumentParser(
		description="Executa adicao, multiplicacao e espelhamento entre imagens PNG."
	)
	parser.add_argument("imagem_a", help="Primeira imagem PNG")
	parser.add_argument("imagem_b", help="Segunda imagem PNG")
	argumentos = parser.parse_args()

	try:
		imagem_a = ler_imagem(argumentos.imagem_a)
		imagem_b = ler_imagem(argumentos.imagem_b)

		resultado_adicao = adicionar(imagem_a, imagem_b)
		resultado_multiplicacao = multiplicar(imagem_a, imagem_b)
		resultado_espelhamento = espelhar_horizontal(imagem_a)
		resultado_negativo = negativo(imagem_a)

		imprimir(resultado_adicao, "adicao")
		imprimir(resultado_multiplicacao, "multiplicacao")
		imprimir(resultado_espelhamento, "espelhamento horizontal")
		imprimir(resultado_negativo, "negativo")

		salvar_imagem(resultado_adicao, "results/resultado_adicao.png")
		salvar_imagem(resultado_multiplicacao, "results/sresultado_multiplicacao.png")
		salvar_imagem(resultado_espelhamento, "results/resultado_espelhamento.png")
		salvar_imagem(resultado_negativo, "results/resultado_negativo.png")

	except (OSError, ValueError) as erro:
		parser.error(str(erro))


if __name__ == "__main__":
	main()