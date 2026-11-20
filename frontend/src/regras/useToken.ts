import { useEffect, useSyncExternalStore } from 'react'
import { encerrarSessao, expiracaoDoToken, getToken, observarSessao, salvarToken, sessaoExpirou } from '../autenticacao/sessao.ts'

export type { UserToken } from '../autenticacao/sessao.ts'
export { getToken }

// setTimeout aceita no máximo ~24,8 dias; acima disso dispararia na hora.
const ESPERA_MAXIMA = 2 ** 31 - 1

// Estável entre renderizações: é dependência do efeito que busca o usuário atual.
const removeToken = () => encerrarSessao()

export default function useToken() {
  const token = useSyncExternalStore(observarSessao, getToken)
  const expirou = useSyncExternalStore(observarSessao, sessaoExpirou)

  // Encerra a sessão no instante em que o token vence, sem esperar a próxima requisição.
  useEffect(() => {
    if (!token) return
    const expiracao = expiracaoDoToken(token)
    if (expiracao === null) return
    const espera = Math.min(Math.max(expiracao - Date.now(), 0), ESPERA_MAXIMA)
    const temporizador = setTimeout(() => encerrarSessao(true), espera)
    return () => clearTimeout(temporizador)
  }, [token])

  return {
    token,
    expirou,
    setToken: salvarToken,
    removeToken,
  }
}
