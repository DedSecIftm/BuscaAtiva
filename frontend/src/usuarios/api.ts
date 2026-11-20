import { ErroApi } from '../regras/api.ts'
import { fetchAutenticado } from '../autenticacao/fetchAutenticado.ts'
import type { DadosUsuario, UsuarioAtual } from './tipos.ts'

const URL_BASE = '/api/users'

async function requisitar(caminho: string, opcoes: RequestInit = {}): Promise<Response> {
  let resposta: Response
  try {
    resposta = await fetchAutenticado(URL_BASE + caminho, {
      ...opcoes,
      headers: {
        'Content-Type': 'application/json',
      },
    })
  } catch {
    throw new ErroApi<DadosUsuario>('Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.')
  }

  if (!resposta.ok) {
    const corpo = await resposta.json().catch(() => ({}))
    const mensagem = corpo.detail ?? 'Ocorreu um erro no servidor. Tente novamente.'
    // O 409 é o e-mail repetido: o backend não indica o campo, mas o erro pertence a ele no formulário.
    const erros = resposta.status === 409 ? { email: mensagem } : (corpo.erros ?? {})
    throw new ErroApi<DadosUsuario>(mensagem, erros, resposta.status)
  }
  return resposta
}

export async function criarUsuario(dados: DadosUsuario): Promise<void> {
  await requisitar('', { method: 'POST', body: JSON.stringify(dados) })
}

export async function buscarUsuarioAtual(): Promise<UsuarioAtual> {
  const resposta = await requisitar('/me')
  return resposta.json()
}
