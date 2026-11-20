import type { DadosRegra, Regra } from './tipos.ts'
import { fetchAutenticado } from '../autenticacao/fetchAutenticado.ts'

const URL_BASE = '/api/regras'

export type ErrosDosCampos<T = DadosRegra> = Partial<Record<keyof T, string>>

export class ErroApi<T = DadosRegra> extends Error {
  readonly erros: ErrosDosCampos<T>
  readonly status?: number

  constructor(message: string, erros: ErrosDosCampos<T> = {}, status?: number) {
    super(message)
    this.erros = erros
    this.status = status
  }
}

// O backend chama de "periodoValido" o erro de período menor que as faltas; no formulário ele pertence ao campo do período.
const CAMPOS_DO_SERVIDOR: Record<string, keyof DadosRegra> = { periodoValido: 'periodoEmDias' }

async function requisitar<T>(caminho: string, opcoes: RequestInit = {}): Promise<T> {
  let resposta: Response
  try {
    resposta = await fetchAutenticado(URL_BASE + caminho, {
      ...opcoes,
      headers: {
        'Content-Type': 'application/json',
        ...opcoes.headers,
      },
    })
  } catch {
    throw new ErroApi('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.')
  }

  if (!resposta.ok) {
    const corpo = await resposta.json().catch(() => ({}))
    const erros: ErrosDosCampos = {}
    Object.entries((corpo.erros ?? {}) as Record<string, string>).forEach(([campo, mensagem]) => {
      erros[CAMPOS_DO_SERVIDOR[campo] ?? (campo as keyof DadosRegra)] = mensagem
    })
    throw new ErroApi(corpo.detail ?? 'Ocorreu um erro no servidor. Tente novamente.', erros, resposta.status)
  }
  return resposta.status === 204 ? (undefined as T) : resposta.json()
}

export function listarRegras(): Promise<Regra[]> {
  return requisitar('')
}

export function criarRegra(dados: DadosRegra): Promise<Regra> {
  return requisitar('', { method: 'POST', body: JSON.stringify(dados) })
}

export function atualizarRegra(id: string, dados: DadosRegra | Regra): Promise<Regra> {
  return requisitar(`/${id}`, { method: 'PUT', body: JSON.stringify(dados) })
}

export function excluirRegra(id: string): Promise<void> {
  return requisitar(`/${id}`, { method: 'DELETE' })
}
