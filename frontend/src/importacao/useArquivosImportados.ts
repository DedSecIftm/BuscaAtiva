import { useState } from 'react'
import { lerArquivo, tipoDoArquivo } from './lerArquivo.ts'
import type { ArquivoCarregado } from './tipos.ts'

function chaveDoArquivo(arquivo: File): string {
  return `${arquivo.name}-${arquivo.size}-${arquivo.lastModified}`
}

// Usado no Layout, acima das rotas: os arquivos e as leituras em andamento continuam ao trocar de tela.
export default function useArquivosImportados() {
  const [itens, setItens] = useState<ArquivoCarregado[]>([])

  function atualizar(novo: ArquivoCarregado) {
    setItens((atuais) => atuais.map((item) => (item.id === novo.id ? novo : item)))
  }

  function adicionarArquivos(arquivos: File[]) {
    const existentes = new Set(itens.map((item) => item.id))
    const novos: ArquivoCarregado[] = arquivos
      .filter((a) => tipoDoArquivo(a) && !existentes.has(chaveDoArquivo(a)))
      .map((a) => ({ id: chaveDoArquivo(a), arquivo: a, tipo: tipoDoArquivo(a)!, status: 'lendo' }))
    setItens((atuais) => [...atuais, ...novos])

    novos.forEach(({ id, arquivo, tipo }) => {
      lerArquivo(arquivo)
        .then((tabela) => atualizar({ id, arquivo, tipo, status: 'pronto', tabela }))
        .catch((erro: unknown) =>
          atualizar({
            id,
            arquivo,
            tipo,
            status: 'erro',
            erro: erro instanceof Error ? erro.message : 'Não foi possível ler o arquivo.',
          }),
        )
    })
  }

  function remover(id: string) {
    setItens((atuais) => atuais.filter((item) => item.id !== id))
  }

  function limpar() {
    setItens([])
  }

  return { itens, adicionarArquivos, remover, limpar }
}

export type ArquivosImportados = ReturnType<typeof useArquivosImportados>
