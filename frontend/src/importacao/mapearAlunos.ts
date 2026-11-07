import type { Linha, Tabela } from './lerArquivo.ts'

export type AlunoImportado = Record<Campo, string>

type Campo = 'nome' | 'matricula' | 'email' | 'telefone' | 'turma' | 'faltas' | 'periodoEmDias'

// Cada arquivo pode nomear as colunas de um jeito; os nomes aqui ficam sem acento, espaço ou maiúscula.
const NOMES_DAS_COLUNAS: Record<Campo, string[]> = {
  nome: ['nome', 'nomedoaluno', 'nomecompleto', 'aluno', 'estudante'],
  matricula: ['matricula', 'numerodematricula', 'ra'],
  email: ['email', 'emaildoaluno'],
  telefone: ['telefone', 'celular', 'fone', 'whatsapp', 'contato'],
  turma: ['turma', 'classe'],
  faltas: ['faltas', 'quantidadedefaltas', 'totaldefaltas'],
  periodoEmDias: ['periodoemdias', 'periodo', 'dias'],
}

const OBRIGATORIOS: Campo[] = ['nome', 'turma']

function normalizar(coluna: string): string {
  return coluna
    .normalize('NFD')
    .replace(/[^a-zA-Z0-9]/g, '')
    .toLowerCase()
}

function colunaDoCampo(campo: Campo, colunas: string[]): string | undefined {
  return colunas.find((coluna) => NOMES_DAS_COLUNAS[campo].includes(normalizar(coluna)))
}

export function mapearAlunos({ colunas, linhas }: Tabela): AlunoImportado[] {
  const campos = Object.keys(NOMES_DAS_COLUNAS) as Campo[]
  const faltando = OBRIGATORIOS.filter((campo) => !colunaDoCampo(campo, colunas))
  if (faltando.length > 0) {
    throw new Error(`O arquivo precisa ter a coluna ${faltando.join(' e a coluna ')}.`)
  }

  const colunaDe = Object.fromEntries(campos.map((campo) => [campo, colunaDoCampo(campo, colunas)]))
  const valor = (linha: Linha, campo: Campo) => linha[colunaDe[campo] ?? '']?.trim() ?? ''
  return linhas.map(
    (linha) =>
      Object.fromEntries(campos.map((campo) => [campo, valor(linha, campo)])) as AlunoImportado,
  )
}
