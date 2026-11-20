// Sessão do usuário: guarda o token JWT e avisa a interface quando ele deixa de valer.

const CHAVE = 'token'

export type UserToken = {
  token: string
}

type Ouvinte = () => void

const ouvintes = new Set<Ouvinte>()
let token: string | null = lerDoArmazenamento()
let expirou = false

function avisar() {
  ouvintes.forEach(ouvinte => ouvinte())
}

// Data de expiração (em milissegundos) lida do campo "exp" do JWT; null se o token não for legível.
export function expiracaoDoToken(jwt: string): number | null {
  try {
    const carga = jwt.split('.')[1]
    const json = atob(carga.replace(/-/g, '+').replace(/_/g, '/'))
    const { exp } = JSON.parse(json)
    return typeof exp === 'number' ? exp * 1000 : null
  } catch {
    return null
  }
}

function estaVencido(jwt: string): boolean {
  const expiracao = expiracaoDoToken(jwt)
  return expiracao === null || expiracao <= Date.now()
}

function lerDoArmazenamento(): string | null {
  try {
    const salvo = localStorage.getItem(CHAVE)
    if (!salvo) return null
    const guardado: string | undefined = JSON.parse(salvo)?.token
    if (guardado && !estaVencido(guardado)) return guardado
    localStorage.removeItem(CHAVE) // vencido ou corrompido
  } catch {
    // armazenamento indisponível ou JSON inválido: segue sem sessão
  }
  return null
}

export function getToken(): string | null {
  return token
}

// true quando a última sessão terminou por token vencido ou recusado (e não porque o usuário clicou em "Sair").
export function sessaoExpirou(): boolean {
  return expirou
}

export function salvarToken(userToken: UserToken) {
  token = userToken.token
  expirou = false
  try {
    localStorage.setItem(CHAVE, JSON.stringify({ token: userToken.token }))
  } catch {
    // sem armazenamento a sessão vale só até recarregar a página
  }
  avisar()
}

export function encerrarSessao(porExpiracao = false) {
  if (token === null) return
  token = null
  expirou = porExpiracao
  try {
    localStorage.removeItem(CHAVE)
  } catch {
    // ignorado
  }
  avisar()
}

export function observarSessao(ouvinte: Ouvinte): () => void {
  ouvintes.add(ouvinte)
  return () => ouvintes.delete(ouvinte)
}

// Outra aba entrou ou saiu: acompanha para que as abas não divirjam.
window.addEventListener('storage', evento => {
  if (evento.key !== CHAVE && evento.key !== null) return
  token = lerDoArmazenamento()
  avisar()
})
