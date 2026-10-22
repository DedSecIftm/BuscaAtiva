import type { Tabela, TipoArquivo } from './lerArquivo.ts'

interface Base {
  id: string
  arquivo: File
  tipo: TipoArquivo
}

export type ArquivoCarregado =
  | (Base & { status: 'lendo' })
  | (Base & { status: 'pronto'; tabela: Tabela })
  | (Base & { status: 'erro'; erro: string })
