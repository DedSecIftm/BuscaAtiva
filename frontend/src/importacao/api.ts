import { getToken } from '../regras/useToken.ts'
import type { AlunoImportado } from './mapearAlunos.ts'

export interface ResultadoDaImportacao {
  criados: number
  atualizados: number
  erros: { linha: number; mensagem: string }[]
}

export async function importarAlunos(alunos: AlunoImportado[]): Promise<ResultadoDaImportacao> {
  const token = getToken()
  let resposta: Response
  try {
    resposta = await fetch('/api/alunos/importacao', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: JSON.stringify({ alunos }),
    })
  } catch {
    throw new Error('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.')
  }

  if (!resposta.ok) {
    const corpo = await resposta.json().catch(() => ({}))
    const [erroDoCampo] = Object.values((corpo.erros ?? {}) as Record<string, string>)
    throw new Error(erroDoCampo ?? corpo.detail ?? 'Ocorreu um erro no servidor. Tente novamente.')
  }
  return resposta.json()
}
