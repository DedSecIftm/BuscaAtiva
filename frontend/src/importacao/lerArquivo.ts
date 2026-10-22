export type TipoArquivo = 'Excel' | 'XML' | 'JSON'

export type Linha = Record<string, string>

export interface Tabela {
  colunas: string[]
  linhas: Linha[]
}

const EXTENSOES: Record<string, TipoArquivo> = {
  xlsx: 'Excel',
  xls: 'Excel',
  xml: 'XML',
  json: 'JSON',
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

function normalizarLinha(objeto: Record<string, unknown>): Linha {
  return Object.fromEntries(Object.entries(objeto).map(([chave, valor]) => [chave, paraTexto(valor)]))
}

// A biblioteca de Excel é grande, então só é carregada quando necessária.
async function lerExcel(arquivo: File): Promise<Tabela> {
  const { read, utils } = await import('xlsx')
  const planilha = read(await arquivo.arrayBuffer(), { cellDates: true })
  const primeiraAba = planilha.Sheets[planilha.SheetNames[0]]
  const linhas = utils.sheet_to_json<Record<string, unknown>>(primeiraAba, { defval: '' })
  return montarTabela(linhas.map(normalizarLinha))
}

// Aceita uma lista de objetos ou um objeto que contenha uma lista de objetos.
async function lerJson(arquivo: File): Promise<Tabela> {
  let dados: unknown
  try {
    dados = JSON.parse(await arquivo.text())
  } catch {
    throw new Error('O arquivo JSON está com formato inválido.')
  }
  const lista = Array.isArray(dados)
    ? dados
    : Object.values(dados as object).find((valor) => Array.isArray(valor))
  if (!lista) throw new Error('Não foi encontrada uma lista de registros no arquivo JSON.')
  return montarTabela(
    lista.filter((item) => item && typeof item === 'object').map((item) => normalizarLinha(item)),
  )
}

// Cada filho do elemento raiz é um registro; os campos vêm dos atributos e dos elementos filhos.
async function lerXml(arquivo: File): Promise<Tabela> {
  const documento = new DOMParser().parseFromString(await arquivo.text(), 'application/xml')
  if (documento.querySelector('parsererror')) throw new Error('O arquivo XML está com formato inválido.')

  let registros = Array.from(documento.documentElement.children)
  // Se a raiz tiver um único filho que agrupa os registros (ex.: <dados><alunos><aluno/>...), desce um nível.
  while (registros.length === 1 && registros[0].children.length > 0 && registros[0].children[0].children.length > 0) {
    registros = Array.from(registros[0].children)
  }

  const linhas = registros.map((registro) => {
    const linha: Linha = {}
    Array.from(registro.attributes).forEach((atributo) => (linha[atributo.name] = atributo.value))
    Array.from(registro.children).forEach((campo) => (linha[campo.tagName] = campo.textContent?.trim() ?? ''))
    return linha
  })
  return montarTabela(linhas)
}

const LEITORES: Record<TipoArquivo, (arquivo: File) => Promise<Tabela>> = {
  Excel: lerExcel,
  XML: lerXml,
  JSON: lerJson,
}

export function lerArquivo(arquivo: File): Promise<Tabela> {
  const tipo = tipoDoArquivo(arquivo)
  if (!tipo) return Promise.reject(new Error('Formato de arquivo não aceito.'))
  return LEITORES[tipo](arquivo)
}
