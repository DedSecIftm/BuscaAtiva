import { fetchAutenticado } from '../autenticacao/fetchAutenticado.ts'

// Resposta de POST /api/importacoes. Em `erros`, cada texto já começa com o número da linha da planilha.
export interface ResultadoDaImportacao {
  turmasCriadas: number
  alunosCriados: number
  alunosAtualizados: number
  linhasComErro: number
  erros: string[]
}

// O backend lê a planilha .xlsx inteira: o arquivo vai como está, no campo "arquivo".
export async function importarPlanilha(arquivo: File): Promise<ResultadoDaImportacao> {
  const corpoDoEnvio = new FormData()
  corpoDoEnvio.append('arquivo', arquivo)

  let resposta: Response
  try {
    // Sem Content-Type: o navegador define o multipart com o separador correto.
    resposta = await fetchAutenticado('/api/importacoes', { method: 'POST', body: corpoDoEnvio })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.')
  }

  if (!resposta.ok) {
    const corpo = await resposta.json().catch(() => ({}))
    const mensagem =
      resposta.status === 413
        ? 'O arquivo é grande demais. O limite é de 10 MB.'
        : (corpo.detail ?? 'Ocorreu um erro no servidor. Tente novamente.')
    throw new Error(mensagem)
  }
  return resposta.json()
}
