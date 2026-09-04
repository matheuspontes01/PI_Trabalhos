"""Adicao e multiplicacao pixel a pixel em imagens PGM P2."""

import argparse


MAX_PIXEL = 255


def ler_tokens(caminho):
	with open(caminho, "r", encoding="ascii") as arquivo:
		for linha in arquivo:
			linha_sem_comentario = linha.split("#", 1)[0]
			yield from linha_sem_comentario.split()


def ler_pgm(caminho):
	tokens = iter(ler_tokens(caminho))
	try:
		formato = next(tokens)
		largura = int(next(tokens))
		altura = int(next(tokens))
		valor_maximo = int(next(tokens))
	except (StopIteration, ValueError) as erro:
		raise ValueError("Arquivo PGM invalido ou incompleto.") from erro

	if formato != "P2":
		raise ValueError("O arquivo precisa estar no formato PGM P2.")
	if largura <= 0 or altura <= 0 or valor_maximo <= 0:
		raise ValueError("Cabecalho PGM invalido.")

	pixels = []
	for _ in range(largura * altura):
		try:
			pixel = int(next(tokens))
		except (StopIteration, ValueError) as erro:
			raise ValueError("Arquivo PGM incompleto ou com pixel invalido.") from erro

		if not 0 <= pixel <= valor_maximo:
			raise ValueError("Pixel fora do intervalo informado no PGM.")
		pixels.append(round(pixel * MAX_PIXEL / valor_maximo))

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


def imprimir(imagem, nome_operacao):
	largura, altura, pixels = imagem
	print(f"\nResultado da {nome_operacao} ({largura}x{altura} pixels):")
	for inicio in range(0, largura * altura, largura):
		print(*pixels[inicio:inicio + largura])


def main():
	parser = argparse.ArgumentParser(
		description="Executa adicao e multiplicacao entre duas imagens PGM P2."
	)
	parser.add_argument("imagem_a", help="Primeira imagem PGM")
	parser.add_argument("imagem_b", help="Segunda imagem PGM")
	argumentos = parser.parse_args()

	try:
		imagem_a = ler_pgm(argumentos.imagem_a)
		imagem_b = ler_pgm(argumentos.imagem_b)
		imprimir(adicionar(imagem_a, imagem_b), "adicao")
		imprimir(multiplicar(imagem_a, imagem_b), "multiplicacao")
	except (OSError, ValueError) as erro:
		parser.error(str(erro))


if __name__ == "__main__":
	main()
