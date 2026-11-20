import { encerrarSessao, getToken } from './sessao.ts'

// fetch que envia o token JWT no cabeçalho Authorization. Se o servidor recusar o token (401: vencido, inválido ou
// usuário removido), a sessão é encerrada e o app volta para o login; quem chamou ainda recebe a resposta.
export async function fetchAutenticado(url: string, opcoes: RequestInit = {}): Promise<Response> {
  const token = getToken()
  const resposta = await fetch(url, {
    ...opcoes,
    headers: {
      ...(token && { Authorization: `Bearer ${token}` }),
      ...opcoes.headers,
    },
  })
  if (resposta.status === 401 && token) encerrarSessao(true)
  return resposta
}
