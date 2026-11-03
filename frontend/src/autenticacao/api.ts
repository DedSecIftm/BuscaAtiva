import type { UserToken } from '../regras/useToken.ts'

export type Credenciais = {
  email: string
  password: string
}

export type ErrosDoLogin = Partial<Record<keyof Credenciais, string>>

export class ErroLogin extends Error {
  readonly erros: ErrosDoLogin

  constructor(message: string, erros: ErrosDoLogin = {}) {
    super(message)
    this.erros = erros
  }
}

export async function entrar(credenciais: Credenciais): Promise<UserToken> {
  let resposta: Response
  try {
    resposta = await fetch('/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credenciais),
    })
  } catch {
    throw new ErroLogin('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.')
  }

  if (!resposta.ok) {
    const corpo = await resposta.json().catch(() => ({}))
    throw new ErroLogin(corpo.detail ?? 'Não foi possível entrar. Tente novamente.', corpo.erros ?? {})
  }
  return resposta.json()
}
