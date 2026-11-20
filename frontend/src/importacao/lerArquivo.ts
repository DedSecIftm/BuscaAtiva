// O backend só importa planilhas .xlsx. A leitura aqui serve apenas para mostrar a prévia antes de enviar.
export type TipoArquivo = 'Excel'

export type Linha = Record<string, string>

export interface Tabela {
  colunas: string[]
  linhas: Linha[]
}

const EXTENSOES: Record<string, TipoArquivo> = {
  xlsx: 'Excel',
}

export function tipoDoArquivo(arquivo: File): TipoArquivo | undefined {
  const extensao = arquivo.name.split('.').pop()?.toLowerCase() ?? ''
  return EXTENSOES[extensao]
}

function montarTabela(linhas: Linha[]): Tabela {
  const colunas = new Set<string>()
  linhas.forEach((linha) => Object.keys(linha).forEach((c) => colunas.add(c)))
  if (linhas.length === 0) throw new Error('Nenhum registro encontrado no arquivo.')
  return { colunas: [...colunas], linhas }
}

function paraTexto(valor: unknown): string {
  if (valor === null || valor === undefined) return ''
  if (valor instanceof Date) return valor.toLocaleDateString('pt-BR')
  if (typeof valor === 'object') return JSON.stringify(valor)
  return String(valor)
}

const COLUNAS_ESPERADAS = ['turma', 'idsimade', 'estudante', '%depresenca', 'faltas']

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .replace(/\s+/g, '')
    .toLowerCase()
}

// Como no backend, o cabeçalho pode vir depois de linhas de título: procura nas primeiras linhas a que tem todas as
// colunas esperadas. Devolve -1 se nenhuma linha tiver.
function posicaoDoCabecalho(linhas: unknown[][]): number {
  return linhas.slice(0, 20).findIndex((linha) => {
    const nomes = new Set(linha.map((celula) => normalizar(paraTexto(celula))))
    return COLUNAS_ESPERADAS.every((coluna) => nomes.has(coluna))
  })
}

// A biblioteca de Excel é grande, então só é carregada quando necessária.
async function lerExcel(arquivo: File): Promise<Tabela> {
  const { read, utils } = await import('xlsx')
  const planilha = read(await arquivo.arrayBuffer(), { cellDates: true })
  const primeiraAba = planilha.Sheets[planilha.SheetNames[0]]
  const matriz = utils.sheet_to_json<unknown[]>(primeiraAba, { header: 1, defval: '' })

  const achado = posicaoDoCabecalho(matriz)
  const cabecalho = Math.max(achado, 0)
  const nomes = (matriz[cabecalho] ?? []).map((celula, indice) => paraTexto(celula).trim() || `Coluna ${indice + 1}`)

  // Com o cabeçalho reconhecido, mostra só as colunas que a importação usa; outras (listas auxiliares, colunas
  // vazias) ficam de fora para não parecerem parte da importação. Sem cabeçalho reconhecido, mostra tudo.
  const posicoes = nomes
    .map((_, indice) => indice)
    .filter((indice) => achado < 0 || COLUNAS_ESPERADAS.includes(normalizar(nomes[indice])))

  const linhas = matriz
    .slice(cabecalho + 1)
    .filter((linha) => posicoes.some((indice) => paraTexto(linha[indice]).trim() !== ''))
    .map((linha) => Object.fromEntries(posicoes.map((indice) => [nomes[indice], paraTexto(linha[indice])])))
  return montarTabela(linhas)
}

export function lerArquivo(arquivo: File): Promise<Tabela> {
  const tipo = tipoDoArquivo(arquivo)
  if (!tipo) return Promise.reject(new Error('Formato de arquivo não aceito.'))
  return lerExcel(arquivo)
}
