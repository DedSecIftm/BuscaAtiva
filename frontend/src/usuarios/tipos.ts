export type PapelUsuario = 'ROLE_USER' | 'ROLE_ADMIN'

export interface DadosUsuario {
  nome: string
  email: string
  password: string
  role: PapelUsuario
}

// A confirmação de senha só existe na tela: não é enviada à API.
export interface DadosFormularioUsuario extends DadosUsuario {
  confirmacaoSenha: string
}

export interface UsuarioAtual {
  id: number
  nome: string | null
  email: string
  roles: PapelUsuario[]
}

export function ehAdministrador(usuario: UsuarioAtual | null): boolean {
  return usuario?.roles.includes('ROLE_ADMIN') ?? false
}

const FORMATO_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Retorna a mensagem de erro de cada campo inválido.
export function validarUsuario(usuario: DadosFormularioUsuario): Partial<Record<keyof DadosFormularioUsuario, string>> {
  const erros: Partial<Record<keyof DadosFormularioUsuario, string>> = {}
  const email = usuario.email.trim()
  const nome = usuario.nome.trim()
  if (!nome) erros.nome = 'Informe o nome.'
  else if (nome.length > 100) erros.nome = 'O nome pode ter no máximo 100 caracteres.'
  if (!email) erros.email = 'Informe o e-mail.'
  else if (!FORMATO_EMAIL.test(email)) erros.email = 'Informe um e-mail válido.'
  if (usuario.password.length < 8 || usuario.password.length > 72) {
    erros.password = 'A senha deve ter entre 8 e 72 caracteres.'
  }
  if (!usuario.confirmacaoSenha) erros.confirmacaoSenha = 'Repita a senha.'
  else if (usuario.confirmacaoSenha !== usuario.password) erros.confirmacaoSenha = 'As senhas não são iguais.'
  return erros
}
