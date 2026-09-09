"""Operacoes pixel a pixel em imagens PNG em escala de cinza."""

# Adicao, multiplicacao, espelhamento horizontal e negativo de imagens.
# Feito pelos alunos: Matheus Silva Pontes & Lucas Monteiro de Carvalho

# Execute no terminal:
# pip install -r requirements.txt
# python Trabalho3.py testes/imagem_a.pgm testes/imagem_b.pgm

import argparse
from PIL import Image


MAX_PIXEL = 255


# Carrega a imagem, converte para escala de cinza e transforma os pixels em lista.
def ler_imagem(caminho):
	imagem = Image.open(caminho).convert("L")
	largura, altura = imagem.size
	pixels = list(imagem.getdata())
	return largura, altura, pixels


# Garante que as duas imagens possam ser comparadas pixel a pixel.
def validar_tamanho(imagem_a, imagem_b):
	if imagem_a[:2] != imagem_b[:2]:
		raise ValueError("As imagens precisam ter o mesmo tamanho.")


# Soma os pixels correspondentes e limita o resultado ao valor maximo permitido.
def adicionar(imagem_a, imagem_b):
	validar_tamanho(imagem_a, imagem_b)
	largura, altura, pixels_a = imagem_a
	_, _, pixels_b = imagem_b
	pixels = [min(MAX_PIXEL, a + b) for a, b in zip(pixels_a, pixels_b)]
	return largura, altura, pixels


# Multiplica os pixels correspondentes e normaliza o resultado para a faixa 0-255.
def multiplicar(imagem_a, imagem_b):
	validar_tamanho(imagem_a, imagem_b)
	largura, altura, pixels_a = imagem_a
	_, _, pixels_b = imagem_b
	pixels = [round(a * b / MAX_PIXEL) for a, b in zip(pixels_a, pixels_b)]
	return largura, altura, pixels


# Inverte a ordem dos pixels de cada linha, espelhando a imagem horizontalmente.
def espelhar_horizontal(imagem):
    largura, altura, pixels = imagem
    resultado = []
	
    for y in range(altura):
		#Faz o calculo do inicio da matriz
        inicio = y * largura
		#Percorremos a matriz de tras pra frente
        for x in range(largura - 1, -1, -1):
            resultado.append(pixels[inicio + x])
    return largura, altura, resultado

# Inverte a intensidade de cada pixel: preto vira branco e vice-versa.
def negativo(imagem):
	largura, altura, pixels = imagem
	pixels = [MAX_PIXEL - pixel for pixel in pixels]
	return largura, altura, pixels

# Cria uma imagem em escala de cinza e grava os pixels no caminho informado.
def salvar_imagem(imagem, caminho):
	largura, altura, pixels = imagem
	imagem_saida = Image.new("L", (largura, altura))
	imagem_saida.putdata(pixels)
	imagem_saida.save(caminho)


# Exibe a matriz de pixels no terminal, linha por linha.
def imprimir(imagem, nome_operacao):
	largura, altura, pixels = imagem
	print(f"\nResultado da {nome_operacao} ({largura}x{altura} pixels):")
	for inicio in range(0, largura * altura, largura):
		print(*pixels[inicio:inicio + largura])


# Le os argumentos, executa as operacoes e salva os resultados.
def main():
	parser = argparse.ArgumentParser(
		description="Executa operacoes pixel a pixel entre duas imagens."
	)
	parser.add_argument("imagem_a", help="Primeira imagem PNG")
	parser.add_argument("imagem_b", help="Segunda imagem PNG")
	argumentos = parser.parse_args()

	try:
		imagem_a = ler_imagem(argumentos.imagem_a)
		imagem_b = ler_imagem(argumentos.imagem_b)

		# Calcula cada resultado usando a primeira imagem como referencia.
		resultado_adicao = adicionar(imagem_a, imagem_b)
		resultado_multiplicacao = multiplicar(imagem_a, imagem_b)
		resultado_espelhamento = espelhar_horizontal(imagem_a)
		resultado_negativo = negativo(imagem_a)

		# Mostra os resultados numericamente antes de gerar os arquivos.
		imprimir(resultado_adicao, "adicao")
		imprimir(resultado_multiplicacao, "multiplicacao")
		imprimir(resultado_espelhamento, "espelhamento horizontal")
		imprimir(resultado_negativo, "negativo")

		# Salva cada operacao na pasta de resultados.
		salvar_imagem(resultado_adicao, "results/resultado_adicao.png")
		salvar_imagem(resultado_multiplicacao, "results/resultado_multiplicacao.png")
		salvar_imagem(resultado_espelhamento, "results/resultado_espelhamento.png")
		salvar_imagem(resultado_negativo, "results/resultado_negativo.png")

	except (OSError, ValueError) as erro:
		parser.error(str(erro))


if __name__ == "__main__":
	main()